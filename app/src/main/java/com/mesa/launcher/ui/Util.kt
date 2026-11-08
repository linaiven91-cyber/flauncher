package com.mesa.launcher.ui

import android.content.Context
import android.content.Intent
import android.provider.Settings
import android.widget.Toast
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.input.pointer.pointerInput
import kotlinx.coroutines.delay

private val confirmKeys = setOf(Key.DirectionCenter, Key.Enter, Key.NumPadEnter)

/**
 * Нажатие кнопкой OK на пульте (и тап для мыши/air-mouse).
 * Удержание OK вызывает [onLongClick].
 * Ставить ПЕРЕД .onFocusChanged() и .focusable().
 */
fun Modifier.tvPress(
    onClick: () -> Unit,
    onLongClick: (() -> Unit)? = null,
): Modifier = composed {
    var longFired by remember { mutableStateOf(false) }
    val click by rememberUpdatedState(onClick)
    val long by rememberUpdatedState(onLongClick)

    this
        .onPreviewKeyEvent { event ->
            if (event.key !in confirmKeys) return@onPreviewKeyEvent false
            when (event.type) {
                KeyEventType.KeyDown -> {
                    if (long != null && event.nativeKeyEvent.repeatCount > 0 && !longFired) {
                        longFired = true
                        long?.invoke()
                    }
                    true
                }
                KeyEventType.KeyUp -> {
                    if (!longFired) click()
                    longFired = false
                    true
                }
                else -> false
            }
        }
        .pointerInput(Unit) {
            detectTapGestures(
                onLongPress = { long?.invoke() },
                onTap = { click() },
            )
        }
}

/** Текущее время в мс, обновляется раз в минуту (по границе минуты). */
@Composable
fun rememberNowMillis(): State<Long> = produceState(System.currentTimeMillis()) {
    while (true) {
        value = System.currentTimeMillis()
        delay(60_000L - value % 60_000L + 50L)
    }
}

fun openSystemSettings(context: Context) {
    try {
        context.startActivity(
            Intent(Settings.ACTION_SETTINGS).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        )
    } catch (e: Exception) {
        Toast.makeText(context, "Не удалось открыть настройки", Toast.LENGTH_SHORT).show()
    }
}
