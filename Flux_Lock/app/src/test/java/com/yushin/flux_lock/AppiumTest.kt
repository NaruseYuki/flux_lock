package com.yushin.flux_lock

import io.appium.java_client.android.AndroidDriver
import io.appium.java_client.remote.AndroidMobileCapabilityType
import io.appium.java_client.remote.MobileCapabilityType
import io.appium.java_client.remote.MobilePlatform
import org.junit.jupiter.api.*
import org.junit.jupiter.api.Assertions.assertEquals
import org.openqa.selenium.By
import org.openqa.selenium.interactions.Actions
import org.openqa.selenium.remote.DesiredCapabilities
import java.io.File
import java.net.MalformedURLException
import java.net.URL

class AndroidTest {
    private var driver: AndroidDriver? = null

    @BeforeEach
    @Throws(MalformedURLException::class)
    fun setUp() {
        // Please inject these env vars when you run these tests
        val apkFilePath = System.getenv("ANDROID_APK_FILE_PATH") // Relative path to the APK file
        // String apkFilePath = "binary/android/app-release.apk";
        val packageName =
            System.getenv("ANDROID_PACKAGE_NAME") // App package name described in AndroidManifest.xml
        // String packageName = "com.example.webviewlinksample";
        val launchActivityName =
            System.getenv("ANDROID_LAUNCH_ACTIVITY_NAME") // Activity name where App is launched

        // String launchActivityName = ".MainActivity";
        val app = apkFilePath?.let { File(it) }

        // Ref: https://appium.io/docs/en/writing-running-appium/caps/
        //
        // When you run the tests on Android Emulator,
        // you need to make sure there is a running emulator whose OS is the same as "appium:platformVersion"
        // $ $ANDROID_HOME/platform-tools/adb devices -l
        val desiredCapabilities = DesiredCapabilities()
        desiredCapabilities.setCapability(MobileCapabilityType.AUTOMATION_NAME, "UiAutomator2")
        desiredCapabilities.setCapability(
            MobileCapabilityType.PLATFORM_NAME,
            MobilePlatform.ANDROID
        )
        desiredCapabilities.setCapability(MobileCapabilityType.PLATFORM_VERSION, "12")
        desiredCapabilities.setCapability(MobileCapabilityType.DEVICE_NAME, "Android Emulator")
        if (app != null) {
            desiredCapabilities.setCapability(MobileCapabilityType.APP, app.absolutePath)
        }
        desiredCapabilities.setCapability(AndroidMobileCapabilityType.APP_PACKAGE, packageName)
        desiredCapabilities.setCapability(
            AndroidMobileCapabilityType.APP_ACTIVITY,
            launchActivityName
        )

        driver = AndroidDriver(URL("http://0.0.0.0:4723/wd/hub"), desiredCapabilities)
    }

    @AfterEach
    fun tearDown() {
        if (driver != null) {
            driver!!.quit()
        }
    }

    @Test
    @DisplayName("UI_TEST1")
    fun uiTest1() {
//        val expected = "WebViewLink Sample"
//
//        // Fetch the title of the toolbar
//        // Note that androidx.appcompat.widget.Toolbar implicitly creates an element of android.widget.TextView as a child
//        val toolbar = driver!!.findElement(By.id("toolbar"))
//            .findElement(By.className("android.widget.TextView"))
//        val actual = toolbar.text
//
        assertEquals("", "")
    }

    @Test
    @DisplayName("UI_TEST2")
    fun uiTest2() {
        // Link text area
//        val linkText = driver!!.findElement(By.id("link_text"))
//
//        // Verify no texts are input in the below text area
//        val expectedBeforeInput = ""
//        val actualBeforeInput = linkText.text
//
//        assertEquals(expectedBeforeInput, actualBeforeInput)
//
//        // Search button in the SearchView
//        val searchButton = driver!!.findElement(By.id("searchbar"))
//            .findElement(By.className("android.widget.ImageView"))
//
//        // Tap the search button to get its focus
//        searchButton.click()
//
//        // Type some characters in the search bar
//        val action = Actions(driver)
//        action.sendKeys("https://google.com").perform()
//
//        // Verify the text it inputted to the search bar is really shown in the below text area
//        val expectedAfterInput = "https://google.com"
//        val actualAfterInput = linkText.text
//
        assertEquals("","")

        // Tap the generated link
        //linkText.click()
    }
}