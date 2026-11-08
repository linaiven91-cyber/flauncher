package com.mesa.launcher.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date

/** Виджет «Часы» — крупное время и дата. */
@Composable
fun ClockWidget(nowMs: Long, modifier: Modifier = Modifier) {
    val time = remember(nowMs) { SimpleDateFormat("HH:mm", RuLocale).format(Date(nowMs)) }
    val date = remember(nowMs) {
        SimpleDateFormat("EEEE, d MMMM", RuLocale).format(Date(nowMs))
            .replaceFirstChar { it.uppercase() }
    }
    Column(
        modifier
            .width(280.dp)
            .glass(RoundedCornerShape(22.dp))
            .padding(horizontal = 20.dp, vertical = 14.dp)
    ) {
        MacText(time, size = 54.sp, weight = FontWeight.Light)
        MacText(date, size = 18.sp, color = MacColors.TextSecondary)
    }
}

/** Виджет «Календарь» — текущий месяц, сегодняшний день выделен красным кружком. */
@Composable
fun CalendarWidget(nowMs: Long, modifier: Modifier = Modifier) {
    val cal = Calendar.getInstance().apply { timeInMillis = nowMs }
    val today = cal.get(Calendar.DAY_OF_MONTH)
    val title = SimpleDateFormat("LLLL yyyy", RuLocale).format(cal.time)
        .replaceFirstChar { it.uppercase() }
    val first = (cal.clone() as Calendar).apply { set(Calendar.DAY_OF_MONTH, 1) }
    val offset = (first.get(Calendar.DAY_OF_WEEK) + 5) % 7 // понедельник = 0
    val daysInMonth = cal.getActualMaximum(Calendar.DAY_OF_MONTH)
    val rows = (List(offset) { 0 } + (1..daysInMonth).toList()).chunked(7)

    Column(
        modifier
            .width(280.dp)
            .glass(RoundedCornerShape(22.dp))
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(2.dp),
    ) {
        MacText(
            title,
            Modifier.padding(start = 4.dp, bottom = 4.dp),
            size = 18.sp,
            weight = FontWeight.Bold,
            color = MacColors.CalendarRed,
            shadow = false,
        )
        Row {
            for (name in listOf("Пн", "Вт", "Ср", "Чт", "Пт", "Сб", "Вс")) {
                MacText(
                    name,
                    Modifier.width(34.dp),
                    size = 14.sp,
                    color = MacColors.TextSecondary,
                    align = TextAlign.Center,
                    shadow = false,
                )
            }
        }
        for (row in rows) {
            Row {
                for (i in 0 until 7) {
                    DayCell(day = row.getOrNull(i) ?: 0, isToday = row.getOrNull(i) == today, weekend = i >= 5)
                }
            }
        }
    }
}

@Composable
private fun DayCell(day: Int, isToday: Boolean, weekend: Boolean) {
    Box(
        Modifier
            .width(34.dp)
            .height(30.dp),
        contentAlignment = Alignment.Center,
    ) {
        if (day == 0) return@Box
        if (isToday) {
            Box(Modifier.size(28.dp).background(MacColors.CalendarRed, CircleShape))
        }
        MacText(
            day.toString(),
            size = 16.sp,
            weight = if (isToday) FontWeight.Bold else FontWeight.Normal,
            color = if (weekend && !isToday) Color.White.copy(alpha = 0.6f) else Color.White,
            align = TextAlign.Center,
            shadow = false,
        )
    }
}
