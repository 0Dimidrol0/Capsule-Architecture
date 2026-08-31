package io.github.dimidrol.capsule.core

/**
 * Optional bridge between Capsule Android shells and a separately installed debug module.
 *
 * The bridge is empty in production unless debug tooling installs an observer.
 */
object CapsuleDebugBridge {
    @Volatile
    private var observer: CapsuleDebugObserver? = null

    /** Installs [newObserver] and returns a handle that removes only that observer. */
    fun install(newObserver: CapsuleDebugObserver): AutoCloseable {
        observer = newObserver
        return AutoCloseable {
            if (observer === newObserver) {
                observer = null
            }
        }
    }

    /** Connects [target] when debug tooling is installed, otherwise returns a no-op handle. */
    fun connect(target: CapsuleDebugTarget<*>): AutoCloseable =
        observer?.onTargetAvailable(target) ?: AutoCloseable { Unit }
}
