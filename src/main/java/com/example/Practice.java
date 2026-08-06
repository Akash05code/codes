package com.example;

import java.time.Duration;
import java.util.List;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.support.ui.*;

public class Practice {
    public static void main(String[] args) throws InterruptedException {

        int startFromIndex = 1; //CHANGE THIS VALUE TO START FROM ANY CURRENCY INDEX 
        // THB=1 BDT=2 INR=3 PKR=4 SGD=5 AUD=6 HKD=7 IDR=8 MYR=9 NPR=10 PHP=11 MMK=12 USD=13 VND=14 LAK=15 TWD=16 PNG=17 

        WebDriver driver = new ChromeDriver();
        driver.manage().window().maximize();
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(30));
        JavascriptExecutor js = (JavascriptExecutor) driver;
        driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(5));

        driver.get("https://admin-centerbo.ibstest.site/system-notification");

        driver.findElement(By.id("mer_code")).sendKeys("masterbo");
        driver.findElement(By.id("username")).sendKeys("mbproviderteam");
        driver.findElement(By.id("password")).sendKeys("asdf1234");

        String captcha = driver.findElement(By.xpath("//span[@class='input-group-addon captchaNum']")).getText();
        driver.findElement(By.id("captcha")).sendKeys(captcha);
        driver.findElement(By.id("btnLogin")).click();

        wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath("//span[contains(text(),'System')]")));

        WebElement notification = driver.findElement(By.xpath("//span[contains(text(),'3.1 System Notification')]"));
        js.executeScript("arguments[0].scrollIntoView(true);", notification);
        js.executeScript("arguments[0].click();", notification);

        wait.until(ExpectedConditions.elementToBeClickable(By.id("btnAddNew"))).click();

        WebElement currencyDropdown = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("currencyA")));
        Select selectCurrency = new Select(currencyDropdown);
        List<WebElement> currencies = selectCurrency.getOptions();

        for (int i = 1; i < currencies.size(); i++) {
            if (i < startFromIndex) {
                System.out.println("Skipping currency index: " + i);
                continue;
            }
            String currencyName = currencies.get(i).getText().trim();
            if (currencyName.isEmpty()) {
                currencyName = currencies.get(i).getAttribute("value").trim();
            }
            System.out.println("\n=== Selecting currency: " + currencyName + " ===");

            currencyDropdown = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("currencyA")));
            selectCurrency = new Select(currencyDropdown);
            selectCurrency.selectByIndex(i);

            Thread.sleep(1000);

            WebElement merchantSelect = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("merchantList")));

            js.executeScript("""
                var sel = arguments[0];
                for (var i = 0; i < sel.options.length; i++) {
                    sel.options[i].selected = true;
                }
                sel.dispatchEvent(new Event('change', { bubbles: true }));
            """, merchantSelect);

            System.out.println("----- All merchants selected successfully.-----");
 
            String desiredProvider = "MEGAH5-CARD [MEGAH5-CARD]"; //   
            WebElement providerDropdown = null;     
                                                
                                                  
                                                  
            for (int retry = 0; retry < 5; retry++) { 
                try {
                    providerDropdown = wait.until(ExpectedConditions 
                            .visibilityOfElementLocated(By.id("providerA")));

                    Select selectProvider = new Select(providerDropdown);

                    boolean providerExists = selectProvider.getOptions().stream()
                            .anyMatch(opt -> opt.getText().trim().equals(desiredProvider));

                    if (!providerExists) {
                        System.out.println("Provider '" + desiredProvider + "' NOT AVAILABLE for currency: " + currencyName);
                        System.out.println(" Skipping this currency...");
                        providerDropdown = null;
                        break;
                    }

                    selectProvider.selectByVisibleText(desiredProvider);
                    System.out.println("Provider selected successfully for: " + currencyName);
                    break;

                } catch (StaleElementReferenceException e) {
                    System.out.println("Provider dropdown stale... retrying " + (retry + 1));
                    Thread.sleep(400);
                } catch (Exception e) {
                    System.out.println("Provider not ready... retrying " + (retry + 1));
                    Thread.sleep(400);
                }
            }

            if (providerDropdown == null) {
                continue;
            }

            Select selectProvider = new Select(providerDropdown);
            selectProvider.selectByVisibleText(desiredProvider);

            WebElement startDate = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("start_dateA")));
            startDate.clear();
            startDate.sendKeys("2026-08-07 09:00:00");

            WebElement endDate = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("end_dateA")));
            endDate.clear();
            endDate.sendKeys("2026-08-07 13:00:00");

        
            WebElement remark = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("remarkA")));
            remark.clear();
            remark.sendKeys("provider is on maintenance");

            System.out.println("------- Data filled for currency: " + currencyName);

            System.out.println(" Waiting 10 seconds... Please click CANCEL manually...");
            Thread.sleep(10000);

            wait.until(ExpectedConditions.invisibilityOfElementLocated(
                    By.xpath("//div[@class='modal-dialog']")));

            WebElement addButton = wait.until(ExpectedConditions.elementToBeClickable(By.id("btnAddNew")));
            js.executeScript("arguments[0].click();", addButton);
            System.out.println("----- Reopened Add Notification modal successfully.");
        }

        driver.quit();
    }
}    