package com.mesa.launcher.data

import android.os.Build
import java.text.SimpleDateFormat
import java.util.Locale

/** Данные для окна «Об этом ТВ» — читаются с самого устройства. */
data class DeviceInfo(
    val title: String,
    val subtitle: String,
    val rows: List<Pair<String, String>>,
)

fun readDeviceInfo(): DeviceInfo {
    val release = nonBlank(Build.VERSION.RELEASE)
    val model = Build.MODEL.orEmpty().trim()
    val maker = Build.MANUFACTURER.orEmpty().trim()

    val title = when {
        model.isEmpty() -> "Smart TV"
        maker.isNotEmpty() && !model.startsWith(maker, ignoreCase = true) ->
            maker.replaceFirstChar { it.uppercase() } + " " + model
        else -> model
    }

    return DeviceInfo(
        title = title,
        subtitle = "Android TV $release",
        rows = listOf(
            "Версия ОС Android TV" to release,
            "Исправление системы безопасности" to formatPatch(Build.VERSION.SECURITY_PATCH),
            "Версия ядра" to nonBlank(System.getProperty("os.version")),
            "Сборка ОС Android TV" to nonBlank(Build.DISPLAY),
            "Сведения об изделии" to nonBlank(Build.VERSION.INCREMENTAL),
        ),
    )
}

private fun nonBlank(value: String?): String =
    if (value.isNullOrBlank()) "—" else value.trim()

/** «2024-03-05» → «5 марта 2024 г.» */
private fun formatPatch(raw: String?): String {
    if (raw.isNullOrBlank()) return "—"
    return try {
        val date = SimpleDateFormat("yyyy-MM-dd", Locale.US).parse(raw)
        if (date == null) raw else SimpleDateFormat("d MMMM yyyy 'г.'", Locale("ru", "RU")).format(date)
    } catch (e: Exception) {
        raw
    }
}
