package com.example;

import java.time.Duration;
import java.util.List;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;

public class GameNames {

    public static void main(String[] args) throws InterruptedException {

        int startFromIndex = 1; //CHANGE THIS VALUE TO START FROM ANY CURRENCY INDEX 
        // THB=1 BDT=2 INR=3 PKR=4 SGD=5 AUD=6 HKD=7 MYR=8 NPR=9 PHP=10 MMK=11 IDR=12 USD=13 VND=14 LAK=15 TWD=16 PNG=17 

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
    }}}