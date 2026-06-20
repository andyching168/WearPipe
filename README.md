# WearPipe

WearPipe is a standalone YouTube search and playback app designed for Wear OS 3 and newer.
It supports video and background audio playback without a phone, a persistent queue, playback
history, search history, rotary input, Bluetooth media controls, and swipe-to-dismiss navigation.

## Build

Requirements:

- JDK 21
- Android SDK 36
- A Wear OS 3+ emulator or watch

```shell
./gradlew :app:assembleDebug
```

The APK is generated at `app/build/outputs/apk/debug/app-debug.apk`.

## Architecture

- Wear Compose Material 3 and Navigation 3 for the watch UI
- NewPipe Extractor behind `StreamingRepository`
- Media3 `MediaSessionService` for foreground and background playback
- Room for the queue, playback progress, and search history

The original mobile, desktop, and iOS source trees are retained temporarily as migration
references but are excluded from the Gradle project and from all WearPipe artifacts.

## License and attribution

WearPipe is licensed under GPL-3.0-or-later. It is based on NewPipe and uses
[NewPipe Extractor](https://github.com/TeamNewPipe/NewPipeExtractor). WearPipe is an independent
fork and is not an official Team NewPipe release.
