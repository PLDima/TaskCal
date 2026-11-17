package inc.dimasic.taskcal.data.remote

import com.squareup.moshi.FromJson
import com.squareup.moshi.ToJson
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

/**
 * Moshi has no built-in support for `java.time`. Dates travel as `2026-11-09` and date-times as
 * zone-less `2026-11-09T07:30:00`: the server keeps "every Monday at 09:00" at 09:00 wherever the
 * user is, exactly as Room does.
 */
class JavaTimeAdapters {

    @ToJson
    fun localDateToJson(value: LocalDate): String = value.toString()

    @FromJson
    fun localDateFromJson(value: String): LocalDate = LocalDate.parse(value)

    /** Always writes seconds; the server may omit them in responses, and parsing accepts both. */
    @ToJson
    fun localDateTimeToJson(value: LocalDateTime): String = DateTimeFormatter.ISO_LOCAL_DATE_TIME.format(value)

    @FromJson
    fun localDateTimeFromJson(value: String): LocalDateTime = LocalDateTime.parse(value)

    @ToJson
    fun dayOfWeekToJson(value: DayOfWeek): String = value.name

    @FromJson
    fun dayOfWeekFromJson(value: String): DayOfWeek = DayOfWeek.valueOf(value)
}