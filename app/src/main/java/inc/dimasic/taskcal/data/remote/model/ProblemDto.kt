package inc.dimasic.taskcal.data.remote.model

import com.squareup.moshi.JsonClass

/**
 * The `application/problem+json` body the server sends with every error. [errors] lists every
 * broken validation rule at once, not just the first.
 */
@JsonClass(generateAdapter = true)
data class ProblemDto(
    val type: String? = null,
    val title: String? = null,
    val status: Int? = null,
    val detail: String? = null,
    val errors: List<String> = emptyList(),
)