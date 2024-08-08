package com.mathias8dev.embellir.ui

import android.app.PictureInPictureParams
import android.content.Intent
import android.content.res.Configuration
import android.os.Build
import android.os.Bundle
import android.util.Rational
import androidx.activity.ComponentActivity
import androidx.activity.OnBackPressedCallback
import androidx.activity.compose.setContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.mathias8dev.embellir.R
import com.mathias8dev.embellir.findActivity
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.PlayerConstants
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.YouTubePlayer
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.listeners.YouTubePlayerListener
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.properties.Delegates
import kotlin.time.Duration.Companion.seconds


class YoutubeVideoPlayerActivity : ComponentActivity(), YouTubePlayerListener {

    private lateinit var youtubeVideoId: String
    private var currentViewedSeconds by Delegates.notNull<Float>()
    private var currentSeconds by Delegates.notNull<Float>()
    private var localVideoDuration = -1F

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        youtubeVideoId = intent.getStringExtra(YOUTUBE_VIDEO_ID) ?: ""
        currentSeconds = intent.getFloatExtra(CURRENT_SECONDS, 0f)
        currentViewedSeconds = intent.getFloatExtra(CURRENT_VIEWED_SECONDS, 0f)

        setContent {
            YoutubeVideoPlayerContent()
        }

        onBackPressedDispatcher.addCallback(object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                closeActivity()
            }

        })
    }


    @Composable
    private fun YoutubeVideoPlayerContent() {
        val localContext = LocalContext.current
        val inPipMode = rememberIsInPipMode()
        val coroutineScope = rememberCoroutineScope()
        var showMenuButtons by rememberSaveable {
            mutableStateOf(false)
        }

        var paddingValues by remember {
            mutableStateOf(PaddingValues(top = 50.dp))
        }

        Surface(
            color = Color.Black,
            modifier = Modifier.fillMaxSize()
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .pointerInput(Unit) {
                        awaitEachGesture {
                            awaitFirstDown(false)
                            coroutineScope.launch {
                                if (!inPipMode) {
                                    //paddingValues = PaddingValues(top = 50.dp)
                                    showMenuButtons = true
                                    delay(8.seconds)
                                    showMenuButtons = false
                                }
                            }

                        }
                    },
                contentAlignment = Alignment.Center
            ) {
                YoutubeVideoPlayerComponent(
                    initInFullscreenMode = true,
                    selfHandleFullScreenMode = false,
                    onReady = { youTubePlayer: YouTubePlayer ->
                        youTubePlayer.loadVideo(
                            youtubeVideoId,
                            currentSeconds
                        )
                        youTubePlayer.play()
                        onReady(youTubePlayer)
                    },
                    onCurrentSecond = { player, seconds ->
                        onCurrentSecond(player, seconds)
                        currentSeconds = seconds
                        currentViewedSeconds += 0.1f
                    },
                    onExitFullscreen = {
                        closeActivity()
                    },
                    onApiChange = {
                        onApiChange(it)
                    },
                    onError = { player, error ->
                        onError(player, error)
                    },
                    onPlaybackQualityChange = { player, quality ->
                        onPlaybackQualityChange(player, quality)
                    },
                    onPlaybackRateChange = { player, rate ->
                        onPlaybackRateChange(player, rate)
                    },
                    onStateChange = { youTubePlayer, state ->
                        onStateChange(youTubePlayer, state)
                    },
                    onVideoDuration = { youTubePlayer, duration ->
                        onVideoDuration(youTubePlayer, duration)
                    },
                    onVideoId = { youTubePlayer, videoId ->
                        onVideoId(youTubePlayer, videoId)
                    },
                    onVideoLoadedFraction = { youTubePlayer, loadedFraction ->
                        onVideoLoadedFraction(youTubePlayer, loadedFraction)
                    }
                )

                AnimatedVisibility (
                    visible = !inPipMode && showMenuButtons,
                    modifier = Modifier.align(Alignment.TopCenter)
                ) {
                    Row(
                        modifier = Modifier
                            .padding(paddingValues)
                            .fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        IconButton(
                            onClick = {
                                closeActivity()
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = null,
                                tint = Color.White
                            )
                        }

                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                            IconButton(
                                onClick = {
                                    localContext.findActivity().enterPictureInPictureMode(
                                        PictureInPictureParams.Builder()
                                            .setAspectRatio(Rational(16, 9))
                                            .build()
                                    )
                                }
                            ) {
                                Icon(
                                    painter = painterResource(id = R.drawable.ic_pip),
                                    contentDescription = null,
                                    tint = Color.White
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    private fun closeActivity() {
        val data = Intent().apply {
            putExtra(CURRENT_SECONDS, currentSeconds)
            putExtra(CURRENT_VIEWED_SECONDS, currentViewedSeconds)
            onSetExtraResultExtra(this)
        }
        setResult(ENDED_SUCCESSFULLY, data)
        finish()
    }


    override fun onPictureInPictureModeChanged(
        isInPictureInPictureMode: Boolean,
        configuration: Configuration
    ) {
        if (lifecycle.currentState == androidx.lifecycle.Lifecycle.State.CREATED) {
            closeActivity()
        }
        super.onPictureInPictureModeChanged(isInPictureInPictureMode)
    }

    protected open fun onSetExtraResultExtra(intent: Intent) {}

    override fun onApiChange(youTubePlayer: YouTubePlayer) {}

    override fun onCurrentSecond(youTubePlayer: YouTubePlayer, second: Float) {
    }

    override fun onError(youTubePlayer: YouTubePlayer, error: PlayerConstants.PlayerError) {}

    override fun onPlaybackQualityChange(
        youTubePlayer: YouTubePlayer,
        playbackQuality: PlayerConstants.PlaybackQuality
    ) {
    }

    override fun onPlaybackRateChange(
        youTubePlayer: YouTubePlayer,
        playbackRate: PlayerConstants.PlaybackRate
    ) {
    }

    override fun onReady(youTubePlayer: YouTubePlayer) {}

    override fun onStateChange(youTubePlayer: YouTubePlayer, state: PlayerConstants.PlayerState) {}

    override fun onVideoDuration(youTubePlayer: YouTubePlayer, duration: Float) {
        localVideoDuration = duration
    }

    override fun onVideoId(youTubePlayer: YouTubePlayer, videoId: String) {}

    override fun onVideoLoadedFraction(youTubePlayer: YouTubePlayer, loadedFraction: Float) {}


    companion object {
        const val ENDED_SUCCESSFULLY = 1
        const val CURRENT_SECONDS = "currentSeconds"
        const val CURRENT_VIEWED_SECONDS = "currentViewedSeconds"
        const val YOUTUBE_VIDEO_ID = "youtubeVideoId"
    }

}