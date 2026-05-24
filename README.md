# Ringside Interval Timer

Ringside is a high-performance, offline-first boxing and combat sports interval timer built for Android. Explicitly designed with latency-free execution and high-visibility interfaces in mind, Ringside thrives in rigorous training environments—from your local gym to completely disconnected setups in the Arctic.

## Features

- **Extreme Offline Reliability**: Built with Room Database. Ringside requires absolutely zero internet connection for its core features. It works wherever you are.
- **Precision Timing Engine**: Uses high-precision Coroutine-based timing bound to hardware clocks (`System.nanoTime()`) for zero-drift interval progression, accurate down to the millisecond.
- **Combat Sports Profiles**: Comes pre-loaded with configurations for:
  - Classic Boxing
  - Mixed Martial Arts (MMA)
  - Brazilian Jiu-Jitsu (BJJ)
  - Collegiate Wrestling
  - High-Intensity Interval Training (HIIT)
- **Material 3 Expressive Design**: Huge, tabular typography, high-contrast colors, and dynamic circular progress mapping designed to be easily readable through sweat from across the room.
- **Haptic & Audio Feedback**: Low-latency `ToneGenerator` audio signals and precise `Vibrator` patterns ensure you know exactly when a round starts, when the rest ends, and when your warning period hits. 

## Architectural Overview

Ringside follows Clean Architecture principles focused on a lightweight and deterministic state machine:

- **UI Layer (Jetpack Compose)**: `TimerScreen` operates entirely on unidirectional data flow powered by StateFlows. Extensively uses custom `Canvas` drawing for a highly performant circular progress indicator instead of heavy nested layouts.
- **Presentation Layer (ViewModel)**: `TimerViewModel` acts as the state machine bounding the execution cycle (Prep -> Work -> Rest -> Finished). Updates run decoupled from the UI thread using Kotlin Couroutines.
- **Data Layer (Room DB)**: Local SQLite wrapper providing robust persistence of user configurations via `WorkoutProfileDao`. The database is heavily optimized for zero-overhead initializations.

## Developer Setup

### Prerequisites
- Android Studio Ladybug or newer
- JDK 17+
- Android SDK 36

### Building from Source
1. Clone the repository: `git clone https://github.com/your-username/ringside-timer.git`
2. Open the project in Android Studio.
3. Gradle sync will execute automatically.
4. Run the `:app:assembleDebug` task or simply press "Run" to deploy to an emulator or physical device.

## Technologies Used
- **Kotlin 2.0+**
- **Jetpack Compose** (Material 3)
- **Kotlin Coroutines / Flow**
- **AndroidX Room**
- **AndroidX Lifecycle & ViewModel**

## License
MIT License. See `LICENSE` for details.
