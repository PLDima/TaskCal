package inc.dimasic.taskcal

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking

/**
 * Takes the first value a flow emits. The use cases and repositories all build their flows with
 * `combine`, so feeding them finite `flowOf(...)` inputs makes the result emit once and settle.
 */
internal fun <T> Flow<T>.firstValue(): T = runBlocking { first() }
