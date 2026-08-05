package com.example;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.support.ui.*;

public class Comparison {
    public static void main(String[] args) {
        // Initialize ChromeDriver
        WebDriver driver = new ChromeDriver();
        try {
            driver.manage().window().maximize();
            driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(5));
            WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(25));
            JavascriptExecutor js = (JavascriptExecutor) driver;
 
            driver.get("https://www.mt88my.com/en-my/instantwin");  

            // Optional: wait for slot category to load
            wait.until(ExpectedConditions.visibilityOfElementLocated(
                    By.xpath("//div[contains(@class,'tab_btn_bg relative overflow-hidden')]"))); //game-type-text text-xs text-center pt-3 uppercase truncate //tab_btn_text

            // Scroll to make all providers visible (if lazy loading)
            js.executeScript("window.scrollTo(0, document.body.scrollHeight);");
            Thread.sleep(2000); // small pause for loading

            // Fetch all providers under slot category
            List<WebElement> providerElements = driver.findElements(
                    By.xpath("//div[@class='tab_btn_bg relative overflow-hidden']")); //tab_btn_text text-center text-xs mt-2 uppercase w-[50px] truncate

            // Store names in a list
            List<String> providerNames = new ArrayList<>();
            for (WebElement provider : providerElements) {
                String name = provider.getText().trim();
                if (!name.isEmpty()) {
                    providerNames.add(name);
                }
            }

            // Sort names alphabetically
            Collections.sort(providerNames);

            // Print total count
            System.out.println("Total providers: " + providerNames.size());

            // Print provider names with serial numbers
            int count = 1;
            for (String name : providerNames) {
                System.out.println(count + ". " + name);
                count++;
            }

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            driver.quit();
        }
    }
}
