package io.github.dimidrol.capsule.debug

import io.github.dimidrol.capsule.core.CapsuleDebugTarget
import io.github.dimidrol.capsule.core.CapsuleStateMode
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class CapsuleDebugRegistryTest {

    @Test
    fun `history is bounded and keeps latest live states`() = runTest {
        var clock = 100L
        val registry = CapsuleDebugRegistry(
            scope = backgroundScope,
            config = CapsuleDebugConfig(maxSnapshotsPerScreen = 3),
            nowMillis = { clock++ }
        )
        val target = FakeTarget()
        registry.onTargetAvailable(target)
        runCurrent()

        target.emit(1)
        runCurrent()
        target.emit(2)
        runCurrent()
        target.emit(3)
        runCurrent()

        val states = registry.sessions.value.single().snapshots.value.map { it.state }
        assertEquals(listOf(1, 2, 3), states)
    }

    @Test
    fun `snapshot preview and live resume are delegated to target`() = runTest {
        val registry = CapsuleDebugRegistry(scope = backgroundScope)
        val target = FakeTarget()
        registry.onTargetAvailable(target)
        runCurrent()
        target.emit(7)
        runCurrent()
        val session = registry.sessions.value.single()
        val snapshot = session.snapshots.value.last()

        assertTrue(session.preview(snapshot.id))
        assertEquals(7, target.previewedState)
        assertEquals(CapsuleStateMode.Preview, target.stateMode.value)

        session.resumeLive()
        assertEquals(CapsuleStateMode.Live, target.stateMode.value)
        assertFalse(session.preview(Long.MAX_VALUE))
    }

    @Test
    fun `closing registration removes session and resumes live state`() = runTest {
        val registry = CapsuleDebugRegistry(scope = backgroundScope)
        val target = FakeTarget()
        val registration = registry.onTargetAvailable(target)
        runCurrent()
        val snapshot = registry.sessions.value.single().snapshots.value.single()
        registry.sessions.value.single().preview(snapshot.id)

        registration.close()

        assertTrue(registry.sessions.value.isEmpty())
        assertEquals(CapsuleStateMode.Live, target.stateMode.value)
    }

    private class FakeTarget : CapsuleDebugTarget<Any?> {
        private val mutableLiveState = MutableStateFlow<Any?>(0)
        private val mutableMode = MutableStateFlow(CapsuleStateMode.Live)

        var previewedState: Any? = null
            private set

        override val debugName: String = "FakeScreen"
        override val liveState: StateFlow<Any?> = mutableLiveState
        override val stateMode: StateFlow<CapsuleStateMode> = mutableMode

        override fun previewState(state: Any?) {
            previewedState = state
            mutableMode.value = CapsuleStateMode.Preview
        }

        override fun resumeLiveState() {
            previewedState = null
            mutableMode.value = CapsuleStateMode.Live
        }

        fun emit(state: Any?) {
            mutableLiveState.value = state
        }
    }
}
