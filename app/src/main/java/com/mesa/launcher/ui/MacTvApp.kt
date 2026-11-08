package com.mesa.launcher.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.mesa.launcher.LauncherViewModel
import kotlinx.coroutines.delay

@Composable
fun MacTvApp(viewModel: LauncherViewModel) {
    val apps by viewModel.apps.collectAsStateWithLifecycle()
    val dockApps by viewModel.dockApps.collectAsStateWithLifecycle()
    val pinned by viewModel.pinnedPackages.collectAsStateWithLifecycle()
    val launchpadOpen by viewModel.launchpadOpen.collectAsStateWithLifecycle()
    val aboutOpen by viewModel.aboutOpen.collectAsStateWithLifecycle()

    // Лаунчер не должен закрываться кнопкой «Назад»
    BackHandler(enabled = true) { viewModel.onBackPressed() }

    BoxWithConstraints(Modifier.fillMaxSize().background(Color.Black)) {
        // Рисуем на виртуальном экране шириной 1280 dp — на любом ТВ (720p / 1080p / 4K)
        // интерфейс получается одинаковых пропорций.
        val widthPx = constraints.maxWidth.toFloat()
        val scaled = remember(widthPx) { Density(density = widthPx / DESIGN_WIDTH_DP, fontScale = 1f) }
        CompositionLocalProvider(LocalDensity provides scaled) {
            Desktop(
                apps = apps,
                dockApps = dockApps,
                pinned = pinned,
                launchpadOpen = launchpadOpen,
                aboutOpen = aboutOpen,
                viewModel = viewModel,
            )
        }
    }
}

@Composable
private fun Desktop(
    apps: List<com.mesa.launcher.data.AppEntry>,
    dockApps: List<com.mesa.launcher.data.AppEntry>,
    pinned: Set<String>,
    launchpadOpen: Boolean,
    aboutOpen: Boolean,
    viewModel: LauncherViewModel,
) {
    val context = LocalContext.current
    val nowMs by rememberNowMillis()
    val dockFocus = remember { FocusRequester() }

    // Фокус на Dock при старте и после закрытия любого окна поверх рабочего стола
    val overlayOpen = launchpadOpen || aboutOpen
    LaunchedEffect(overlayOpen) {
        if (!overlayOpen) {
            delay(300)
            runCatching { dockFocus.requestFocus() }
        }
    }

    Box(Modifier.fillMaxSize()) {
        MacWallpaper()

        MenuBar(
            nowMs = nowMs,
            onAbout = viewModel::openAbout,
            onSettings = { openSystemSettings(context) },
            modifier = Modifier.align(Alignment.TopStart),
        )

        Column(
            Modifier
                .align(Alignment.TopEnd)
                .padding(top = 52.dp, end = 28.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            ClockWidget(nowMs)
            CalendarWidget(nowMs)
        }

        Dock(
            apps = dockApps,
            launchpadFocus = dockFocus,
            onLaunch = viewModel::launch,
            onUnpin = { viewModel.togglePin(it.packageName) },
            onOpenLaunchpad = viewModel::openLaunchpad,
            onOpenSettings = { openSystemSettings(context) },
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 14.dp),
        )

        Launchpad(
            visible = launchpadOpen,
            apps = apps,
            pinned = pinned,
            onLaunch = viewModel::launch,
            onTogglePin = { viewModel.togglePin(it.packageName) },
            modifier = Modifier.fillMaxSize(),
        )

        AboutDialog(
            visible = aboutOpen,
            onClose = viewModel::closeAbout,
            onOpenSettings = {
                viewModel.closeAbout()
                openSystemSettings(context)
            },
            modifier = Modifier.fillMaxSize(),
        )
    }
}
