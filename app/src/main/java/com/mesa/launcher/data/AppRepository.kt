package com.mesa.launcher.data

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.ResolveInfo
import androidx.compose.ui.graphics.asImageBitmap
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Читает список установленных приложений (TV + обычные)
 * и хранит набор закреплённых в Dock приложений.
 */
class AppRepository(private val context: Context) {

    private val pm = context.packageManager
    private val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)

    suspend fun loadApps(): List<AppEntry> = withContext(Dispatchers.Default) {
        val byPackage = LinkedHashMap<String, AppEntry>()
        // Сначала TV-активности, потом обычные: у leanback-входа приоритет.
        for (category in listOf(Intent.CATEGORY_LEANBACK_LAUNCHER, Intent.CATEGORY_LAUNCHER)) {
            val query = Intent(Intent.ACTION_MAIN).addCategory(category)
            for (info in pm.queryIntentActivities(query, 0)) {
                val pkg = info.activityInfo?.packageName ?: continue
                if (pkg == context.packageName || byPackage.containsKey(pkg)) continue
                val entry = info.toEntry(category) ?: continue
                byPackage[pkg] = entry
            }
        }
        byPackage.values.sortedBy { it.label.lowercase() }
    }

    private fun ResolveInfo.toEntry(category: String): AppEntry? = try {
        val ai = activityInfo
        val intent = Intent(Intent.ACTION_MAIN)
            .addCategory(category)
            .setComponent(ComponentName(ai.packageName, ai.name))
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED)
        AppEntry(
            packageName = ai.packageName,
            label = loadLabel(pm).toString(),
            launchIntent = intent,
            icon = IconRenderer.render(loadIcon(pm), ICON_PX).asImageBitmap(),
        )
    } catch (t: Throwable) {
        null
    }

    /** null = пользователь ещё ничего не закреплял, нужно взять значения по умолчанию. */
    fun loadPinned(): List<String>? =
        prefs.getString(KEY_PINNED, null)?.split(',')?.filter { it.isNotBlank() }

    fun savePinned(packages: List<String>) {
        prefs.edit().putString(KEY_PINNED, packages.joinToString(",")).apply()
    }

    fun defaultPins(apps: List<AppEntry>): List<String> {
        val installed = apps.map { it.packageName }.toSet()
        val preferred = DEFAULT_PACKAGES.filter { it in installed }
        return preferred.ifEmpty { apps.take(6).map { it.packageName } }.take(MAX_DOCK)
    }

    companion object {
        const val MAX_DOCK = 9
        private const val PREFS = "mesa_launcher"
        private const val KEY_PINNED = "pinned_packages"
        private const val ICON_PX = 256

        /** Что закрепить в Dock при первом запуске (берутся только установленные). */
        private val DEFAULT_PACKAGES = listOf(
            "com.google.android.youtube.tv",
            "com.netflix.ninja",
            "com.amazon.amazonvideo.livingroom",
            "com.google.android.youtube.tvmusic",
            "com.spotify.tv.android",
            "org.videolan.vlc",
            "com.android.vending",
        )
    }
}
