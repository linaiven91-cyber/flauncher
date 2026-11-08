package com.mesa.launcher.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.util.Locale

/**
 * Весь интерфейс рисуется на «виртуальном» экране 1280x720 dp
 * и масштабируется под реальное разрешение ТВ (см. MacTvApp).
 */
const val DESIGN_WIDTH_DP = 1280f

val RuLocale: Locale = Locale("ru", "RU")

object MacColors {
    val TextSecondary = Color.White.copy(alpha = 0.72f)
    val Accent = Color(0xFF0A84FF)
    val TrafficRed = Color(0xFFFF5F57)
    val TrafficYellow = Color(0xFFFEBC2E)
    val TrafficGreen = Color(0xFF28C840)
    val CalendarRed = Color(0xFFFF453A)
}

/** «Матовое стекло»: на API 30 настоящий backdrop-blur недоступен, поэтому имитируем. */
fun Modifier.glass(shape: Shape, strength: Float = 1f): Modifier =
    this
        .clip(shape)
        .background(Color(0x40101018))
        .background(
            Brush.verticalGradient(
                listOf(
                    Color.White.copy(alpha = 0.22f * strength),
                    Color.White.copy(alpha = 0.08f * strength),
                )
            )
        )
        .border(1.dp, Color.White.copy(alpha = 0.30f), shape)

@Composable
fun MacText(
    text: String,
    modifier: Modifier = Modifier,
    size: TextUnit = 14.sp,
    weight: FontWeight = FontWeight.Normal,
    color: Color = Color.White,
    maxLines: Int = 1,
    align: TextAlign = TextAlign.Unspecified,
    shadow: Boolean = true,
) {
    BasicText(
        text = text,
        modifier = modifier,
        maxLines = maxLines,
        overflow = TextOverflow.Ellipsis,
        style = TextStyle(
            color = color,
            fontSize = size,
            fontWeight = weight,
            textAlign = align,
            shadow = if (shadow) Shadow(Color(0x66000000), Offset(0f, 1f), 3f) else null,
        ),
    )
}
