package com.example;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.support.ui.*;

public class HotGames {

    // Normalize for comparison (ignore spaces + case)
    public static String normalize(String text) {
        return text.replaceAll("\\s+", "").toLowerCase().trim();
    }

    // Convert input to UI-searchable format (JumpHigh → Jump High)
    public static String makeSearchable(String text) {
        return text.replaceAll("([a-z])([A-Z])", "$1 $2").trim();
    }

    // Select provider from custom dropdown
    public static void selectProviderFromDropdown(WebDriver driver, WebDriverWait wait, String providerName) throws InterruptedException {
        JavascriptExecutor js = (JavascriptExecutor) driver;

        // Click provider dropdown ("All Providers")
       By providerDropdown = By.xpath(
    "//div[contains(@class,'o-input-wrapper') and contains(@class,'o-select-container')]"
);
        WebElement dropdown = wait.until(ExpectedConditions.elementToBeClickable(providerDropdown));
        js.executeScript("arguments[0].scrollIntoView({block:'center'});", dropdown);
        Thread.sleep(500);

        try {
            dropdown.click();
        } catch (Exception e) {
            js.executeScript("arguments[0].click();", dropdown);
        }

        // Wait for dropdown popup
        By dropdownPopup = By.xpath("//div[contains(@class,'o-select-dropdown')]");
        wait.until(ExpectedConditions.visibilityOfElementLocated(dropdownPopup));

        // Click provider option using data-slug
        By providerOption = By.xpath("//div[contains(@class,'o-select-dropdown')]//span[@data-slug='" + providerName + "']");

        WebElement option = wait.until(ExpectedConditions.visibilityOfElementLocated(providerOption));
        js.executeScript("arguments[0].scrollIntoView({block:'center'});", option);
        Thread.sleep(500);

        try {
            option.click();
        } catch (Exception e) {
            js.executeScript("arguments[0].click();", option);
        }

        System.out.println("Selected provider: " + providerName);
                 Thread.sleep(10000); // Wait for provider selection to take effect
    // Read all game names from Excel column A
    }

    public static List<String> readGameNamesFromExcel(String filePath) throws Exception {
        List<String> gameNames = new ArrayList<>();

        FileInputStream fis = new FileInputStream(filePath);
        Workbook workbook = new XSSFWorkbook(fis);
        Sheet sheet = workbook.getSheetAt(0);

        for (int i = 1; i <= sheet.getLastRowNum(); i++) {   // start from row 2, skip header
            Row row = sheet.getRow(i);
            if (row == null) {
                gameNames.add("");
                continue;
            }

            Cell cell = row.getCell(0); // Column A
            if (cell == null) {
                gameNames.add("");
                continue;
            }

            cell.setCellType(CellType.STRING);
            String gameName = cell.getStringCellValue().trim();
            gameNames.add(gameName);
        }

        workbook.close();
        fis.close();

        return gameNames;
    }

    // Write status into column B
    public static void writeStatusToExcel(String filePath, int rowNumber, String status) throws Exception {
        FileInputStream fis = new FileInputStream(filePath);
        Workbook workbook = new XSSFWorkbook(fis);
        Sheet sheet = workbook.getSheetAt(0);

        Row row = sheet.getRow(rowNumber);
        if (row == null) {
            row = sheet.createRow(rowNumber);
        }

        Cell statusCell = row.getCell(1); // Column B
        if (statusCell == null) {
            statusCell = row.createCell(1);
        }

        statusCell.setCellValue(status);

        fis.close();

        FileOutputStream fos = new FileOutputStream(filePath);
        workbook.write(fos);
        workbook.close();
        fos.close();
    }

    public static void main(String[] args) throws Exception {

        //Excel file path
                String excelPath = "C:\\Users\\AKASHKUMAR\\Desktop\\Allgames.xlsx";

        WebDriver driver = new ChromeDriver();
        driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(5));
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(15));
        driver.manage().window().maximize();

        try {
            driver.get("https://centralized-bo.ibscbo.com/en-us");

            driver.findElement(By.xpath("//input[@placeholder='Group Code']")).sendKeys("super");
            driver.findElement(By.xpath("//input[@placeholder='Username']")).sendKeys("cbo_lynn");
            driver.findElement(By.xpath("//input[@placeholder='Password']")).sendKeys("qweqwe@11");

            String captcha = wait.until(ExpectedConditions.visibilityOfElementLocated(
                    By.xpath("//div[contains(@class,'cursor-pointer space-x-3 text-2xl font-normal tracking-normal')]"))).getText();

            driver.findElement(By.xpath("//input[@placeholder='Captcha Code']")).sendKeys(captcha);
            driver.findElement(By.xpath("//button[text()='Login']")).click();

            wait.until(ExpectedConditions.elementToBeClickable(
                    By.xpath("//div[text()='Admin Game Settings']"))).click();

            wait.until(ExpectedConditions.elementToBeClickable(
                    By.xpath("//a[text()='Provider Game Sequence']"))).click(); //HOT or ALL GAMES need to be selected
                     wait.until(ExpectedConditions.elementToBeClickable(
                    By.xpath("//button[text()='slot']"))).click();           //Category selection

            wait.until(ExpectedConditions.elementToBeClickable(
                    By.xpath("//button[text()='PNG']"))).click(); //Currency selection   HKD, LAK, PNG

            System.out.println("PNG clicked successfully");

           
            String providerName = "POP";   // Provider selection
            selectProviderFromDropdown(driver, wait, providerName);

            // Search box
            WebElement searchBox = wait.until(ExpectedConditions.visibilityOfElementLocated(
                    By.xpath("//input[@placeholder='Search Game Name...']")));

            // Read games from Excel
            List<String> games = readGameNamesFromExcel(excelPath);

            // Row number in Excel:
            // row 0 = header
            // row 1 = Excel row 2
            for (int rowIndex = 0; rowIndex < games.size(); rowIndex++) {

                String gameName = games.get(rowIndex).trim();

                if (gameName.isEmpty()) {
                    writeStatusToExcel(excelPath, rowIndex + 1, "No Game Name");
                    System.out.println("Skipped empty row at Excel row: " + (rowIndex + 2));
                    continue;
                }

                String normalizedInput = normalize(gameName);

                try {
                    String searchableText = makeSearchable(gameName);

                    // Search game
                    searchBox.clear();
                    searchBox.sendKeys(searchableText);
                    Thread.sleep(1500);

                    List<WebElement> results = driver.findElements(
                            By.xpath("//div[contains(@class,'flex w-full items-center justify-between')]")
                    );

                    boolean found = false;

                    for (WebElement item : results) {
                        try {
                            String fullText = item.getText().trim();
                            if (fullText.isEmpty()) {
                                continue;
                            }

                            String title = fullText.split("\n")[0].trim();

                            if (normalize(title).equals(normalizedInput)) {
                                WebElement btn = item.findElement(By.xpath(".//button"));
                                wait.until(ExpectedConditions.elementToBeClickable(btn)).click();

                                System.out.println("Added game: " + gameName);
                                writeStatusToExcel(excelPath, rowIndex + 1, "Added");
                                found = true;
                                break;
                            }

                        } catch (StaleElementReferenceException ignored) {
                        } catch (Exception innerEx) {
                            // ignore individual card issues and continue checking next result
                        }
                    }

                    if (!found) {
                        System.out.println("Game NOT found: " + gameName);
                        writeStatusToExcel(excelPath, rowIndex + 1, "Not Added");
                    }

                } catch (Exception e) {
                    System.out.println("Error processing: " + gameName);
                    writeStatusToExcel(excelPath, rowIndex + 1, "Error");
                }
            }

            System.out.println("Process completed. Check Excel file for Added / Not Added status.");

        } finally {
            // driver.quit(); // Uncomment if you want browser to close automatically
        }
    }
}