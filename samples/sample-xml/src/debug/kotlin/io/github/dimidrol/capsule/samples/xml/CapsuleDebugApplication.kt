package io.github.dimidrol.capsule.samples.xml

import android.app.Application
import io.github.dimidrol.capsule.debug.CapsuleDebugConfig
import io.github.dimidrol.capsule.debug.CapsuleDebugService

class CapsuleDebugApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        CapsuleDebugService.install(
            application = this,
            config = CapsuleDebugConfig(maxSnapshotsPerScreen = 50)
        )
    }
}
