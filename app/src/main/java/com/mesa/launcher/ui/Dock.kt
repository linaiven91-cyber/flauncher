package com.mesa.launcher.ui

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mesa.launcher.data.AppEntry
import kotlin.math.abs

private val DockBase = 68.dp
private val DockGlassHeight = 92.dp

private sealed interface DockItem {
    object Launchpad : DockItem
    data class App(val entry: AppEntry) : DockItem
    object Separator : DockItem
    object Settings : DockItem
}

/** Размер иконки в зависимости от расстояния до иконки в фокусе (эффект увеличения macOS). */
private fun magnifiedSize(index: Int, focusedIndex: Int): Dp {
    if (focusedIndex < 0) return DockBase
    return when (abs(index - focusedIndex)) {
        0 -> 112.dp
        1 -> 88.dp
        2 -> 74.dp
        else -> DockBase
    }
}

/**
 * Dock внизу экрана. Иконка в фокусе увеличивается, соседние — чуть меньше,
 * панель-«стекло» при этом плавно расширяется. Удержание OK на приложении — убрать из Dock.
 */
@Composable
fun Dock(
    apps: List<AppEntry>,
    launchpadFocus: FocusRequester,
    onLaunch: (AppEntry) -> Unit,
    onUnpin: (AppEntry) -> Unit,
    onOpenLaunchpad: () -> Unit,
    onOpenSettings: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val items = remember(apps) {
        buildList<DockItem> {
            add(DockItem.Launchpad)
            apps.forEach { add(DockItem.App(it)) }
            add(DockItem.Separator)
            add(DockItem.Settings)
        }
    }
    var focusedIndex by remember { mutableIntStateOf(-1) }

    fun onFocusChange(index: Int, focused: Boolean) {
        if (focused) focusedIndex = index else if (focusedIndex == index) focusedIndex = -1
    }

    Box(modifier.fillMaxWidth(), contentAlignment = Alignment.BottomCenter) {
        Box(Modifier.height(DockGlassHeight)) {
            // Стеклянная панель фиксированной высоты, увеличенные иконки выступают над ней
            Box(
                Modifier
                    .matchParentSize()
                    .glass(RoundedCornerShape(28.dp), strength = 0.9f)
            )
            Row(
                Modifier
                    .align(Alignment.BottomCenter)
                    .padding(start = 16.dp, end = 16.dp, bottom = 12.dp)
                    .wrapContentHeight(align = Alignment.Bottom, unbounded = true),
                verticalAlignment = Alignment.Bottom,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                items.forEachIndexed { index, item ->
                    val size by animateDpAsState(
                        targetValue = if (item is DockItem.Separator) DockBase else magnifiedSize(index, focusedIndex),
                        animationSpec = spring(dampingRatio = 0.72f, stiffness = 520f),
                        label = "dock-size",
                    )
                    when (item) {
                        is DockItem.Separator -> Box(
                            Modifier
                                .padding(horizontal = 4.dp)
                                .width(1.dp)
                                .height(DockBase * 0.8f)
                                .background(Color.White.copy(alpha = 0.28f))
                        )

                        is DockItem.Launchpad -> DockSlot(
                            size = size,
                            focused = focusedIndex == index,
                            label = "Все приложения",
                            onFocus = { onFocusChange(index, it) },
                            onClick = onOpenLaunchpad,
                            onLongClick = null,
                            modifier = Modifier.focusRequester(launchpadFocus),
                        ) { LaunchpadGlyphTile() }

                        is DockItem.App -> DockSlot(
                            size = size,
                            focused = focusedIndex == index,
                            label = item.entry.label,
                            onFocus = { onFocusChange(index, it) },
                            onClick = { onLaunch(item.entry) },
                            onLongClick = { onUnpin(item.entry) },
                        ) { AppTile(item.entry.icon) }

                        is DockItem.Settings -> DockSlot(
                            size = size,
                            focused = focusedIndex == index,
                            label = "Настройки",
                            onFocus = { onFocusChange(index, it) },
                            onClick = onOpenSettings,
                            onLongClick = null,
                        ) { SettingsGlyphTile() }
                    }
                }
            }
        }
    }
}

@Composable
private fun DockSlot(
    size: Dp,
    focused: Boolean,
    label: String,
    onFocus: (Boolean) -> Unit,
    onClick: () -> Unit,
    onLongClick: (() -> Unit)?,
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit,
) {
    Box(
        modifier
            .size(size)
            .tvPress(onClick, onLongClick)
            .onFocusChanged { onFocus(it.isFocused) }
            .focusable()
    ) {
        content()
        if (focused) {
            DockLabel(
                label,
                Modifier
                    .align(Alignment.TopCenter)
                    .offset(y = (-38).dp)
            )
        }
    }
}

@Composable
private fun DockLabel(text: String, modifier: Modifier = Modifier) {
    Box(
        modifier
            .wrapContentSize(unbounded = true)
            .glass(RoundedCornerShape(9.dp), strength = 1.1f)
            .padding(horizontal = 12.dp, vertical = 5.dp)
    ) {
        MacText(text, size = 16.sp, weight = FontWeight.Medium)
    }
}

@Composable
private fun AppTile(icon: ImageBitmap) {
    // IconRenderer отдаёт непрозрачный квадрат — здесь он обрезается «скруглённым квадратом» с тенью.
    Image(
        bitmap = icon,
        contentDescription = null,
        contentScale = ContentScale.Crop,
        modifier = Modifier
            .fillMaxSize()
            .shadow(6.dp, RoundedCornerShape(percent = 22)),
    )
}

/** Иконка «Все приложения»: сетка из 9 цветных квадратов. */
@Composable
private fun LaunchpadGlyphTile() {
    Canvas(
        Modifier
            .fillMaxSize()
            .shadow(6.dp, RoundedCornerShape(percent = 22))
            .background(Brush.verticalGradient(listOf(Color(0xFF3F455C), Color(0xFF1C2033))))
    ) {
        val colors = listOf(
            Color(0xFFFF5F57), Color(0xFFFF9F0A), Color(0xFFFEDB3B),
            Color(0xFF32D74B), Color(0xFF40D6C9), Color(0xFF0A84FF),
            Color(0xFF5E5CE6), Color(0xFFBF5AF2), Color(0xFFFF5E9C),
        )
        val pad = size.width * 0.20f
        val cell = (size.width - 2 * pad) / 3f
        for (row in 0..2) {
            for (col in 0..2) {
                drawRoundRect(
                    colors[row * 3 + col],
                    Offset(pad + col * cell + cell * 0.10f, pad + row * cell + cell * 0.10f),
                    Size(cell * 0.80f, cell * 0.80f),
                    CornerRadius(cell * 0.22f),
                )
            }
        }
    }
}

/** Иконка «Настройки»: шестерёнка на серой плитке. */
@Composable
private fun SettingsGlyphTile() {
    Canvas(
        Modifier
            .fillMaxSize()
            .shadow(6.dp, RoundedCornerShape(percent = 22))
            .background(Brush.verticalGradient(listOf(Color(0xFFA3A9B8), Color(0xFF5F6575))))
    ) {
        val gear = Color(0xFFF3F4F6)
        val r = size.minDimension * 0.25f
        val toothW = r * 0.50f
        val toothH = r * 0.60f
        for (i in 0 until 8) {
            rotate(degrees = i * 45f, pivot = center) {
                drawRoundRect(
                    gear,
                    Offset(center.x - toothW / 2f, center.y - r - toothH * 0.70f),
                    Size(toothW, toothH * 1.4f),
                    CornerRadius(toothW * 0.25f),
                )
            }
        }
        drawCircle(gear, r, center)
        drawCircle(Color(0xFF5F6575), r * 0.45f, center)
    }
}
