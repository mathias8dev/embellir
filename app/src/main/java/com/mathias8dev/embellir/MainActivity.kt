package com.mathias8dev.embellir

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.mathias8dev.embellir.ui.YoutubeVideoPlayerComponent
import com.mathias8dev.embellir.ui.theme.EmbellirTheme
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.YouTubePlayer

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            EmbellirTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    Box(
                        modifier = Modifier.padding(0.dp)
                            .offset(y = (-20).dp)
                    ) {
                        EmbellirTestScreen()
                    }
                }
            }
        }
    }
}


@Composable
fun EmbellirTestScreen() {

    var localYoutubePlayer: YouTubePlayer? = remember { null }
    val coroutineScope = rememberCoroutineScope()
    val embellirConnect = rememberEmbellirConnect()

    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        Embellir(
            type = EmbellirType.VERTICAL,
            embellirConnect = embellirConnect
        ) {

            CaptureConnect(
                embellirConnect = embellirConnect,
                isContentAStream = true,
            ) {

                YoutubeVideoPlayerComponent(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp),
                    onReady = { youTubePlayer: YouTubePlayer ->
                        localYoutubePlayer = youTubePlayer
                        youTubePlayer.loadVideo(
                            videoId = "47dtFZ8CFo8",
                            startSeconds = 0F
                        )
                        youTubePlayer.play()
                    }
                )
            }
        }
    }
}