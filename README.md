# Embellir
> embellir is a jetpack compose library that help to extract colors on any components and apply it to another one. It is a simple way to make your app more beautiful and consistent.
> For instance, with embellir, you can reproduce Youtube video player color diffusion effect



<br><br>
[![Jitpack latest version](https://jitpack.io/v/mathias8dev/embellir.svg)](https://jitpack.io/#mathias8dev/embellir)
[![Code Style](https://img.shields.io/badge/code%20style-%E2%9D%A4-FF4081.svg)](https://ktlint.github.io)
[![Apache License](https://img.shields.io/badge/license-Apache%20License%202.0-blue.svg)](LICENSE)


## Setup
### 1. Import JitPack Android Library
Add `maven { url 'https://jitpack.io' }` in
<details open>
  <summary>groovy - settings.gradle</summary>

```gradle
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()

        maven { url 'https://jitpack.io' }
    }
}
```
</details>

<details open>
  <summary>kotlin - settings.gradle.kts</summary>

```gradle
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()

        maven ("https://jitpack.io")
    }
}
```
</details>

### 2. Add dependency
<details open>
  <summary>groovy - build.gradle</summary>

```gradle
dependencies {
    implementation "com.github.mathias8dev:embellir:latest-version"
}
```
</details>
<details open>
  <summary>kotlin - build.gradle.kts</summary>

```gradle
dependencies {
    implementation("com.github.mathias8dev:embellir:latest-version")
}
```
</details>



## Getting Started


### Fast way to learn how to use Embellir
<p>
    The best way to learn how to use Embellir is to look at the code in this repo I use to reproduce the Youtube video player color diffusion effect. <br>
</p>

##### Youtube video player color diffusion effect



https://github.com/user-attachments/assets/906ab075-21c7-4cd4-8c37-8cc2851c84c1




### Embellir
To reproduce the example shown in the video above, you have many options. One is to use the composable Embellir. <br>
There is two versions of this composable. The first inherit CaptureAndEmbellirScope(and therefore should be called in this scope). It takes a content composable behind it will diffuse the extracted colors. <br>
The extracted colors are received from embellirConnect and exposed to the content composable through EmbellirScope. <br>
You can then use these colors to apply them to another composable. <br>. If its parameter embellir is set to true, it will diffuse the extracted colors to the content composable. <br>
The type parameter is used to specify the direction of the diffusion. <br>
The extraSize parameter is used to specify the extra size the diffusion will propagate to. <br>
    
```kotlin

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
)


interface EmbellirScope {
    val extractedPalette: Palette?
    val extractedVibrantColor: Color?
    val extractedMutedColor: Color?
    val extractedDominantColor: Color?
}
```

### CaptureAndEmbellirScope

```kotlin
interface CaptureAndEmbellirScope
```

<p>
    It is just an empty interface. Actually, two composables inherit this scope: Capture and Embellir. <br>
</p>

# Capture

<p>
    Its purpose is to capture the colors of a composable and expose them through embellirConnect. <br>
</p>

```kotlin
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
```

### CaptureConnect

```kotlin
@Composable
fun CaptureConnect(
    embellirConnect: EmbellirConnect,
    isContentAStream: Boolean = true,
    delayBetweenTwoFramesIfStream: Duration = 2.5.seconds,
    content: @Composable CaptureScope.() -> Unit
)
```

<p>
    The purpose of this composable is to extract the colors and expose them through embellirConnect. <br>
    The isContentAStream parameter is used to specify if the content is a stream or not. <br>
    The delayBetweenTwoFramesIfStream parameter is used to specify the delay between two frames if the content is a stream. <br>
    The existence of this composable permit to use capture without CaptureAndEmbellirScope. But the drawback is that you will have to pass the embellirConnect as a parameter. <br>
</p>


### CaptureScope

```kotlin
interface CaptureScope {
    val capturingViewBounds: Rect?
    val palette: Palette?
}
```

<p>
    The CaptureScope interface is used to expose the colors and the bounds of the captured view. <br>
</p>

### EmbellirConnect

```kotlin
interface EmbellirConnect {
    suspend fun publish(palette: Palette)
    suspend fun subscribe(onEvent: (Palette) -> Unit)
}
```

<p>
    The EmbellirConnect interface is used to publish and subscribe to the extracted colors. <br>
    At high level it is a simple way to communicate between Capture and Embellir. <br>
    It is a communication bus.
</p>


### Embellir

```kotlin
@Composable
fun CaptureAndEmbellirScope.Embellir(
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(0.dp),
    embellir: Boolean = true,
    type: EmbellirType = EmbellirType.VERTICAL,
    extraSize: DpSize = DpSize(200.dp, 200.dp),
    contentAlignment: Alignment = Alignment.Center,
    content: @Composable (EmbellirScope.() -> Unit)
)
```

<p>
    This is the version of Embellir that inherit CaptureAndEmbellirScope. <br>
    It is used to apply the extracted colors to another composable and should be called in the CaptureAndEmbellirScope. <br>
</p>

### CaptureAndEmbellir

```kotlin
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
```

<p>
    This composable is used to create the CaptureAndEmbellirScope. <br>
    It is the entry point of the CaptureAndEmbellirScope. <br>
</p>


### EmbellirType

```kotlin

enum class EmbellirType {
    CIRCULAR,
    LINEAR,
    VERTICAL,
    HORIZONTAL
}
```

<p>
    The EmbellirType enum is used to specify the way the diffusion is rendered. Feel free to try. <br>
</p>

## License

This project is licensed under the Apache License, Version 2.0 - see the [LICENSE](LICENSE) file for details.
