package io.github.dimidrol.capsule.core

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel

/**
 * Runtime configuration for [CapsuleRuntime].
 */
data class CapsuleConfig<Intent, State, Operation, Result, Effect>(
    val middlewares: List<CapsuleMiddleware<Intent, State, Operation, Result, Effect>> = emptyList(),
    val operationDispatcher: CoroutineDispatcher = Dispatchers.Default,
    val effectBufferCapacity: Int = Channel.BUFFERED
)
