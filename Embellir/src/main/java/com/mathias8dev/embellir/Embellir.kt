package com.mathias8dev.embellir

import android.graphics.Bitmap
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.animation.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.ProvidableCompositionLocal
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.layout.boundsInRoot
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.core.view.drawToBitmap
import androidx.palette.graphics.Palette
import kotlinx.coroutines.delay
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlin.coroutines.coroutineContext
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

internal val LocalEmbellirConnect: ProvidableCompositionLocal<EmbellirConnect> =
    compositionLocalOf {
        throw IllegalStateException("EmbellirConnect is not provided")
    }

interface CaptureAndEmbellirScope
class CaptureAndEmbellirScopeImpl : CaptureAndEmbellirScope


interface EmbellirScope {
    val extractedPalette: Palette?
    val extractedVibrantColor: Color?
    val extractedMutedColor: Color?
    val extractedDominantColor: Color?
}

class EmbellirScopeImpl : EmbellirScope {
    var _extractedPalette by mutableStateOf<Palette?>(null)
    override val extractedPalette: Palette?
        get() = _extractedPalette
    override val extractedVibrantColor: Color?
        get() = extractedPalette?.vibrantColor
    override val extractedMutedColor: Color?
        get() = extractedPalette?.mutedColor
    override val extractedDominantColor: Color?
        get() = extractedPalette?.dominantColor

    fun updatePalette(palette: Palette?) {
        _extractedPalette = palette
    }
}

val Palette.vibrantColor: Color?
    get() = getVibrantColor(-1).takeIf { it != -1 }?.let { Color(it) }

val Palette.dominantColor: Color?
    get() = getDominantColor(-1).takeIf { it != -1 }?.let { Color(it) }

val Palette.mutedColor: Color?
    get() = getMutedColor(-1).takeIf { it != -1 }?.let { Color(it) }


interface CaptureScope {
    val capturingViewBounds: Rect?
    val palette: Palette?
}

internal class CaptureScopeImpl : CaptureScope {
    var _capturingViewBounds by mutableStateOf<Rect?>(null)
    override val capturingViewBounds: Rect?
        get() = _capturingViewBounds

    var _palette by mutableStateOf<Palette?>(null)
    override val palette: Palette?
        get() = _palette

    fun updatePalette(palette: Palette) {
        _palette = palette
    }

    fun updateCapturingViewBounds(bounds: Rect) {
        _capturingViewBounds = bounds
    }
}




interface EmbellirConnect {

    open suspend fun publish(palette: Palette)

    suspend fun subscribe(onEvent: (Palette) -> Unit)
}

open class EmbellirConnectImpl : EmbellirConnect {
    private val _events = MutableSharedFlow<Palette>()
    val events = _events.asSharedFlow()

    override suspend fun publish(palette: Palette) {
        _events.emit(palette)
    }

    override suspend fun subscribe(onEvent: (Palette) -> Unit) {
        events.collectLatest { event ->
            coroutineContext.ensureActive()
            onEvent(event)
        }
    }
}


fun Color.toPastel(fraction: Float = 0.5f): Color {
    val red = this.red
    val green = this.green
    val blue = this.blue

    // Calculate pastel color components
    val pastelRed = red + (1 - red) * fraction
    val pastelGreen = green + (1 - green) * fraction
    val pastelBlue = blue + (1 - blue) * fraction

    // Create and return the pastel color
    return Color(pastelRed, pastelGreen, pastelBlue, this.alpha)
}

enum class EmbellirType {
    CIRCULAR,
    LINEAR,
    VERTICAL,
    HORIZONTAL
}

@Composable
fun rememberEmbellirConnect(): EmbellirConnect {
    return remember {
        EmbellirConnectImpl()
    }
}


@Composable
fun rememberCaptureAndEmbellirScope(): CaptureAndEmbellirScope {
    return remember {
        CaptureAndEmbellirScopeImpl()
    }
}


@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun CaptureAndEmbellirScope.Capture(
    isContentAStream: Boolean = true,
    delayBetweenTwoFramesIfStream: Duration = 2.5.seconds,
    content: @Composable CaptureScope.() -> Unit
) {


    val embellirConnect = LocalEmbellirConnect.current
    val ambiantScope = this

    CaptureConnect(
        embellirConnect = embellirConnect,
        isContentAStream = isContentAStream,
        delayBetweenTwoFramesIfStream = delayBetweenTwoFramesIfStream
    ) {
        content()
    }
}


@Composable
fun CaptureAndEmbellirScope.Embellir(
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(0.dp),
    embellir: Boolean = true,
    type: EmbellirType = EmbellirType.VERTICAL,
    extraSize: DpSize = DpSize(200.dp, 200.dp),
    contentAlignment: Alignment = Alignment.Center,
    content: @Composable (EmbellirScope.() -> Unit)
) {

    val embellirConnect = LocalEmbellirConnect.current
    val ambiantScope = this

    Embellir(
        modifier = modifier,
        shape = shape,
        embellir = embellir,
        type = type,
        extraSize = extraSize,
        embellirConnect = embellirConnect,
        contentAlignment = contentAlignment,
    ) {
        content()
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun CaptureAndEmbellir(
    content: @Composable CaptureAndEmbellirScope.() -> Unit
) {
    val scope = rememberCaptureAndEmbellirScope()


    val embellirConnect = rememberEmbellirConnect()

    CompositionLocalProvider(
        LocalEmbellirConnect provides embellirConnect
    ) {
        content(scope)
    }


}



@Composable
fun CaptureConnect(
    embellirConnect: EmbellirConnect,
    isContentAStream: Boolean = true,
    delayBetweenTwoFramesIfStream: Duration = 2.5.seconds,
    content: @Composable CaptureScope.() -> Unit
) {


    var capturingViewBounds by remember { mutableStateOf<Rect?>(null) }

    val localContext = LocalContext.current
    val activity = localContext.findActivity()

    val captureScope = remember {
        CaptureScopeImpl()
    }

    LaunchedEffect(capturingViewBounds) {
        while (true) {
            capturingViewBounds?.let { bounds ->
                captureScope.updateCapturingViewBounds(bounds)
                val drawing = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    activity.window.drawToBitmap(
                        bounds = android.graphics.Rect(
                            bounds.left.toInt(),
                            bounds.top.toInt(),
                            bounds.right.toInt(),
                            bounds.bottom.toInt()
                        ),
                    )
                } else {
                    val decorView = activity.window.decorView.rootView
                    decorView.isDrawingCacheEnabled = false
                    val fullBitmap = decorView.drawToBitmap()

                    // Crop the bitmap to the bounding rect
                    Bitmap.createBitmap(
                        fullBitmap,
                        bounds.left.toInt(),
                        bounds.top.toInt(),
                        (bounds.right - bounds.left).toInt(),
                        (bounds.bottom - bounds.top).toInt()
                    )
                }
                val palette = Palette.Builder(drawing).generate()
                embellirConnect.publish(palette)
                captureScope.updatePalette(palette)

                delay(delayBetweenTwoFramesIfStream)
            }
            if (!isContentAStream) break
        }
    }

    Box(
        modifier = Modifier
            .wrapContentSize()
            .onGloballyPositioned {
                capturingViewBounds = it.boundsInRoot()
            }
    ) {
        content(captureScope)
    }

}



@Composable
fun Embellir(
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(0.dp),
    embellir: Boolean = true,
    type: EmbellirType = EmbellirType.HORIZONTAL,
    extraSize: DpSize = DpSize(200.dp, 200.dp),
    embellirConnect: EmbellirConnect,
    contentAlignment: Alignment = Alignment.Center,
    content: @Composable EmbellirScope.() -> Unit
) {

    val updatedType by rememberUpdatedState(type)
    val updatedExtraSize by rememberUpdatedState(extraSize)
    val vibrantAnimatedColor = remember { Animatable(Color.Transparent) }
    val mutedAnimatedColor = remember { Animatable(Color.Transparent) }
    val dominantAnimatedColor = remember { Animatable(Color.Transparent) }


    val embellirScope = remember {
        EmbellirScopeImpl()
    }

    LaunchedEffect(Unit) {
        val coroutineScope = this
        embellirConnect.subscribe { palette ->

            embellirScope.updatePalette(palette)

            val vibrantColor = palette.vibrantColor ?: Color.Transparent
            val mutedColor = palette.mutedColor ?: Color.Transparent
            val dominantColor = palette.dominantColor ?: Color.Transparent

            coroutineScope.launch {
                vibrantAnimatedColor.animateTo(
                    vibrantColor,
                    animationSpec = tween(
                        durationMillis = 2500
                    )
                )
            }

            coroutineScope.launch {
                mutedAnimatedColor.animateTo(
                    mutedColor,
                    animationSpec = tween(
                        durationMillis = 2500
                    )
                )
            }

            coroutineScope.launch {
                dominantAnimatedColor.animateTo(
                    dominantColor,
                    animationSpec = tween(
                        durationMillis = 2500
                    )
                )
            }
        }
    }

    var contentSize: DpSize by remember { mutableStateOf(DpSize(0.dp, 0.dp)) }
    val localDensity = LocalDensity.current

    Box(
        modifier = modifier
    ) {
        Box(
            modifier = Modifier
                .clip(shape)
                .run {
                    if (embellir) {
                        background(
                            brush = when (updatedType) {
                                EmbellirType.CIRCULAR -> Brush.radialGradient(
                                    colors = listOf(
                                        Color.Transparent,
                                        vibrantAnimatedColor.value,
                                        Color.Transparent
                                    )
                                )

                                EmbellirType.LINEAR -> Brush.linearGradient(
                                    colors = listOf(
                                        Color.Transparent,
                                        dominantAnimatedColor.value,
                                        Color.Transparent
                                    )

                                )

                                EmbellirType.VERTICAL -> Brush.verticalGradient(
                                    colors = listOf(
                                        Color.Transparent,
                                        vibrantAnimatedColor.value,
                                        Color.Transparent
                                    )
                                )

                                EmbellirType.HORIZONTAL -> Brush.horizontalGradient(
                                    colors = listOf(
                                        Color.Transparent,
                                        dominantAnimatedColor.value,
                                        Color.Transparent
                                    )
                                )
                            }
                        )
                    } else {
                        this
                    }
                }
                .sizeIn(
                    minWidth = contentSize.width + updatedExtraSize.width,
                    minHeight = contentSize.height + updatedExtraSize.height,
                ),
            contentAlignment = contentAlignment
        ) {
            Box(
                modifier = Modifier
                    .wrapContentSize()
                    .onGloballyPositioned {
                        with(localDensity) {
                            contentSize = DpSize(it.size.width.toDp(), it.size.height.toDp())
                        }
                    }
            ) {
                content(embellirScope)
            }
        }
    }
}
