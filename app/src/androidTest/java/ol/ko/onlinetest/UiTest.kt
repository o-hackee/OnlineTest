package ol.ko.onlinetest

import android.content.Intent
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.uiautomator.By
import androidx.test.uiautomator.UiDevice
import androidx.test.uiautomator.Until
import org.junit.Assert
import org.junit.Test
import org.junit.runner.RunWith
import java.util.regex.Pattern

@RunWith(AndroidJUnit4::class)
class UiTest {
    @Test
    fun uiAutomatorTest() {
        // firebase virtual devices have some unprintable symbols around the target text,
        // e.g. By.text("Off") wouldn't work, By.textContains("Off") seems a bit too loose,
        // hence ending up with regex

        // run on LowRes/Pixel2, API 28/29. LowRes 28 fails since mobile date switch doesn't fit into the first screen

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

        // there is also a "Mobile data off" describing mobile signal icon, check for the view type
        device.findObject(By.descContains("Mobile data").clazz(".Switch")).click()
        Assert.assertNotNull(
            device.wait(
                Until.findObject(By.text(Pattern.compile("[^\\w]*(LTE|4G)[^\\w]*"))),
                2000
            )
        )

        InstrumentationRegistry.getInstrumentation().context.sendBroadcast(Intent(Intent.ACTION_CLOSE_SYSTEM_DIALOGS))
    }
}