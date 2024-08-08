### kotlin-yup
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


## License

This project is licensed under the Apache License, Version 2.0 - see the [LICENSE](LICENSE) file for details.