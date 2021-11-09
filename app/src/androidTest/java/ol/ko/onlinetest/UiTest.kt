package ol.ko.onlinetest

import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.os.Build
import android.os.SystemClock
import android.provider.Settings
import android.util.Log
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.uiautomator.By
import androidx.test.uiautomator.UiDevice
import androidx.test.uiautomator.Until
import org.junit.Assert
import org.junit.Test
import org.junit.runner.RunWith
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL
import java.util.regex.Pattern

@RunWith(AndroidJUnit4::class)
class UiTest {
    companion object {
        private val TAG = "OLKO"
    }

    @Test
    fun uiAutomatorTest() {
        // firebase virtual devices have some unprintable symbols around the target text,
        // e.g. By.text("Off") wouldn't work, By.textContains("Off") seems a bit too loose,
        // hence ending up with regex

        // run on LowRes/Pixel2, API 28/29. LowRes 28 fails since mobile date switch doesn't fit into the first screen
        assert(isConnected(InstrumentationRegistry.getInstrumentation().context))

        val device = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())
        assert(device.openQuickSettings())
        val mobileDataSwitch = device.wait(Until.findObject(By.descContains("Mobile data")), 5000)
        Assert.assertNotNull(mobileDataSwitch)

        mobileDataSwitch.click()
        val pattern = Pattern.compile("(?i)[^\\w]*(Turn off)[^\\w]*")
        val turnOff = device.wait(Until.findObject(By.text(pattern)), 5000)
        if (turnOff != null) { // shown for the first time only
            turnOff.click()
            assert(device.openQuickSettings())
        }

        device.wait(Until.findObject(By.descContains("Mobile data")), 5000)
        Assert.assertNotNull(
            device.wait(
                Until.findObject(By.text(Pattern.compile("[^\\w]*Off[^\\w]*"))),
                2000
            )
        )
        assert(!isConnected(InstrumentationRegistry.getInstrumentation().context))

        // there is also a "Mobile data off" describing mobile signal icon, check for the view type
        device.findObject(By.descContains("Mobile data").clazz(".Switch")).click()
        Assert.assertNotNull(
            device.wait(
                Until.findObject(By.text(Pattern.compile("[^\\w]*(LTE|4G)[^\\w]*"))),
                2000
            )
        )
        InstrumentationRegistry.getInstrumentation().context.sendBroadcast(Intent(Intent.ACTION_CLOSE_SYSTEM_DIALOGS))
        SystemClock.sleep(1000)

        // re-open
        assert(device.openQuickSettings())
        Assert.assertNotNull(device.wait(Until.findObject(By.descContains("Mobile data")), 5000))
        Assert.assertNotNull(
            device.wait(
                Until.findObject(By.text(Pattern.compile("[^\\w]*(LTE|4G)[^\\w]*"))),
                2000
            )
        )
        InstrumentationRegistry.getInstrumentation().context.sendBroadcast(Intent(Intent.ACTION_CLOSE_SYSTEM_DIALOGS))

        var connected = false
        for (i in 0 until 15) {
            connected = isConnected(InstrumentationRegistry.getInstrumentation().context)
            if (!connected) {
                SystemClock.sleep(1000)
            }
        }
        assert(connected)
    }

    @Test
    fun settingsPanelTest() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q)
            Log.i(TAG, "Skip test")

        val context = InstrumentationRegistry.getInstrumentation().context
        assert(isConnected(context))

        val panelIntent = Intent(Settings.Panel.ACTION_INTERNET_CONNECTIVITY).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(panelIntent)

        val device = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())

        val mobileDataLabel = device.wait(Until.findObject(By.text(pattern("Mobile\\sdata"))), 5000)
        Assert.assertNotNull(mobileDataLabel)
        val mobileDataParent = mobileDataLabel.parent.parent

        val mobileDataSwitch = mobileDataParent?.findObject(By.text(pattern("ON")).clazz(".Switch"))
        Assert.assertNotNull(mobileDataSwitch)
        mobileDataSwitch?.click()

        SystemClock.sleep(1000)
        assert(!isConnected(context))

        val potentiallyNewMobileDataLabel = device.wait(Until.findObject(By.text(pattern("Mobile\\sdata"))), 5000)
        Assert.assertNotNull(potentiallyNewMobileDataLabel)
        val potentiallyNewMobileDataParent = potentiallyNewMobileDataLabel.parent.parent

        val potentiallyNewMobileDataSwitch = potentiallyNewMobileDataParent?.findObject(By.text(pattern("OFF")).clazz(".Switch"))
        Assert.assertNotNull(potentiallyNewMobileDataSwitch)
        potentiallyNewMobileDataSwitch?.click()

        var connected = false
        for (i in 0 until 15) {
            connected = isConnected(InstrumentationRegistry.getInstrumentation().context)
            if (!connected) {
                SystemClock.sleep(1000)
            }
        }
        assert(connected)
    }

    private fun pattern(string: String) = Pattern.compile("[^\\w]*$string[^\\w]*")

    private fun isConnected(context: Context): Boolean {
        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val activeNetwork = cm.activeNetworkInfo
        return if (activeNetwork?.isConnected == true) {
            try {
                val url = URL("https://clients3.google.com/generate_204")
                val connection: HttpURLConnection = (url.openConnection() as HttpURLConnection).apply {
                    setRequestProperty("User-Agent", "Android")
                    setRequestProperty("Connection", "close")
                    useCaches = false
                    connectTimeout = 1000 // mTimeout is in seconds
                    connect()
                }
                connection.responseCode == 204 && connection.contentLength == 0
            } catch (e: IOException) {
                Log.i(TAG, "Error checking internet connection", e)
                false
            }
        } else {
            Log.i(TAG, "No network")
            false
        }
    }
}