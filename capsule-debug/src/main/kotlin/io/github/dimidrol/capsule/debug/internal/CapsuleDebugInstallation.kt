package io.github.dimidrol.capsule.debug.internal

import android.app.Application
import io.github.dimidrol.capsule.debug.CapsuleDebugRegistry
import io.github.dimidrol.capsule.debug.ui.CapsuleDebugOverlay
import kotlinx.coroutines.CoroutineScope

internal data class CapsuleDebugInstallation(
    val application: Application,
    val scope: CoroutineScope,
    val registry: CapsuleDebugRegistry,
    val bridgeHandle: AutoCloseable,
    val overlay: CapsuleDebugOverlay
)
