package io.github.dimidrol.capsule.debug

import io.github.dimidrol.capsule.core.CapsuleDebugObserver
import io.github.dimidrol.capsule.core.CapsuleDebugTarget
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicLong
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

/** Tracks all active Capsule state hosts and owns their bounded histories. */
class CapsuleDebugRegistry(
    private val scope: CoroutineScope,
    private val config: CapsuleDebugConfig = CapsuleDebugConfig(),
    private val nowMillis: () -> Long = { System.currentTimeMillis() }
) : CapsuleDebugObserver, AutoCloseable {

    private val nextSessionId = AtomicLong(0)
    private val mutableSessions = MutableStateFlow<List<CapsuleDebugSession>>(emptyList())
    private val closed = AtomicBoolean(false)

    val sessions: StateFlow<List<CapsuleDebugSession>> = mutableSessions.asStateFlow()

    override fun onTargetAvailable(target: CapsuleDebugTarget<*>): AutoCloseable {
        check(!closed.get()) { "CapsuleDebugRegistry is closed" }

        // Snapshots are sent back only to the target that produced them, preserving type safety.
        @Suppress("UNCHECKED_CAST")
        val untypedTarget = target as CapsuleDebugTarget<Any?>
        val session = CapsuleDebugSession(
            id = nextSessionId.incrementAndGet(),
            name = target.debugName,
            target = untypedTarget,
            scope = scope,
            config = config,
            nowMillis = nowMillis
        )
        val registrationClosed = AtomicBoolean(false)

        mutableSessions.update { current -> current + session }

        return AutoCloseable {
            if (registrationClosed.compareAndSet(false, true)) {
                session.close()
                mutableSessions.update { current -> current.filterNot { it.id == session.id } }
            }
        }
    }

    override fun close() {
        if (!closed.compareAndSet(false, true)) return

        mutableSessions.value.forEach(CapsuleDebugSession::close)
        mutableSessions.value = emptyList()
    }
}
