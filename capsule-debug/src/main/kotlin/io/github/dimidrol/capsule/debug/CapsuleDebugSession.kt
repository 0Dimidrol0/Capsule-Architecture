package io.github.dimidrol.capsule.debug

import io.github.dimidrol.capsule.core.CapsuleDebugTarget
import io.github.dimidrol.capsule.core.CapsuleStateMode
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicLong
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/** One tracked ViewModel/screen and its bounded state history. */
class CapsuleDebugSession internal constructor(
    val id: Long,
    val name: String,
    private val target: CapsuleDebugTarget<Any?>,
    private val scope: CoroutineScope,
    private val config: CapsuleDebugConfig,
    private val nowMillis: () -> Long
) : AutoCloseable {

    private val sequence = AtomicLong(0)
    private val mutableSnapshots = MutableStateFlow<List<CapsuleStateSnapshot>>(emptyList())
    private val closed = AtomicBoolean(false)
    private val collectionJob: Job

    val snapshots: StateFlow<List<CapsuleStateSnapshot>> = mutableSnapshots.asStateFlow()
    val stateMode: StateFlow<CapsuleStateMode> = target.stateMode

    init {
        collectionJob = scope.launch {
            target.liveState.collect { state ->
                appendSnapshot(state)
            }
        }
    }

    /** Shows a historical state in the screen UI without changing its live runtime state. */
    fun preview(snapshotId: Long): Boolean {
        val snapshot = mutableSnapshots.value.firstOrNull { it.id == snapshotId } ?: return false
        target.previewState(snapshot.state)
        return true
    }

    /** Returns the screen UI to its latest runtime state. */
    fun resumeLive() {
        target.resumeLiveState()
    }

    /** Clears old history while retaining the current live state as a new first snapshot. */
    fun clearHistory() {
        mutableSnapshots.value = emptyList()
        appendSnapshot(target.liveState.value)
    }

    override fun close() {
        if (!closed.compareAndSet(false, true)) return

        collectionJob.cancel()
        target.resumeLiveState()
    }

    private fun appendSnapshot(state: Any?) {
        val rawText = runCatching { config.stateFormatter(state) }
            .getOrElse { throwable -> "<formatter failed: ${throwable.message}>" }
        val rendered = rawText.take(config.maxRenderedStateLength)
        val nextSequence = sequence.incrementAndGet()
        val snapshot = CapsuleStateSnapshot(
            id = nextSequence,
            sequence = nextSequence,
            capturedAtMillis = nowMillis(),
            renderedState = rendered,
            state = state
        )

        mutableSnapshots.update { current ->
            (current + snapshot).takeLast(config.maxSnapshotsPerScreen)
        }
    }
}
