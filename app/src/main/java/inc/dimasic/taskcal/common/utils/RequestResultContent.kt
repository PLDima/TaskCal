package inc.dimasic.taskcal.common.utils

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import kotlinx.coroutines.flow.StateFlow

/**
 * Renders [onSuccess], [onError] or [onLoading] for the current [RequestResult] a
 * [inc.dimasic.taskcal.common.CommonViewModel] exposes via `getState()`. [RequestResult.Error]
 * and [RequestResult.Failure] both route to [onError] — this app has no API layer producing
 * [RequestResult.Error] yet, so splitting them at every call site would be unused ceremony today.
 */
@Composable
fun <T> StateFlow<RequestResult<T>>.defaultRequestHandler(
    onSuccess: @Composable (T) -> Unit,
    onError: @Composable (message: String) -> Unit = { DefaultErrorContent(it) },
    onLoading: @Composable () -> Unit = { DefaultLoadingDialog() },
) {
    when (val state = collectAsState().value) {
        is RequestResult.Success -> onSuccess(state.data)
        is RequestResult.Error -> onError(state.message)
        is RequestResult.Failure -> onError(state.exception.message ?: "Something went wrong.")
        is RequestResult.Loading -> if (state.state) onLoading()
        is RequestResult.IDLE -> Unit
    }
}

/**
 * Non-dismissible by design: blocking input while a request is in flight is what keeps rapidly
 * re-triggering (paging through days, switching period) from racing.
 */
@Composable
fun DefaultLoadingDialog() {
    Dialog(
        onDismissRequest = {},
        properties = DialogProperties(dismissOnBackPress = false, dismissOnClickOutside = false),
    ) {
        Surface(shape = RoundedCornerShape(12.dp)) {
            Box(modifier = Modifier.padding(24.dp)) {
                CircularProgressIndicator()
            }
        }
    }
}

@Composable
fun DefaultErrorContent(message: String, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Icon(
            imageVector = Icons.Filled.ErrorOutline,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.error,
        )
        Text(
            text = message,
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center,
        )
    }
}
