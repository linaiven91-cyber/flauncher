package com.mesa.launcher.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.focusGroup
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusProperties
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mesa.launcher.data.AppEntry
import kotlinx.coroutines.delay

/** Полноэкранная сетка всех приложений (аналог Launchpad). */
@Composable
fun Launchpad(
    visible: Boolean,
    apps: List<AppEntry>,
    pinned: Set<String>,
    onLaunch: (AppEntry) -> Unit,
    onTogglePin: (AppEntry) -> Unit,
    modifier: Modifier = Modifier,
) {
    AnimatedVisibility(
        visible = visible,
        modifier = modifier,
        enter = fadeIn(tween(220)) + scaleIn(initialScale = 1.06f, animationSpec = tween(260)),
        exit = fadeOut(tween(160)) + scaleOut(targetScale = 1.06f, animationSpec = tween(200)),
    ) {
        LaunchpadContent(apps, pinned, onLaunch, onTogglePin)
    }
}

@Composable
private fun LaunchpadContent(
    apps: List<AppEntry>,
    pinned: Set<String>,
    onLaunch: (AppEntry) -> Unit,
    onTogglePin: (AppEntry) -> Unit,
) {
    val firstItem = remember { FocusRequester() }
    LaunchedEffect(Unit) {
        delay(150)
        runCatching { firstItem.requestFocus() }
    }

    Box(
        Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(Color(0xE6120F2E), Color(0xF20A0818))))
            // Фокус не должен «утекать» на рабочий стол под полупрозрачным фоном
            .focusProperties { exit = { FocusRequester.Cancel } }
            .focusGroup()
    ) {
        Column(Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally) {
            Spacer(Modifier.height(30.dp))
            Row(
                Modifier
                    .glass(RoundedCornerShape(50), strength = 0.9f)
                    .padding(horizontal = 24.dp, vertical = 8.dp)
            ) {
                MacText("Приложения · ${apps.size}", size = 18.sp, weight = FontWeight.Medium)
            }

            if (apps.isEmpty()) {
                Box(Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                    MacText("Приложения загружаются…", size = 18.sp, color = MacColors.TextSecondary)
                }
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(7),
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    contentPadding = PaddingValues(horizontal = 72.dp, vertical = 24.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    itemsIndexed(apps, key = { _, app -> app.packageName }) { index, app ->
                        LaunchpadTile(
                            app = app,
                            pinned = app.packageName in pinned,
                            onClick = { onLaunch(app) },
                            onLongClick = { onTogglePin(app) },
                            modifier = if (index == 0) Modifier.focusRequester(firstItem) else Modifier,
                        )
                    }
                }
            }

            MacText(
                "OK — открыть     •     удерживайте OK — закрепить в Dock / убрать     •     Назад — закрыть",
                Modifier.padding(bottom = 16.dp),
                size = 15.sp,
                color = MacColors.TextSecondary,
            )
        }
    }
}

@Composable
private fun LaunchpadTile(
    app: AppEntry,
    pinned: Boolean,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var focused by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (focused) 1.12f else 1f,
        animationSpec = spring(dampingRatio = 0.7f, stiffness = 500f),
        label = "tile-scale",
    )

    Column(
        modifier
            .fillMaxWidth()
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .clip(RoundedCornerShape(18.dp))
            .background(if (focused) Color.White.copy(alpha = 0.20f) else Color.Transparent)
            .tvPress(onClick, onLongClick)
            .onFocusChanged { focused = it.isFocused }
            .focusable()
            .padding(horizontal = 6.dp, vertical = 10.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Image(
            bitmap = app.icon,
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .size(84.dp)
                .shadow(5.dp, RoundedCornerShape(percent = 22)),
        )
        Spacer(Modifier.height(8.dp))
        MacText(
            app.label,
            Modifier.fillMaxWidth(),
            size = 16.sp,
            align = TextAlign.Center,
        )
        Spacer(Modifier.height(4.dp))
        Box(
            Modifier
                .size(5.dp)
                .background(if (pinned) Color.White.copy(alpha = 0.85f) else Color.Transparent, CircleShape)
        )
    }
}
