package io.github.dimidrol.capsule.core

/** Receives lifecycle-aware state hosts from Android shells such as BaseCapsuleViewModel. */
fun interface CapsuleDebugObserver {
    fun onTargetAvailable(target: CapsuleDebugTarget<*>): AutoCloseable
}
