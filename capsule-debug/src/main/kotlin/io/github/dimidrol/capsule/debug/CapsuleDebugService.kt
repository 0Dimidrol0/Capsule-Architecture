package io.github.dimidrol.capsule.debug

import android.app.Application
import io.github.dimidrol.capsule.core.CapsuleDebugBridge
import io.github.dimidrol.capsule.debug.internal.CapsuleDebugInstallation
import io.github.dimidrol.capsule.debug.ui.CapsuleDebugOverlay
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.StateFlow

/** Installs the Capsule state inspector and its in-app floating button for debug builds. */
object CapsuleDebugService {
    private var installation: CapsuleDebugInstallation? = null

    val isInstalled: Boolean
        get() = installation != null

    val sessions: StateFlow<List<CapsuleDebugSession>>
        get() = checkNotNull(installation) {
            "CapsuleDebugService.install(application) must be called first"
        }.registry.sessions

    /**
     * Installs debug tracking application-wide. Repeated calls keep the existing installation.
     *
     * Prefer calling this from a debug-only Application source set.
     */
    @Synchronized
    fun install(
        application: Application,
        config: CapsuleDebugConfig = CapsuleDebugConfig()
    ) {
        if (installation != null) return

        val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
        val registry = CapsuleDebugRegistry(scope = scope, config = config)
        val bridgeHandle = CapsuleDebugBridge.install(registry)
        val overlay = CapsuleDebugOverlay(
            registry = registry,
            config = config
        )
        application.registerActivityLifecycleCallbacks(overlay)

        installation = CapsuleDebugInstallation(
            application = application,
            scope = scope,
            registry = registry,
            bridgeHandle = bridgeHandle,
            overlay = overlay
        )
    }

    /** Removes the overlay, clears histories, and detaches the debug bridge. */
    @Synchronized
    fun uninstall() {
        val current = installation ?: return
        installation = null

        current.application.unregisterActivityLifecycleCallbacks(current.overlay)
        current.overlay.close()
        current.bridgeHandle.close()
        current.registry.close()
        current.scope.cancel()
    }
}
