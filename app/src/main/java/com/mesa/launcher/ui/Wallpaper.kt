package com.mesa.launcher.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path

/**
 * Собственные обои в духе macOS: плавный градиент, мягкие световые пятна и «волны».
 * Нарисованы кодом (без картинок), статичные — чтобы не нагружать GPU телевизора.
 */
@Composable
fun MacWallpaper(modifier: Modifier = Modifier) {
    Canvas(modifier.fillMaxSize()) {
        val w = size.width
        val h = size.height

        drawRect(
            Brush.linearGradient(
                colors = listOf(
                    Color(0xFF14104F),
                    Color(0xFF4A2A9B),
                    Color(0xFFD9467F),
                    Color(0xFFFF9F5A),
                ),
                start = Offset(0f, 0f),
                end = Offset(w, h),
            )
        )

        // Световые пятна
        val blueCenter = Offset(w * 0.18f, h * 0.28f)
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(Color(0x9948C6EF), Color.Transparent),
                center = blueCenter,
                radius = w * 0.42f,
            ),
            radius = w * 0.42f,
            center = blueCenter,
        )
        val pinkCenter = Offset(w * 0.78f, h * 0.78f)
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(Color(0x80FF6AD5), Color.Transparent),
                center = pinkCenter,
                radius = w * 0.45f,
            ),
            radius = w * 0.45f,
            center = pinkCenter,
        )

        // Две полупрозрачные «волны»
        val wave1 = Path().apply {
            moveTo(0f, h * 0.70f)
            cubicTo(w * 0.25f, h * 0.52f, w * 0.52f, h * 0.88f, w, h * 0.60f)
            lineTo(w, h)
            lineTo(0f, h)
            close()
        }
        drawPath(
            wave1,
            Brush.verticalGradient(
                colors = listOf(Color(0x30FFFFFF), Color(0x00FFFFFF)),
                startY = h * 0.5f,
                endY = h,
            ),
        )
        val wave2 = Path().apply {
            moveTo(0f, h * 0.82f)
            cubicTo(w * 0.30f, h * 0.70f, w * 0.62f, h * 1.00f, w, h * 0.74f)
            lineTo(w, h)
            lineTo(0f, h)
            close()
        }
        drawPath(
            wave2,
            Brush.verticalGradient(
                colors = listOf(Color(0x26000000), Color(0x00000000)),
                startY = h * 0.65f,
                endY = h,
            ),
        )

        // Затемнение сверху — для читаемости строки меню
        drawRect(
            Brush.verticalGradient(
                colors = listOf(Color(0x40000000), Color.Transparent),
                startY = 0f,
                endY = h * 0.14f,
            )
        )
    }
}
