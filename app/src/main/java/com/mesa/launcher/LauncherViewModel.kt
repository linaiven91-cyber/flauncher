package com.mesa.launcher

import android.app.Application
import android.widget.Toast
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.mesa.launcher.data.AppEntry
import com.mesa.launcher.data.AppRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class LauncherViewModel(application: Application) : AndroidViewModel(application) {

    private val repo = AppRepository(application)

    private val _apps = MutableStateFlow<List<AppEntry>>(emptyList())
    val apps: StateFlow<List<AppEntry>> = _apps.asStateFlow()

    // null = пользователь ещё не менял Dock (используем набор по умолчанию)
    private val _pinned = MutableStateFlow(repo.loadPinned())

    val dockApps: StateFlow<List<AppEntry>> =
        combine(_apps, _pinned) { apps, pins ->
            val byPackage = apps.associateBy { it.packageName }
            (pins ?: emptyList()).mapNotNull { byPackage[it] }
        }.stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    val pinnedPackages: StateFlow<Set<String>> =
        _pinned.map { (it ?: emptyList()).toSet() }
            .stateIn(viewModelScope, SharingStarted.Eagerly, emptySet())

    private val _launchpadOpen = MutableStateFlow(false)
    val launchpadOpen: StateFlow<Boolean> = _launchpadOpen.asStateFlow()

    private val _aboutOpen = MutableStateFlow(false)
    val aboutOpen: StateFlow<Boolean> = _aboutOpen.asStateFlow()

    private var refreshJob: Job? = null

    fun refresh(debounceMs: Long = 0) {
        refreshJob?.cancel()
        refreshJob = viewModelScope.launch {
            if (debounceMs > 0) delay(debounceMs)
            val list = repo.loadApps()
            _apps.value = list
            if (_pinned.value == null && list.isNotEmpty()) {
                val defaults = repo.defaultPins(list)
                repo.savePinned(defaults)
                _pinned.value = defaults
            }
        }
    }

    fun launch(entry: AppEntry) {
        try {
            getApplication<Application>().startActivity(entry.launchIntent)
            _launchpadOpen.value = false
        } catch (e: Exception) {
            Toast.makeText(getApplication(), "Не удалось открыть ${entry.label}", Toast.LENGTH_SHORT).show()
        }
    }

    fun togglePin(packageName: String) {
        val current = _pinned.value ?: emptyList()
        val next = when {
            packageName in current -> current - packageName
            current.size < AppRepository.MAX_DOCK -> current + packageName
            else -> {
                Toast.makeText(getApplication(), "В Dock помещается не больше ${AppRepository.MAX_DOCK} приложений", Toast.LENGTH_SHORT).show()
                current
            }
        }
        repo.savePinned(next)
        _pinned.value = next
    }

    fun openLaunchpad() { _launchpadOpen.value = true }
    fun closeLaunchpad() { _launchpadOpen.value = false }
    fun openAbout() { _aboutOpen.value = true }
    fun closeAbout() { _aboutOpen.value = false }

    /** Кнопка Home, пока лаунчер уже открыт: закрываем всё поверх рабочего стола. */
    fun onHomePressed() {
        _launchpadOpen.value = false
        _aboutOpen.value = false
    }

    /** Кнопка Back: сначала закрываем окна, потом ничего (лаунчер не закрывается). */
    fun onBackPressed() {
        when {
            _aboutOpen.value -> _aboutOpen.value = false
            _launchpadOpen.value -> _launchpadOpen.value = false
        }
    }
}
