package com.example;

import java.time.Duration;
import java.util.List;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.support.ui.*;
import java.util.Scanner;

public class working {

    public static void main(String[] args) throws InterruptedException {

        Scanner sc = new Scanner(System.in);
        String desiredProvider = "";
        boolean providerSelected = false;

        System.out.print("Enter Start Date (Example: 2026-03-13 09:00:00): ");
        String startDateInput = sc.nextLine();

        System.out.print("Enter End Date (Example: 2026-03-13 13:00:00): ");
        String endDateInput = sc.nextLine();

        int startFromIndex = 1;

        WebDriver driver = new ChromeDriver();
        driver.manage().window().maximize();

        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(25));
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

            // Provider dropdown
            WebElement providerDropdown = wait.until(
                    ExpectedConditions.visibilityOfElementLocated(By.id("providerA")));

            Select selectProvider = new Select(providerDropdown);
            List<WebElement> providerOptions = selectProvider.getOptions();

            // Show providers only once
            if (!providerSelected) {

                System.out.println("\nAvailable Providers:");

                for (int p = 0; p < providerOptions.size(); p++) {

                    String pname = providerOptions.get(p).getText().trim();

                    if (!pname.isEmpty()) {
                        System.out.println(p + ". " + pname);
                    }
                }

                int providerChoice = -1;

                while (providerChoice < 0 || providerChoice >= providerOptions.size()) {

                    System.out.print("\nSelect Provider by Number: ");
                    providerChoice = sc.nextInt();
                }

                desiredProvider = providerOptions.get(providerChoice).getText().trim();
                providerSelected = true;

                System.out.println("Selected Provider: " + desiredProvider);
            }

            selectProvider.selectByVisibleText(desiredProvider);

            System.out.println("Provider selected successfully for: " + currencyName);

            WebElement startDate = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("start_dateA")));
            startDate.clear();
            startDate.sendKeys(startDateInput);

            WebElement endDate = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("end_dateA")));
            endDate.clear();
            endDate.sendKeys(endDateInput);

            WebElement remark = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("remarkA")));
            remark.clear();
            remark.sendKeys("provider is on maintenance");

            System.out.println("------- Data filled for currency: " + currencyName);

            System.out.println("Waiting 10 seconds... Please click CANCEL manually...");
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