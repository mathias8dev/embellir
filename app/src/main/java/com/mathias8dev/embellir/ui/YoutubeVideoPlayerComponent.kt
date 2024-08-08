package com.mathias8dev.embellir.ui

import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.os.Build
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.core.app.PictureInPictureModeChangedInfo
import androidx.core.util.Consumer
import com.mathias8dev.embellir.findActivity
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.PlayerConstants
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.YouTubePlayer
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.listeners.AbstractYouTubePlayerListener
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.listeners.FullscreenListener
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.options.IFramePlayerOptions
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.views.YouTubePlayerView
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun YoutubeVideoPlayerComponent(
    modifier: Modifier = Modifier,
    playerHeight: Int = 130,
    youtubeId: String? = null,
    selfHandleFullScreenMode: Boolean = true,
    initInFullscreenMode: Boolean = false,
    onApiChange: ((youTubePlayer: YouTubePlayer) -> Unit)? = null,
    onCurrentSecond: ((youTubePlayer: YouTubePlayer, second: Float) -> Unit)? = null,
    onError: ((youTubePlayer: YouTubePlayer, error: PlayerConstants.PlayerError) -> Unit)? = null,
    onPlaybackQualityChange: ((
        youTubePlayer: YouTubePlayer,
        playbackQuality: PlayerConstants.PlaybackQuality
    ) -> Unit)? = null,
    onPlaybackRateChange: ((
        youTubePlayer: YouTubePlayer,
        playbackRate: PlayerConstants.PlaybackRate
    ) -> Unit)? = null,
    onReady: ((youTubePlayer: YouTubePlayer) -> Unit)? = null,
    onStateChange: ((youTubePlayer: YouTubePlayer, state: PlayerConstants.PlayerState) -> Unit)? = null,
    onVideoDuration: ((youTubePlayer: YouTubePlayer, duration: Float) -> Unit)? = null,
    onVideoId: ((youTubePlayer: YouTubePlayer, videoId: String) -> Unit)? = null,
    onVideoLoadedFraction: ((
        youTubePlayer: YouTubePlayer,
        loadedFraction: Float
    ) -> Unit)? = null,
    onEnterFullscreen: ((viewedSeconds: Float) -> Unit)? = null,
    onExitFullscreen: ((viewedSeconds: Float) -> Unit)? = null,
    onPlayerAvailable: ((youTubePlayer: YouTubePlayer) -> Unit)? = null,
) {
    val localContext = LocalContext.current
    val fullscreenViewContainer = remember {
        val layout = LinearLayout(localContext)
        layout.layoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT
        )
        layout.orientation = LinearLayout.VERTICAL
        layout.visibility = LinearLayout.VISIBLE
        layout.gravity = Gravity.CENTER
        layout
    }

    val inFullscreenMode = rememberSaveable {
        mutableStateOf(false)
    }

    var localYoutubePlayer: YouTubePlayer? by remember {
        mutableStateOf(null)
    }

    val currentViewedSeconds = rememberSaveable {
        mutableFloatStateOf(0F)
    }


    val fullScreenListenerCallback: (playerView: YouTubePlayerView) -> FullscreenListener =
        remember {
            { playerView ->
                object : FullscreenListener {
                    override fun onEnterFullscreen(
                        fullscreenView: View, exitFullscreen: () -> Unit
                    ) {
                        onEnterFullscreen?.invoke(currentViewedSeconds.floatValue)
                        fullscreenViewContainer.addView(fullscreenView)
                        fullscreenView.layoutParams = LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.MATCH_PARENT,
                            LinearLayout.LayoutParams.MATCH_PARENT,
                        )
                        fullscreenViewContainer.visibility = View.VISIBLE
                        inFullscreenMode.value = true
                        playerView.visibility = View.GONE
                    }

                    override fun onExitFullscreen() {
                        onExitFullscreen?.invoke(currentViewedSeconds.floatValue)
                        fullscreenViewContainer.visibility = View.GONE
                        playerView.visibility = View.VISIBLE
                        fullscreenViewContainer.removeAllViews()
                        inFullscreenMode.value = false
                    }
                }
            }
        }


    val playerListener = remember {
        object : AbstractYouTubePlayerListener() {
            override fun onApiChange(youTubePlayer: YouTubePlayer) {
                onApiChange?.invoke(youTubePlayer)
                localYoutubePlayer = youTubePlayer
            }

            override fun onCurrentSecond(youTubePlayer: YouTubePlayer, second: Float) {
                onCurrentSecond?.invoke(youTubePlayer, second)
                currentViewedSeconds.value = second
                localYoutubePlayer = youTubePlayer
            }

            override fun onError(youTubePlayer: YouTubePlayer, error: PlayerConstants.PlayerError) {
                onError?.invoke(youTubePlayer, error)
                localYoutubePlayer = youTubePlayer
            }

            override fun onPlaybackQualityChange(
                youTubePlayer: YouTubePlayer,
                playbackQuality: PlayerConstants.PlaybackQuality
            ) {
                onPlaybackQualityChange?.invoke(youTubePlayer, playbackQuality)
                localYoutubePlayer = youTubePlayer
            }

            override fun onPlaybackRateChange(
                youTubePlayer: YouTubePlayer,
                playbackRate: PlayerConstants.PlaybackRate
            ) {
                onPlaybackRateChange?.invoke(youTubePlayer, playbackRate)
                localYoutubePlayer = youTubePlayer
            }

            override fun onReady(youTubePlayer: YouTubePlayer) {
                onPlayerAvailable?.invoke(youTubePlayer)
                onReady?.invoke(youTubePlayer)
                localYoutubePlayer = youTubePlayer
                if (initInFullscreenMode && !inFullscreenMode.value) {
                    youTubePlayer.toggleFullscreen()
                    inFullscreenMode.value = true
                }
            }

            override fun onStateChange(
                youTubePlayer: YouTubePlayer,
                state: PlayerConstants.PlayerState
            ) {
                onStateChange?.invoke(youTubePlayer, state)
                localYoutubePlayer = youTubePlayer
            }

            override fun onVideoDuration(youTubePlayer: YouTubePlayer, duration: Float) {
                onVideoDuration?.invoke(youTubePlayer, duration)
                localYoutubePlayer = youTubePlayer
            }

            override fun onVideoId(youTubePlayer: YouTubePlayer, videoId: String) {
                onVideoId?.invoke(youTubePlayer, videoId)
                localYoutubePlayer = youTubePlayer
            }

            override fun onVideoLoadedFraction(
                youTubePlayer: YouTubePlayer,
                loadedFraction: Float
            ) {
                onVideoLoadedFraction?.invoke(youTubePlayer, loadedFraction)
                localYoutubePlayer = youTubePlayer
            }

        }
    }

    val coroutineScope = rememberCoroutineScope()

    val fullScreenVideoLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) {
        //localYoutubePlayer?.play()
        coroutineScope.launch {
            delay(300)
            inFullscreenMode.value = false
            localYoutubePlayer?.toggleFullscreen()
            it.data?.let { resultIntent ->
                resultIntent.getFloatExtra(
                    YoutubeVideoPlayerActivity.CURRENT_SECONDS,
                    0f
                ).let { viewedSeconds ->
                    localYoutubePlayer?.seekTo(viewedSeconds)
                    localYoutubePlayer?.play()
                }
            }
        }
    }


    val youtubePlayerView = rememberYoutubeVideoPlayer(
        playerListener,
        fullScreenListenerCallback
    )




    Box(modifier = modifier) {
        AndroidView(
            modifier = modifier,
            factory = { _ ->
                youtubePlayerView.apply {
                    layoutParams = ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        playerHeight
                    )
                }
            },
            update = { view ->
                view.apply {
                    visibility = if (inFullscreenMode.value) {
                        View.GONE
                    } else {
                        View.VISIBLE
                    }
                }
            }
        )

    }


    if (inFullscreenMode.value) {
        AndroidView(
            factory = { _ ->
                fullscreenViewContainer
            }, update = { view ->
                view
            }
        )
        if (selfHandleFullScreenMode) {
            LaunchedEffect(Unit) {
                fullScreenVideoLauncher.launch(
                    Intent(
                        localContext,
                        YoutubeVideoPlayerActivity::class.java
                    ).apply {
                        putExtra(
                            YoutubeVideoPlayerActivity.YOUTUBE_VIDEO_ID,
                            youtubeId ?: ""
                        )
                        putExtra(
                            YoutubeVideoPlayerActivity.CURRENT_SECONDS,
                            currentViewedSeconds.floatValue
                        )
                    }
                )
            }
        }
    }
}

@Composable
fun rememberIsInPipMode(): Boolean {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        val activity = LocalContext.current.findActivity()
        var pipMode by remember { mutableStateOf(activity.isInPictureInPictureMode) }
        DisposableEffect(activity) {
            val observer = Consumer<PictureInPictureModeChangedInfo> { info ->
                pipMode = info.isInPictureInPictureMode
            }
            activity.addOnPictureInPictureModeChangedListener(
                observer
            )
            onDispose { activity.removeOnPictureInPictureModeChangedListener(observer) }
        }
        return pipMode
    } else {
        return false
    }
}

@Composable
fun rememberYoutubeVideoPlayer(
    listener: AbstractYouTubePlayerListener? = null,
    fullscreenListenerCallback: ((playerView: YouTubePlayerView) -> FullscreenListener)? = null
): YouTubePlayerView {
    val localContext = LocalContext.current
    val activity = localContext as ComponentActivity

    val playerView: YouTubePlayerView = remember {
        val iFramePlayerOptions = IFramePlayerOptions.Builder()
            .controls(1)
            .fullscreen(1)
            .modestBranding(1)
            .build()

        YouTubePlayerView(localContext).apply {
            enableAutomaticInitialization = false
            listener?.let {
                initialize(it, iFramePlayerOptions)
            }
            fullscreenListenerCallback?.let {
                val fullscreenListener = it.invoke(this)
                addFullscreenListener(fullscreenListener)
            }
            activity.lifecycle.addObserver(this)
        }
    }

    DisposableEffect(playerView) {
        onDispose {
            activity.lifecycle.removeObserver(playerView)
        }
    }
    return playerView
}


