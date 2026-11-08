package com.mesa.launcher.data

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.Shader
import android.graphics.drawable.AdaptiveIconDrawable
import android.graphics.drawable.Drawable
import android.os.Build
import kotlin.math.min
import kotlin.math.roundToInt

/**
 * Приводит иконки приложений к единому виду: непрозрачный квадрат,
 * который интерфейс затем обрезает «скруглённым квадратом» как в macOS.
 *
 *  - adaptive-иконки рисуются слоями без системной маски (иначе круглая маска
 *    устройства давала бы круги внутри скруглённых квадратов);
 *  - «старые» иконки с прозрачными полями кладутся на однотонную подложку.
 */
object IconRenderer {

    fun render(source: Drawable, size: Int): Bitmap {
        val icon = draw(source, size)
        return if (needsPlate(icon)) onPlate(icon) else icon
    }

    private fun draw(source: Drawable, size: Int): Bitmap {
        val bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && source is AdaptiveIconDrawable) {
            // Слои adaptive-иконки — 108x108dp, видна центральная область 72x72dp.
            val inset = (size * 0.25f).roundToInt()
            val layers = listOf(source.background, source.foreground)
            for (layer in layers) {
                if (layer == null) continue
                layer.setBounds(-inset, -inset, size + inset, size + inset)
                layer.draw(canvas)
            }
            return bitmap
        }

        val iw = if (source.intrinsicWidth > 0) source.intrinsicWidth else size
        val ih = if (source.intrinsicHeight > 0) source.intrinsicHeight else size
        val scale = min(size.toFloat() / iw, size.toFloat() / ih)
        val w = (iw * scale).roundToInt()
        val h = (ih * scale).roundToInt()
        val left = (size - w) / 2
        val top = (size - h) / 2
        source.setBounds(left, top, left + w, top + h)
        source.draw(canvas)
        return bitmap
    }

    /** Углы иконки прозрачные → это не «полноразмерная» иконка, нужна подложка. */
    private fun needsPlate(bitmap: Bitmap): Boolean {
        val last = bitmap.width - 1
        val m = (bitmap.width * 0.08f).roundToInt()
        val points = listOf(
            Pair(m, m),
            Pair(last - m, m),
            Pair(m, last - m),
            Pair(last - m, last - m),
        )
        for (p in points) {
            if (Color.alpha(bitmap.getPixel(p.first, p.second)) < 250) return true
        }
        return false
    }

    private fun onPlate(icon: Bitmap): Bitmap {
        val size = icon.width
        val out = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(out)

        val light = isLight(icon)
        val top = if (light) 0xFF4A5062.toInt() else 0xFFFFFFFF.toInt()
        val bottom = if (light) 0xFF262A36.toInt() else 0xFFD8DBE2.toInt()
        val plate = Paint(Paint.ANTI_ALIAS_FLAG)
        plate.shader = LinearGradient(0f, 0f, 0f, size.toFloat(), top, bottom, Shader.TileMode.CLAMP)
        canvas.drawRect(0f, 0f, size.toFloat(), size.toFloat(), plate)

        val inner = (size * 0.78f).roundToInt()
        val offset = (size - inner) / 2
        val paint = Paint(Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG)
        canvas.drawBitmap(icon, null, Rect(offset, offset, offset + inner, offset + inner), paint)
        return out
    }

    /** Светлая иконка (например, белый логотип) — подложка нужна тёмная, и наоборот. */
    private fun isLight(bitmap: Bitmap): Boolean {
        val pixels = IntArray(bitmap.width * bitmap.height)
        bitmap.getPixels(pixels, 0, bitmap.width, 0, 0, bitmap.width, bitmap.height)
        var sum = 0.0
        var count = 0
        for (p in pixels) {
            if (Color.alpha(p) < 128) continue
            sum += 0.299 * Color.red(p) + 0.587 * Color.green(p) + 0.114 * Color.blue(p)
            count++
        }
        return count > 0 && sum / count > 190.0
    }
}
