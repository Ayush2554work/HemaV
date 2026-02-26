package com.meditech.hemav.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.meditech.hemav.ui.theme.AyurvedicGreen
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun HemaVCalendarStrip(
    modifier: Modifier = Modifier,
    selectedDate: Calendar = Calendar.getInstance(),
    onDateSelected: (Calendar) -> Unit = {}
) {
    val days = remember {
        val list = mutableListOf<Calendar>()
        val cal = Calendar.getInstance()
        // Start from beginning of current week (fallback to today if needed)
        // Let's just show 14 days starting from today for simplicity
        for (i in 0 until 14) {
            val d = cal.clone() as Calendar
            list.add(d)
            cal.add(Calendar.DAY_OF_YEAR, 1)
        }
        list
    }

    val monthFormat = remember { SimpleDateFormat("MMMM yyyy", Locale.getDefault()) }
    val dayNameFormat = remember { SimpleDateFormat("EEE", Locale.getDefault()) }
    val dayNumberFormat = remember { SimpleDateFormat("dd", Locale.getDefault()) }

    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = monthFormat.format(days.first().time),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(horizontal = 4.dp, vertical = 8.dp)
        )

        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            contentPadding = PaddingValues(horizontal = 4.dp)
        ) {
            items(days) { date ->
                val isSelected = isSameDay(date, selectedDate)
                val isToday = isSameDay(date, Calendar.getInstance())

                CalendarDayItem(
                    dayName = dayNameFormat.format(date.time),
                    dayNumber = dayNumberFormat.format(date.time),
                    isSelected = isSelected,
                    isToday = isToday,
                    onClick = { onDateSelected(date) }
                )
            }
        }
    }
}

@Composable
private fun CalendarDayItem(
    dayName: String,
    dayNumber: String,
    isSelected: Boolean,
    isToday: Boolean,
    onClick: () -> Unit
) {
    val backgroundColor = when {
        isSelected -> AyurvedicGreen
        isToday -> AyurvedicGreen.copy(alpha = 0.15f)
        else -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
    }

    val contentColor = when {
        isSelected -> Color.White
        isToday -> AyurvedicGreen
        else -> MaterialTheme.colorScheme.onSurface
    }

    Column(
        modifier = Modifier
            .width(55.dp)
            .height(75.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(backgroundColor)
            .clickable(onClick = onClick)
            .padding(vertical = 10.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = dayName.uppercase(),
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Medium,
            color = contentColor.copy(alpha = 0.8f),
            fontSize = 10.sp
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = dayNumber,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = contentColor,
            fontSize = 18.sp
        )
        if (isToday && !isSelected) {
            Box(
                modifier = Modifier
                    .size(4.dp)
                    .clip(CircleShape)
                    .background(AyurvedicGreen)
            )
        }
    }
}

private fun isSameDay(cal1: Calendar, cal2: Calendar): Boolean {
    return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
            cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR)
}
