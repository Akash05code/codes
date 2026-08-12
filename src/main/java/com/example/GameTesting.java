package com.example;

import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.support.ui.*;

import java.time.Duration;
import java.util.List;
import java.util.Set;

public class GameTesting {

    public static void main(String[] args) throws InterruptedException {

        WebDriver driver = new ChromeDriver();
        driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(5));
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(15));
        JavascriptExecutor jse = (JavascriptExecutor) driver;

        driver.manage().window().maximize();

        driver.get("https://member-npr96.ibstest.site/ne-np/slot");
        Thread.sleep(20000);

        List<WebElement> providers = driver.findElements(
                By.xpath("//div[contains(@class,'mt-5 flex items-center slot_btn_container w-full overflow-auto light-scrollbar-h pb-[10px]')]//button[@aria-label]")
        );

        for (int p = 34; p < providers.size(); p++) {   //change the provider accordingly

            providers = driver.findElements(
                    By.xpath("//div[contains(@class,'mt-5 flex items-center slot_btn_container w-full overflow-auto light-scrollbar-h pb-[10px]')]//button[@aria-label]")
            );

            WebElement provider = providers.get(p);

            String providerName = provider.getAttribute("aria-label");

            try {
                provider.click();
            } catch (Exception e) {
                jse.executeScript("arguments[0].click();", provider);
            }

            System.out.println(" Provider selected: " + providerName);

            Thread.sleep(2000);

            List<WebElement> playBtns = driver.findElements(
                    By.xpath("//button[@aria-label='Play Now']")
            );

            int count = Math.min(3, playBtns.size());   //change the number of games to be checked...

            for (int i = 0; i < count; i++) {

                playBtns = driver.findElements(
                        By.xpath("//button[@aria-label='Play Now']")
                );

                WebElement playBtn = playBtns.get(i);

                String mainWindow = driver.getWindowHandle();
                int beforeClickWindows = driver.getWindowHandles().size();

                try {
                    playBtn.click();
                } catch (Exception e) {
                    jse.executeScript("arguments[0].click();", playBtn);
                }

                System.out.println(" Clicked Play button " + (i + 1));

                Thread.sleep(8000);

                List<WebElement> errorPopup = driver.findElements(
                        By.xpath("//*[contains(text(),'Something went wrong')]")
                );

                if (!errorPopup.isEmpty()) {
                    System.out.println(" Error popup detected");

                    List<WebElement> okBtn = driver.findElements(
                            By.xpath("//button[contains(text(),'okay') or contains(text(),'okay')]")
                    );

                    if (!okBtn.isEmpty()) {
                        okBtn.get(0).click();
                    }

                    continue;
                }
                Set<String> windows = driver.getWindowHandles();

                if (windows.size() > beforeClickWindows) {

                    for (String win : windows) {
                        if (!win.equals(mainWindow)) {
                            driver.switchTo().window(win);
                            break;
                        }
                    }

                    System.out.println("New tab game opened");

                    Thread.sleep(5000);

                    driver.close();
                    driver.switchTo().window(mainWindow);

                } else {
                    System.out.println(" Same tab game opened");

                    Thread.sleep(5000);

                    driver.navigate().back();
                }

                Thread.sleep(3000);
            }
        }

        driver.quit();
    }
}  