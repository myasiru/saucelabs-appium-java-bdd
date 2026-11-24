package com.saucelabs.debug;

import io.appium.java_client.android.AndroidDriver;
import io.appium.java_client.android.options.UiAutomator2Options;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.List;

/**
 * Debug utility test to dump page source XML.
 * Run with: mvn test -Dtest=PageSourceDumper -pl .
 */
public class PageSourceDumper {

    private static AndroidDriver driver;

    @BeforeAll
    static void setup() throws Exception {
        UiAutomator2Options options = new UiAutomator2Options()
                .setDeviceName("emulator-5554")
                .setApp("C:\\Users\\Yasir\\Desktop\\VS_Code_Projects\\saucelabs-appium-java-bdd\\apps\\SauceLabsApp.apk")
                .setAppPackage("com.saucelabs.mydemoapp.android")
                .setAppActivity("com.saucelabs.mydemoapp.android.view.activities.SplashActivity")
                .setAutomationName("UiAutomator2")
                .setNewCommandTimeout(Duration.ofSeconds(60))
                .setAppWaitActivity("*")
                .setNoReset(false);

        driver = new AndroidDriver(new URL("http://127.0.0.1:4723"), options);
        Thread.sleep(3000);
    }

    @AfterAll
    static void tearDown() {
        if (driver != null) driver.quit();
    }

    @Test
    void dumpCatalogAndDetailPageSource() throws Exception {
        String basePath = "C:\\Users\\Yasir\\Desktop\\VS_Code_Projects\\saucelabs-appium-java-bdd\\";

        // 1) Dump catalog page
        String catalogSource = driver.getPageSource();
        Files.writeString(Path.of(basePath + "catalog_page_source.xml"), catalogSource);
        System.out.println(">>> CATALOG PAGE SOURCE SAVED <<<");

        // 2) Tap on first product title
        List<WebElement> titles = driver.findElements(By.xpath("//*[@resource-id='com.saucelabs.mydemoapp.android:id/titleTV']"));
        System.out.println(">>> Found " + titles.size() + " product titles <<<");

        if (!titles.isEmpty()) {
            titles.get(0).click();
            Thread.sleep(3000);

            // 3) Dump product detail page
            String detailSource = driver.getPageSource();
            Files.writeString(Path.of(basePath + "detail_page_source.xml"), detailSource);
            System.out.println(">>> DETAIL PAGE SOURCE SAVED <<<");
        }
    }
}
