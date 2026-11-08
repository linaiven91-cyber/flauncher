package com.mesa.launcher.data

import android.content.Intent
import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.ImageBitmap

/** Одно приложение, которое можно запустить из лаунчера. */
@Immutable
data class AppEntry(
    val packageName: String,
    val label: String,
    val launchIntent: Intent,
    val icon: ImageBitmap,
)
