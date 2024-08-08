package com.mathias8dev.embellir

import android.content.Context
import android.content.ContextWrapper
import android.graphics.Bitmap
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.view.PixelCopy
import android.view.Window
import androidx.activity.ComponentActivity
import androidx.annotation.RequiresApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

fun Context.findActivity(): ComponentActivity {
    var context = this
    while (context is ContextWrapper) {
        if (context is ComponentActivity) return context
        context = context.baseContext
    }
    throw IllegalStateException("Picture in picture should be called in the context of an Activity")
}


@RequiresApi(Build.VERSION_CODES.O)
suspend fun Window.drawToBitmap(
    config: Bitmap.Config = Bitmap.Config.ARGB_8888,
    bounds: android.graphics.Rect,
    timeoutInMs: Long = 1000
): Bitmap {
    var result = PixelCopy.ERROR_UNKNOWN
    val latch = CountDownLatch(1)

    val bitmap =
        Bitmap.createBitmap(bounds.width(), bounds.height(), config)
    PixelCopy.request(this, bounds, bitmap, { copyResult ->
        result = copyResult
        latch.countDown()
    }, Handler(Looper.getMainLooper()))

    var timeout = false
    withContext(Dispatchers.IO) {
        runCatching {
            timeout = !latch.await(timeoutInMs, TimeUnit.MILLISECONDS)
        }
    }

    if (timeout) error("Failed waiting for PixelCopy")
    if (result != PixelCopy.SUCCESS) error("Non success result: $result")

    return bitmap
}