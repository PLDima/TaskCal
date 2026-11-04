package inc.dimasic.taskcal.ui.screens.statistic.statistic_views

import inc.dimasic.taskcal.domain.model.DateRange
import java.time.format.DateTimeFormatter

private val rangeLabelFormatter = DateTimeFormatter.ofPattern("MMM d")
private val singleDayFormatter = DateTimeFormatter.ofPattern("EEE, MMM d")

/** How [PeriodNavigator] words the period it's showing: "Sep 14 - Sep 20", or "Mon, Sep 14" for a single day. */
internal fun DateRange.toLabel(): String =
    if (start == end) {
        start.format(singleDayFormatter)
    } else {
        "${start.format(rangeLabelFormatter)} - ${end.format(rangeLabelFormatter)}"
    }
