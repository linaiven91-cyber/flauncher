package com.mesa.launcher.ui

import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Date

private enum class NetKind { Wifi, Ethernet, None }

/** Строка меню сверху: логотип (→ «Об этом ТВ»), пункты меню, статус сети, пункт управления, часы. */
@Composable
fun MenuBar(
    nowMs: Long,
    onAbout: () -> Unit,
    onSettings: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val net by rememberNetKind()
    val clock = remember(nowMs) {
        SimpleDateFormat("EEE d MMM  HH:mm", RuLocale).format(Date(nowMs))
    }

    Column(modifier.fillMaxWidth()) {
        Row(
            Modifier
                .fillMaxWidth()
                .height(36.dp)
                .background(Color(0x59000000))
                .padding(horizontal = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            MenuButton(onClick = onAbout) { LogoGlyph() }
            MacText("Рабочий стол", Modifier.padding(horizontal = 8.dp), size = 17.sp, weight = FontWeight.Bold)
            for (item in listOf("Файл", "Правка", "Вид", "Переход", "Окно", "Справка")) {
                MacText(item, Modifier.padding(horizontal = 8.dp), size = 17.sp, color = Color.White.copy(alpha = 0.92f))
            }

            Spacer(Modifier.weight(1f))

            when (net) {
                NetKind.Ethernet -> EthernetGlyph()
                NetKind.Wifi -> WifiGlyph(1f)
                NetKind.None -> WifiGlyph(0.35f)
            }
            Spacer(Modifier.width(6.dp))
            MenuButton(onClick = onSettings) { ControlGlyph() }
            MacText(clock, Modifier.padding(start = 6.dp, end = 4.dp), size = 17.sp, weight = FontWeight.Medium)
        }
        Box(
            Modifier
                .fillMaxWidth()
                .height(1.dp)
                .background(Color(0x26FFFFFF))
        )
    }
}

@Composable
private fun MenuButton(onClick: () -> Unit, content: @Composable () -> Unit) {
    var focused by remember { mutableStateOf(false) }
    Box(
        Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(if (focused) Color.White.copy(alpha = 0.30f) else Color.Transparent)
            .tvPress(onClick)
            .onFocusChanged { focused = it.isFocused }
            .focusable()
            .padding(horizontal = 9.dp, vertical = 4.dp),
        contentAlignment = Alignment.Center,
    ) { content() }
}

@Composable
private fun LogoGlyph() {
    Canvas(Modifier.size(16.dp)) {
        val gap = size.width * 0.12f
        val cell = (size.width - gap) / 2f
        val radius = CornerRadius(cell * 0.28f)
        for (row in 0..1) {
            for (col in 0..1) {
                drawRoundRect(
                    Color.White,
                    Offset(col * (cell + gap), row * (cell + gap)),
                    Size(cell, cell),
                    radius,
                )
            }
        }
    }
}

@Composable
private fun ControlGlyph() {
    Canvas(Modifier.size(18.dp, 14.dp)) {
        val pillH = size.height * 0.40f
        val radius = CornerRadius(pillH / 2f)
        drawRoundRect(Color.White.copy(alpha = 0.55f), Offset(0f, 0f), Size(size.width, pillH), radius)
        drawCircle(Color.White, pillH * 0.38f, Offset(size.width - pillH / 2f, pillH / 2f))
        drawRoundRect(
            Color.White.copy(alpha = 0.55f),
            Offset(0f, size.height - pillH),
            Size(size.width, pillH),
            radius,
        )
        drawCircle(Color.White, pillH * 0.38f, Offset(pillH / 2f, size.height - pillH / 2f))
    }
}

@Composable
private fun WifiGlyph(alpha: Float) {
    Canvas(Modifier.size(22.dp, 16.dp)) {
        val color = Color.White.copy(alpha = alpha)
        val stroke = Stroke(width = size.height * 0.16f, cap = StrokeCap.Round)
        val cx = size.width / 2f
        val cy = size.height
        for (f in listOf(0.95f, 0.66f, 0.38f)) {
            val r = size.height * f
            drawArc(
                color = color,
                startAngle = 225f,
                sweepAngle = 90f,
                useCenter = false,
                topLeft = Offset(cx - r, cy - r),
                size = Size(r * 2f, r * 2f),
                style = stroke,
            )
        }
        drawCircle(color, size.height * 0.09f, Offset(cx, cy - size.height * 0.04f))
    }
}

@Composable
private fun EthernetGlyph() {
    Canvas(Modifier.size(16.dp, 14.dp)) {
        val stroke = Stroke(width = size.height * 0.14f, cap = StrokeCap.Round)
        drawRoundRect(
            Color.White,
            Offset(stroke.width / 2f, stroke.width / 2f),
            Size(size.width - stroke.width, size.height * 0.62f),
            CornerRadius(size.height * 0.12f),
            style = stroke,
        )
        for (i in 1..3) {
            val x = size.width * i / 4f
            drawLine(
                Color.White,
                Offset(x, size.height * 0.18f),
                Offset(x, size.height * 0.44f),
                strokeWidth = stroke.width,
                cap = StrokeCap.Round,
            )
        }
        drawLine(
            Color.White,
            Offset(size.width / 2f, size.height * 0.64f),
            Offset(size.width / 2f, size.height),
            strokeWidth = stroke.width,
            cap = StrokeCap.Round,
        )
    }
}

@Composable
private fun rememberNetKind(): State<NetKind> {
    val context = LocalContext.current
    return produceState(NetKind.None) {
        val cm = context.getSystemService(ConnectivityManager::class.java)
        while (true) {
            value = try {
                val caps = cm?.getNetworkCapabilities(cm.activeNetwork)
                when {
                    caps == null -> NetKind.None
                    caps.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> NetKind.Wifi
                    caps.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> NetKind.Ethernet
                    else -> NetKind.None
                }
            } catch (e: Exception) {
                NetKind.None
            }
            delay(10_000)
        }
    }
}
