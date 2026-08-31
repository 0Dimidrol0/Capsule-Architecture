# Debug State Time Travel

`capsule-debug` provides bounded state history and an in-app inspector for screens backed by
`BaseCapsuleViewModel`.

## Safety Model

Capsule keeps two state views while debug preview is active:

- `liveState`: the real state owned by the Capsule runtime.
- `state`: the state presented to UI, which can temporarily be a historical snapshot.

Operations and result reducers always continue against `liveState`. Selecting a snapshot cannot
rewrite an in-flight operation or corrupt runtime state. Sending a new intent automatically exits
preview mode and presents the latest live state.

State objects should remain immutable, as expected by Capsule architecture. Snapshots retain state
references and are bounded by `maxSnapshotsPerScreen` to control memory use.

## Setup

Add the module only to debug variants:

```kotlin
dependencies {
    debugImplementation("io.github.0dimidrol0:capsule-debug:0.1.0-SNAPSHOT")
}
```

Create a debug-only Application class in `src/debug`:

```kotlin
class CapsuleDebugApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        CapsuleDebugService.install(
            application = this,
            config = CapsuleDebugConfig(
                maxSnapshotsPerScreen = 50
            )
        )
    }
}
```

Point `src/debug/AndroidManifest.xml` to it:

```xml
<manifest xmlns:android="http://schemas.android.com/apk/res/android">
    <application android:name=".CapsuleDebugApplication" />
</manifest>
```

No screen registration is required. `BaseCapsuleViewModel` connects when its state is first used and
disconnects when `onCleared()` runs.

## Inspector

The floating `C` button is attached inside the resumed Activity. It requires no system overlay
permission. Tap it to open the inspector, or drag it to any free place inside the screen. Its
relative position is retained across Activity changes and orientation changes. The inspector
supports:

- switching between active Capsule screens;
- viewing the latest bounded state snapshots;
- previewing any retained snapshot;
- returning to `LIVE` state;
- clearing one screen's history.

The inspector follows the current system light/dark mode. Its surface, text, controls, state rows,
and dividers use dedicated palettes for both themes.

Override `capsuleDebugName` in a ViewModel to provide a clearer screen name:

```kotlin
override val capsuleDebugName: String = "Login"
```
