package io.github.dimidrol.capsule.base.viewmodel

import io.github.dimidrol.capsule.core.Capsule
import io.github.dimidrol.capsule.core.CapsuleStateMode
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalCoroutinesApi::class)
class BaseCapsuleViewModelTest {

    private val mainDispatcher = StandardTestDispatcher()

    @Before
    fun setUp() {
        Dispatchers.setMain(mainDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `preview changes presented state but preserves live state`() = runTest(mainDispatcher) {
        val viewModel = TestViewModel()
        viewModel.state
        runCurrent()

        viewModel.runtimeState.value = TestState(value = 2)
        runCurrent()
        viewModel.previewState(TestState(value = 1))
        runCurrent()

        assertEquals(TestState(value = 1), viewModel.state.value)
        assertEquals(TestState(value = 2), viewModel.liveState.value)
        assertEquals(CapsuleStateMode.Preview, viewModel.stateMode.value)
    }

    @Test
    fun `sending intent resumes live state`() = runTest(mainDispatcher) {
        val viewModel = TestViewModel()
        viewModel.state
        runCurrent()
        viewModel.runtimeState.value = TestState(value = 2)
        viewModel.previewState(TestState(value = 1))
        runCurrent()

        viewModel.send(TestIntent.Refresh)
        runCurrent()

        assertEquals(TestState(value = 2), viewModel.state.value)
        assertEquals(CapsuleStateMode.Live, viewModel.stateMode.value)
        assertEquals(listOf<TestIntent>(TestIntent.Refresh), viewModel.receivedIntents)
    }

    private class TestViewModel : BaseCapsuleViewModel<TestIntent, TestState, Nothing>() {
        val runtimeState = MutableStateFlow(TestState())
        val receivedIntents = mutableListOf<TestIntent>()

        override fun buildCapsule(scope: CoroutineScope): Capsule<TestIntent, TestState, Nothing> =
            object : Capsule<TestIntent, TestState, Nothing> {
                override val state: StateFlow<TestState> = runtimeState
                override val effects: Flow<Nothing> = emptyFlow()

                override fun send(intent: TestIntent) {
                    receivedIntents += intent
                }
            }
    }

    private data class TestState(val value: Int = 0)

    private sealed interface TestIntent {
        data object Refresh : TestIntent
    }
}
