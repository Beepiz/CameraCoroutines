# CameraCoroutines

WIP full featured coroutines-based library for Camera2 Android API.

The documentation will be completed later in the future by the author or by you. 

Any discussion about this can be started in the issues.

## How to use the sample

1. Run the `app` module on a device that has at least one rear camera.
2. Click on the first button and grant the camera permission.
3. Click on the second button and grant the microphone permission.
4. Click on the last button to run a test that will attempt to record 6 seconds of video.

Notice the toasts are advertising the start and the completion of the recording, as well as
any cancellation/disconnection/error caused by things like the `Activity` being left or another
higher priority app like the device Camera app being launched.

All error cases should be handled correctly without crashing. Open an issue if this is not the case.

To see the recorded video, browse to the
`Android/data/com.beepiz.cameracoroutines.sample/files/ExtensionsApproachVideoRecord.mp4` directory
in the internal public storage (from Android Studio, you can find it under `/sdcard` in the Device
File Explorer tool window while the device is plugged to computer with adb enabled and authorized).
