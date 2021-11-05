# OnlineTest
mobile data on/off test with UI Automator

"svc data enable/disable" turned out to not work (currently) on Pixel2 API 29 virtual device from Firebase Test Lab (while still fine in the same configuration on AVD locally).

That's a try-out of an UI Automator for the sake of education, as well as making sure the disabling/enabling itself is not broken in general, but only in the way it's done through adb.

Test execution results: https://console.firebase.google.com/project/onlinetest-3cf88/testlab/histories/bh.25af3c963ed4eb08/matrices/6780097992713160450
On physical device:
> Pixel 2, Google | Android 10.x, API Level 29 (Q) | English | Portrait
> Skipped triggering the test execution: Incompatible device/OS combination
> Pixel 2, Google | Android 9.x, API Level 28 (Pie) | English | Portrait
> Skipped triggering the test execution: Incompatible device/OS combination
what does that mean? - use Pixel 4

Settings.Panel.ACTION_INTERNET_CONNECTIVITY would be another option, didn't work on API level 30 so far for me
