package com.mesa.launcher.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.focusGroup
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusProperties
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mesa.launcher.data.readDeviceInfo
import kotlinx.coroutines.delay

private val WindowShape = RoundedCornerShape(14.dp)

/** Окно «Об этом ТВ» — открывается по логотипу в левом углу строки меню. */
@Composable
fun AboutDialog(
    visible: Boolean,
    onClose: () -> Unit,
    onOpenSettings: () -> Unit,
    modifier: Modifier = Modifier,
) {
    AnimatedVisibility(
        visible = visible,
        modifier = modifier,
        enter = fadeIn(tween(180)) + scaleIn(initialScale = 0.92f, animationSpec = tween(220)),
        exit = fadeOut(tween(120)) + scaleOut(targetScale = 0.95f, animationSpec = tween(140)),
    ) {
        AboutContent(onClose, onOpenSettings)
    }
}

@Composable
private fun AboutContent(onClose: () -> Unit, onOpenSettings: () -> Unit) {
    val closeButton = remember { FocusRequester() }
    val info = remember { readDeviceInfo() }
    LaunchedEffect(Unit) {
        delay(120)
        runCatching { closeButton.requestFocus() }
    }

    Box(
        Modifier
            .fillMaxSize()
            .background(Color(0x66000000))
            .focusProperties { exit = { FocusRequester.Cancel } }
            .focusGroup(),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            Modifier
                .width(620.dp)
                .clip(WindowShape)
                .background(Color(0xF2262630))
                .border(1.dp, Color.White.copy(alpha = 0.25f), WindowShape)
        ) {
            // Заголовок окна с «светофором»
            Row(
                Modifier
                    .fillMaxWidth()
                    .height(40.dp)
                    .background(Color.White.copy(alpha = 0.06f))
                    .padding(horizontal = 14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                TrafficLight(MacColors.TrafficRed, onClick = onClose, closeGlyph = true)
                TrafficLight(MacColors.TrafficYellow)
                TrafficLight(MacColors.TrafficGreen)
            }

            Column(
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 32.dp, vertical = 22.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                LogoTile()
                Spacer(Modifier.height(14.dp))
                MacText(info.title, size = 28.sp, weight = FontWeight.Bold, shadow = false)
                MacText(info.subtitle, size = 16.sp, color = MacColors.TextSecondary, shadow = false)
                Spacer(Modifier.height(18.dp))

                for ((label, value) in info.rows) {
                    InfoRow(label, value)
                }

                Spacer(Modifier.height(22.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                    MacButton("Закрыть", primary = false, onClick = onClose, modifier = Modifier.focusRequester(closeButton))
                    MacButton("Настройки системы…", primary = true, onClick = onOpenSettings)
                }
            }
        }
    }
}

@Composable
private fun InfoRow(label: String, value: String) {
    Row(Modifier.fillMaxWidth().padding(vertical = 3.dp)) {
        MacText(
            label,
            Modifier.width(250.dp),
            size = 15.sp,
            color = MacColors.TextSecondary,
            align = TextAlign.End,
            maxLines = 2,
            shadow = false,
        )
        Spacer(Modifier.width(14.dp))
        MacText(value, Modifier.weight(1f), size = 15.sp, weight = FontWeight.Medium, maxLines = 2, shadow = false)
    }
}

@Composable
private fun TrafficLight(color: Color, onClick: (() -> Unit)? = null, closeGlyph: Boolean = false) {
    var focused by remember { mutableStateOf(false) }
    val base = Modifier
        .size(14.dp)
        .background(color, CircleShape)
    val modifier = if (onClick != null) {
        base
            .tvPress(onClick)
            .onFocusChanged { focused = it.isFocused }
            .focusable()
            .border(if (focused) 2.dp else 0.dp, Color.White, CircleShape)
    } else base

    Box(modifier, contentAlignment = Alignment.Center) {
        if (closeGlyph && focused) {
            Canvas(Modifier.size(6.dp)) {
                val c = Color(0xB3000000)
                drawLine(c, Offset(0f, 0f), Offset(size.width, size.height), strokeWidth = 1.6f)
                drawLine(c, Offset(size.width, 0f), Offset(0f, size.height), strokeWidth = 1.6f)
            }
        }
    }
}

@Composable
private fun MacButton(
    text: String,
    primary: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var focused by remember { mutableStateOf(false) }
    val shape = RoundedCornerShape(8.dp)
    val bg = when {
        primary -> MacColors.Accent
        else -> Color.White.copy(alpha = 0.16f)
    }
    Box(
        modifier
            .clip(shape)
            .background(bg)
            .tvPress(onClick)
            .onFocusChanged { focused = it.isFocused }
            .focusable()
            .border(if (focused) 3.dp else 1.dp, if (focused) Color.White else Color.White.copy(alpha = 0.18f), shape)
            .padding(horizontal = 22.dp, vertical = 9.dp),
        contentAlignment = Alignment.Center,
    ) {
        MacText(text, size = 17.sp, weight = FontWeight.Medium, shadow = false)
    }
}

/** Крупная плитка с логотипом — такой же, как в строке меню. */
@Composable
private fun LogoTile() {
    Canvas(
        Modifier
            .size(84.dp)
            .clip(RoundedCornerShape(percent = 22))
            .background(Brush.linearGradient(listOf(Color(0xFF5AC8FA), Color(0xFF0A5BFF))))
    ) {
        val pad = size.width * 0.24f
        val gap = size.width * 0.07f
        val cell = (size.width - 2 * pad - gap) / 2f
        for (row in 0..1) {
            for (col in 0..1) {
                drawRoundRect(
                    Color.White.copy(alpha = if ((row + col) % 2 == 0) 0.95f else 0.75f),
                    Offset(pad + col * (cell + gap), pad + row * (cell + gap)),
                    Size(cell, cell),
                    CornerRadius(cell * 0.22f),
                )
            }
        }
    }
}
