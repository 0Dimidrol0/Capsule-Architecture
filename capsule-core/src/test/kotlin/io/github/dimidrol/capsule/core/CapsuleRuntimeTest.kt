package io.github.dimidrol.capsule.core

import app.cash.turbine.test
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class CapsuleRuntimeTest {

    @Test
    fun `send intent updates state`() = runTest {
        val runtime = createRuntime(this)

        runtime.send(TestIntent.Increment)
        runCurrent()

        assertEquals(1, runtime.state.value.count)
    }

    @Test
    fun `operation executes and result updates state`() = runTest {
        val runtime = createRuntime(this)

        runtime.send(TestIntent.Load)
        runCurrent()
        runCurrent()

        assertTrue(runtime.state.value.loaded)
        assertEquals(false, runtime.state.value.loading)
    }

    @Test
    fun `effect emitted`() = runTest {
        val runtime = createRuntime(this)

        runtime.effects.test {
            runtime.send(TestIntent.Emit)
            assertEquals(TestEffect.Message("hello"), awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `middleware receives callbacks`() = runTest {
        val middleware = RecordingMiddleware()
        val runtime = createRuntime(this, listOf(middleware))

        runtime.send(TestIntent.Load)
        runCurrent()
        runCurrent()

        assertTrue(middleware.intents.contains(TestIntent.Load))
        assertTrue(middleware.startedOperations.contains(TestOperation.LoadData))
        assertTrue(middleware.operationResults.contains(TestResult.Loaded))
        assertTrue(middleware.stateChanges >= 1)
    }

    @Test
    fun `failed operation calls onError`() = runTest {
        val middleware = RecordingMiddleware()
        val runtime = createRuntime(this, listOf(middleware))

        runtime.send(TestIntent.Fail)
        runCurrent()
        runCurrent()

        assertEquals(1, middleware.errors.size)
        assertEquals(TestOperation.Crash, middleware.errors.first().first)
        assertEquals("boom", middleware.errors.first().second.message)
    }

    private fun createRuntime(
        scope: TestScope,
        middlewares: List<CapsuleMiddleware<TestIntent, TestState, TestOperation, TestResult, TestEffect>> = emptyList()
    ): TestCapsuleRuntime {
        val dispatcher = StandardTestDispatcher(scope.testScheduler)
        val config = CapsuleConfig(
            middlewares = middlewares,
            operationDispatcher = dispatcher
        )
        return TestCapsuleRuntime(
            scope = scope.backgroundScope,
            config = config
        )
    }

    private class TestCapsuleRuntime(
        scope: CoroutineScope,
        config: CapsuleConfig<TestIntent, TestState, TestOperation, TestResult, TestEffect>
    ) : CapsuleRuntime<TestIntent, TestState, TestOperation, TestResult, TestEffect>(
        initialState = TestState(),
        scope = scope,
        config = config
    ) {
        override fun reduce(
            state: TestState,
            intent: TestIntent
        ): Decision<TestState, TestOperation, TestEffect> = when (intent) {
            TestIntent.Increment -> Decision.state(state.copy(count = state.count + 1))
            TestIntent.Load -> Decision.operation(
                state = state.copy(loading = true),
                operation = TestOperation.LoadData
            )

            TestIntent.Emit -> Decision.effect(
                state = state,
                effect = TestEffect.Message("hello")
            )

            TestIntent.Fail -> Decision.operation(state = state, operation = TestOperation.Crash)
        }

        override suspend fun handleOperation(operation: TestOperation): TestResult = when (operation) {
            TestOperation.LoadData -> TestResult.Loaded
            TestOperation.Crash -> error("boom")
        }

        override fun reduceResult(
            state: TestState,
            result: TestResult
        ): Decision<TestState, TestOperation, TestEffect> = when (result) {
            TestResult.Loaded -> Decision.state(state.copy(loaded = true, loading = false))
        }
    }

    private class RecordingMiddleware : CapsuleMiddleware<TestIntent, TestState, TestOperation, TestResult, TestEffect> {
        val intents = mutableListOf<TestIntent>()
        val startedOperations = mutableListOf<TestOperation>()
        val operationResults = mutableListOf<TestResult>()
        val errors = mutableListOf<Pair<TestOperation?, Throwable>>()
        var stateChanges: Int = 0

        override suspend fun onIntent(intent: TestIntent) {
            intents += intent
        }

        override suspend fun onStateChanged(oldState: TestState, newState: TestState) {
            stateChanges += 1
        }

        override suspend fun onOperationStarted(operation: TestOperation) {
            startedOperations += operation
        }

        override suspend fun onOperationResult(operation: TestOperation, result: TestResult) {
            operationResults += result
        }

        override suspend fun onError(operation: TestOperation?, throwable: Throwable) {
            errors += operation to throwable
        }
    }

    private sealed interface TestIntent {
        data object Increment : TestIntent
        data object Load : TestIntent
        data object Emit : TestIntent
        data object Fail : TestIntent
    }

    private data class TestState(
        val count: Int = 0,
        val loading: Boolean = false,
        val loaded: Boolean = false
    )

    private sealed interface TestOperation {
        data object LoadData : TestOperation
        data object Crash : TestOperation
    }

    private sealed interface TestResult {
        data object Loaded : TestResult
    }

    private sealed interface TestEffect {
        data class Message(val value: String) : TestEffect
    }
}
