package inc.dimasic.taskcal.common

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import inc.dimasic.taskcal.common.utils.RequestResult
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * Base class for a ViewModel whose main UI data comes from one request of type [T]. [getState]
 * is the single state the UI renders for that request; [launchRequest] is how the ViewModel fills
 * it in.
 */
open class CommonViewModel<T> : ViewModel() {

    private val internalStateHolderFlow = MutableStateFlow<RequestResult<T>>(RequestResult.Loading(true))
    private var requestJob: Job? = null

    protected fun emitLoadingState(state: Boolean = true) {
        internalStateHolderFlow.value = RequestResult.Loading(state)
    }

    protected fun emitState(result: RequestResult<T>) {
        internalStateHolderFlow.value = result
    }

    /** Cancels whatever request is still running for this ViewModel, if any. */
    open fun setDefaultState() {
        requestJob?.cancel()
    }

    fun getState() = internalStateHolderFlow.asStateFlow()

    /**
     * Runs [request] on [Dispatchers.IO], setting Loading first and Success/Failure once it
     * resolves. A still-running previous request is cancelled before this one starts, since both
     * would write into the same [getState] — this is what keeps rapidly re-triggering (paging
     * through days, switching period) from letting a slower, superseded request overwrite a
     * newer result. Exceptions become [RequestResult.Failure] rather than crashing the app;
     * [RequestResult.Error] stays reserved for an explicit API-style failure a request itself
     * returns, which a generic catch has no `code`/`message` to construct.
     */
    protected fun launchRequest(request: suspend () -> T) {
        requestJob?.cancel()
        emitLoadingState(true)
        requestJob = viewModelScope.launch(Dispatchers.IO) {
            try {
                emitState(RequestResult.Success(request()))
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                emitState(RequestResult.Failure(e))
            }
        }
    }

    protected fun RequestResult<T>.defaultRequestHandler(
        onSuccess: (data: T) -> Unit,
        onErrorAction: (code: Int, message: String) -> Unit = { code, message ->
            internalStateHolderFlow.value = RequestResult.Error(code, message)
        },
        onFailure: (exception: Throwable) -> Unit = { exception ->
            internalStateHolderFlow.value = RequestResult.Failure(exception)
        },
    ) {
        when (this) {
            is RequestResult.Success -> onSuccess(this.data)
            is RequestResult.Error -> onErrorAction(this.code, this.message)
            is RequestResult.Failure -> onFailure(this.exception)
            is RequestResult.Loading -> Unit
            is RequestResult.IDLE -> Unit
        }
    }
}
