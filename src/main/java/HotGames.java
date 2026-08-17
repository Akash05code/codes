

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

    // ============================================================
    // Normalize text
    // Removes spaces and ignores upper/lower case
    //
    // Example:
    // JumpHigh  -> jumphigh
    // Jump High -> jumphigh
    // ============================================================
    public static String normalize(String text) {

        if (text == null) {
            return "";
        }

        return text
                .replaceAll("\\s+", "")
                .toLowerCase()
                .trim();
    }

    // ============================================================
    // Convert Excel game name to searchable format
    //
    // Example:
    // JumpHigh -> Jump High
    // ============================================================
    public static String makeSearchable(String text) {

        if (text == null) {
            return "";
        }

        return text
                .replaceAll("([a-z])([A-Z])", "$1 $2")
                .trim();
    }

    // ============================================================
    // Select provider from dropdown
    // ============================================================
    public static void selectProviderFromDropdown(
            WebDriver driver,
            WebDriverWait wait,
            String providerName) throws InterruptedException {

        JavascriptExecutor js = (JavascriptExecutor) driver;

        By providerDropdown = By.xpath(
                "//div[contains(@class,'o-input-wrapper') " +
                "and contains(@class,'o-select-container')]"
        );

        WebElement dropdown = wait.until(
                ExpectedConditions.elementToBeClickable(providerDropdown)
        );

        js.executeScript(
                "arguments[0].scrollIntoView({block:'center'});",
                dropdown
        );

        Thread.sleep(500);

        try {
            dropdown.click();
        } catch (Exception e) {
            js.executeScript("arguments[0].click();", dropdown);
        }

        By dropdownPopup = By.xpath(
                "//div[contains(@class,'o-select-dropdown')]"
        );

        wait.until(
                ExpectedConditions.visibilityOfElementLocated(dropdownPopup)
        );

        By providerOption = By.xpath(
                "//div[contains(@class,'o-select-dropdown')]//span[@data-slug='"
                + providerName +
                "']"
        );

        WebElement option = wait.until(
                ExpectedConditions.visibilityOfElementLocated(providerOption)
        );

        js.executeScript(
                "arguments[0].scrollIntoView({block:'center'});",
                option
        );

        Thread.sleep(500);

        try {
            option.click();
        } catch (Exception e) {
            js.executeScript("arguments[0].click();", option);
        }

        System.out.println("Selected provider: " + providerName);

        Thread.sleep(10000);
    }

    // ============================================================
    // Read Game Name + Provider from Excel
    //
    // Column A = Game Name
    // Column B = Provider
    //
    // Row 1 = Header
    // ============================================================
    public static List<String[]> readGamesFromExcel(
            String filePath) throws Exception {

        List<String[]> games = new ArrayList<>();

        FileInputStream fis = new FileInputStream(filePath);

        Workbook workbook = new XSSFWorkbook(fis);

        Sheet sheet = workbook.getSheetAt(0);

        for (int i = 1; i <= sheet.getLastRowNum(); i++) {

            Row row = sheet.getRow(i);

            if (row == null) {
                games.add(new String[]{"", ""});
                continue;
            }

            // ----------------------------------------------------
            // Column A = Game Name
            // ----------------------------------------------------
            Cell gameCell = row.getCell(0);

            String gameName = "";

            if (gameCell != null) {

                if (gameCell.getCellType() != CellType.STRING) {
                    gameCell.setCellType(CellType.STRING);
                }

                gameName = gameCell
                        .getStringCellValue()
                        .trim();
            }

            // ----------------------------------------------------
            // Column B = Provider
            // ----------------------------------------------------
            Cell providerCell = row.getCell(1);

            String providerName = "";

            if (providerCell != null) {

                if (providerCell.getCellType() != CellType.STRING) {
                    providerCell.setCellType(CellType.STRING);
                }

                providerName = providerCell
                        .getStringCellValue()
                        .trim();
            }

            games.add(new String[]{
                    gameName,
                    providerName
            });
        }

        workbook.close();
        fis.close();

        return games;
    }

    // ============================================================
    // Write status into Column C
    //
    // Column A = Game
    // Column B = Provider
    // Column C = Status
    // ============================================================
    public static void writeStatusToExcel(
            String filePath,
            int rowNumber,
            String status) throws Exception {

        FileInputStream fis = new FileInputStream(filePath);

        Workbook workbook = new XSSFWorkbook(fis);

        Sheet sheet = workbook.getSheetAt(0);

        Row row = sheet.getRow(rowNumber);

        if (row == null) {
            row = sheet.createRow(rowNumber);
        }

        Cell statusCell = row.getCell(2);

        if (statusCell == null) {
            statusCell = row.createCell(2);
        }

        statusCell.setCellValue(status);

        fis.close();

        FileOutputStream fos = new FileOutputStream(filePath);

        workbook.write(fos);

        fos.close();

        workbook.close();
    }
  // ============================================================
// CLEAR ALL GAMES FROM RIGHT-SIDE PRIORITY GAME LIST
// ============================================================
// ============================================================
// CLEAR ALL GAMES FROM RIGHT-SIDE PRIORITY GAME LIST
// ============================================================
public static void clearPriorityGames(
        WebDriver driver,
        WebDriverWait wait) throws InterruptedException {

    JavascriptExecutor js = (JavascriptExecutor) driver;

    System.out.println("======================================");
    System.out.println("Clearing Priority Game list...");
    System.out.println("======================================");

    int removedCount = 0;

    /*
     * IMPORTANT:
     *
     * Right-side Priority Game button:
     *
     * <button class="btn !p-1 !text-xl">
     *     <svg>
     *         <path transform="... rotate(180)">
     *
     * The rotate(180) is the important identifier.
     *
     * Left-side Add buttons do NOT have rotate(180).
     */

    By priorityButtonLocator = By.xpath(
            "//button[contains(@class,'!text-xl')]"
            + "[.//path[contains(@transform,'rotate(180)')]]"
    );

    // =========================================================
    // FIRST CHECK
    // =========================================================

    List<WebElement> initialButtons =
            driver.findElements(priorityButtonLocator);

    System.out.println(
            "Priority buttons found initially: "
                    + initialButtons.size()
    );

    if (initialButtons.isEmpty()) {

        System.out.println(
                "No Priority Game buttons found."
        );

        System.out.println("======================================");

        return;
    }

    // =========================================================
    // REMOVE ONE BY ONE
    // =========================================================

    while (true) {

        /*
         * IMPORTANT:
         *
         * Always find the button again.
         *
         * Vue changes the DOM after every click.
         */

        List<WebElement> buttons =
                driver.findElements(priorityButtonLocator);

        System.out.println(
                "Priority buttons remaining: "
                        + buttons.size()
        );

        // -----------------------------------------------------
        // No more priority buttons
        // -----------------------------------------------------

        if (buttons.isEmpty()) {

            System.out.println(
                    "No more Priority Game buttons."
            );

            break;
        }

        WebElement button = buttons.get(0);

        try {

            // -------------------------------------------------
            // Scroll button into view
            // -------------------------------------------------

            js.executeScript(
                    "arguments[0].scrollIntoView({block:'center'});",
                    button
            );

            Thread.sleep(300);

            // -------------------------------------------------
            // Click
            // -------------------------------------------------

            try {

                button.click();

            } catch (Exception e) {

                System.out.println(
                        "Normal click failed. Using JavaScript click..."
                );

                js.executeScript(
                        "arguments[0].click();",
                        button
                );
            }

            removedCount++;

            System.out.println(
                    "Removed Priority Game #"
                            + removedCount
            );

            /*
             * Give Vue time to move the game from:
             *
             * RIGHT SIDE
             *      ↓
             * LEFT SIDE
             */

            Thread.sleep(800);

        } catch (StaleElementReferenceException e) {

          

            System.out.println(
                    "DOM changed. Re-finding Priority button..."
            );

        } catch (Exception e) {

            System.out.println(
                    "Error clicking Priority button: "
                            + e.getMessage()
            );

            // -------------------------------------------------
            // Retry using a freshly located button
            // -------------------------------------------------

            try {

                List<WebElement> retryButtons =
                        driver.findElements(priorityButtonLocator);

                if (!retryButtons.isEmpty()) {

                    WebElement retryButton =
                            retryButtons.get(0);

                    js.executeScript(
                            "arguments[0].scrollIntoView({block:'center'});",
                            retryButton
                    );

                    Thread.sleep(300);

                    js.executeScript(
                            "arguments[0].click();",
                            retryButton
                    );

                    removedCount++;

                    System.out.println(
                            "Removed Priority Game #"
                                    + removedCount
                                    + " using JavaScript"
                    );

                    Thread.sleep(800);

                } else {

                    System.out.println(
                            "No Priority button available during retry."
                    );

                    break;
                }

            } catch (Exception retryException) {

                System.out.println(
                        "Retry also failed: "
                                + retryException.getMessage()
                );

                break;
            }
        }

        // =====================================================
        // SAFETY LIMIT
        // =====================================================

        /*
         * Prevent an infinite loop in case the website keeps
         * returning the same button.
         */

        if (removedCount >= 1000) {

            System.out.println(
                    "Safety limit reached. Stopping."
            );

            break;
        }
    }

    // =========================================================
    // FINAL CHECK
    // =========================================================

    Thread.sleep(1000);

    List<WebElement> remaining =
            driver.findElements(priorityButtonLocator);

    System.out.println("======================================");

    System.out.println(
            "Priority Game clearing completed."
    );

    System.out.println(
            "Total games removed: "
                    + removedCount
    );

    System.out.println(
            "Priority buttons remaining: "
                    + remaining.size()
    );

    System.out.println("======================================");
}
    // ============================================================
    // MAIN
    // ==========================================================
    public static void main(String[] args) throws Exception {

        // ========================================================
        // Excel file
        // ========================================================
        String excelPath =
                "C:\\Users\\AKASHKUMAR\\Desktop\\Allgames.xlsx";

        WebDriver driver = new ChromeDriver();

        driver.manage().timeouts().implicitlyWait(
                Duration.ofSeconds(1)
        );

        WebDriverWait wait =
                new WebDriverWait(driver, Duration.ofSeconds(15));

        driver.manage().window().maximize();

        try {

            // ====================================================
            // LOGIN
            // ====================================================
            driver.get(
                    "https://centralized-bo.ibscbo.com/en-us"
            );

            driver.findElement(
                    By.xpath("//input[@placeholder='Group Code']")
            ).sendKeys("super");

            driver.findElement(
                    By.xpath("//input[@placeholder='Username']")
            ).sendKeys("cbo_lynn");

            driver.findElement(
                    By.xpath("//input[@placeholder='Password']")
            ).sendKeys("qweqwe@11");

            String captcha = wait.until(
                    ExpectedConditions.visibilityOfElementLocated(
                            By.xpath(
                                    "//div[contains(@class," +
                                    "'cursor-pointer space-x-3 " +
                                    "text-2xl font-normal " +
                                    "tracking-normal')]"
                            )
                    )
            ).getText();

            driver.findElement(
                    By.xpath("//input[@placeholder='Captcha Code']")
            ).sendKeys(captcha);

            driver.findElement(
                    By.xpath("//button[text()='Login']")
            ).click();

            // ====================================================
            // ADMIN GAME SETTINGS
            // ====================================================
            wait.until(
                    ExpectedConditions.elementToBeClickable(
                            By.xpath("//div[text()='Admin Game Settings']")
                    )
            ).click();

            // ====================================================
            // ALL GAME
            // ====================================================
            wait.until(
                    ExpectedConditions.elementToBeClickable(
                            By.xpath("//a[text()='All Game']")
                    )
            ).click();

            // ====================================================
            // CURRENCY
            // ====================================================
            wait.until(
                    ExpectedConditions.elementToBeClickable(
                            By.xpath("//button[text()='BDT']")
                    )
            ).click();

            // ====================================================
            // CATEGORY
            // ====================================================
            wait.until(
                    ExpectedConditions.elementToBeClickable(
                            By.xpath("//button[text()='live']")
                    )
            ).click();

      System.out.println("AUD  clicked successfully");

Thread.sleep(20000);

//clearPriorityGames(driver, wait);

//Thread.sleep(2000);

            // ====================================================
            // OPTIONAL PROVIDER SELECTION
            // ====================================================

            /*
            String providerName = "POP";

            selectProviderFromDropdown(
                    driver,
                    wait,
                    providerName
            );
            */

            // ====================================================
            // GAME SEARCH BOX
            // ====================================================
            WebElement searchBox = wait.until(
                    ExpectedConditions.visibilityOfElementLocated(
                            By.xpath(
                                    "//input[@placeholder=" +
                                    "'Search Game Name...']"
                            )
                    )
            );

            // ====================================================
            // READ GAME + PROVIDER FROM EXCEL
            // ====================================================
            List<String[]> games =
                    readGamesFromExcel(excelPath);

            // ====================================================
            // PROCESS EACH EXCEL ROW
            // ====================================================
            for (int rowIndex = 0;
                 rowIndex < games.size();
                 rowIndex++) {

                String gameName =
                        games.get(rowIndex)[0].trim();

                String excelProvider =
                        games.get(rowIndex)[1].trim();

                int excelRowNumber = rowIndex + 2;

                // ------------------------------------------------
                // Empty Game
                // ------------------------------------------------
                if (gameName.isEmpty()) {

                    writeStatusToExcel(
                            excelPath,
                            rowIndex + 1,
                            "No Game Name"
                    );

                    System.out.println(
                            "Skipped empty game at Excel row: "
                            + excelRowNumber
                    );

                    continue;
                }

                // ------------------------------------------------
                // Empty Provider
                // ------------------------------------------------
                if (excelProvider.isEmpty()) {

                    writeStatusToExcel(
                            excelPath,
                            rowIndex + 1,
                            "No Provider"
                    );

                    System.out.println(
                            "❌ Provider missing for game: "
                            + gameName
                    );

                    continue;
                }

                String normalizedInput =
                        normalize(gameName);

                String normalizedExcelProvider =
                        normalize(excelProvider);

                try {

                    // ============================================
                    // Convert JumpHigh -> Jump High
                    // ============================================
                    String searchableText =
                            makeSearchable(gameName);

                    // ============================================
                    // Search Game
                    // ============================================
                    searchBox.clear();

                    searchBox.sendKeys(searchableText);

                    Thread.sleep(1500);

                    // ============================================
                    // Get visible game cards
                    // ============================================
                    List<WebElement> results =
                            driver.findElements(
                                    By.xpath(
                                            "//div[contains(@class," +
                                            "'flex w-full items-center " +
                                            "justify-between')]"
                                    )
                            );

                    boolean found = false;

                    // ============================================
                    // Check every result
                    // ============================================
                    for (WebElement item : results) {

                        try {

                            String fullText =
                                    item.getText().trim();

                            if (fullText.isEmpty()) {
                                continue;
                            }

                            // ------------------------------------
                            // Get first line = Game Name
                            // ------------------------------------
                            String[] lines =
                                    fullText.split("\\r?\\n");

                            if (lines.length == 0) {
                                continue;
                            }

                            String uiGameName =
                                    lines[0].trim();

                            // ------------------------------------
                            // Locate provider specifically from
                            // <p class="text-xs text-gray-500">
                            // ------------------------------------
                            List<WebElement> providerElements =
                                    item.findElements(
                                            By.xpath(
                                                    ".//p[contains(@class," +
                                                    "'text-xs') and " +
                                                    "contains(@class," +
                                                    "'text-gray-500')]"
                                            )
                                    );

                            if (providerElements.isEmpty()) {

                                System.out.println(
                                        "Provider element not found for: "
                                        + uiGameName
                                );

                                continue;
                            }

                            String uiProvider =
                                    providerElements
                                            .get(0)
                                            .getText()
                                            .trim();

                            // ====================================
                            // NORMALIZED VALUES
                            // ====================================
                            String normalizedUIName =
                                    normalize(uiGameName);

                            String normalizedUIProvider =
                                    normalize(uiProvider);

                            // ====================================
                            // DEBUG OUTPUT
                            // ====================================
                            System.out.println(
                                    "Checking -> Game: ["
                                    + uiGameName
                                    + "] | Provider: ["
                                    + uiProvider
                                    + "]"
                            );

                            // ====================================
                            // EXACT GAME + PROVIDER MATCH
                            // ====================================
                            boolean gameMatches =
                                    normalizedUIName.equals(
                                            normalizedInput
                                    );

                            boolean providerMatches =
                                    normalizedUIProvider.equals(
                                            normalizedExcelProvider
                                    );

                            // ====================================
                            // BOTH MUST MATCH
                            // ====================================
                            if (gameMatches &&
                                providerMatches) {

                                WebElement btn =
                                        item.findElement(
                                                By.xpath(".//button")
                                        );

                                wait.until(
                                        ExpectedConditions
                                                .elementToBeClickable(btn)
                                ).click();

                                System.out.println(
                                        "✅ Added game: "
                                        + gameName
                                        + " | Provider: "
                                        + excelProvider
                                );

                                writeStatusToExcel(
                                        excelPath,
                                        rowIndex + 1,
                                        "Added"
                                );

                                found = true;

                                break;
                            }

                        } catch (
                                StaleElementReferenceException
                                ignored) {

                            // DOM changed, ignore this result
                        } catch (Exception innerEx) {

                            // Ignore individual game card
                        }
                    }

                    // ============================================
                    // RESULT
                    // ============================================
                    if (!found) {

                        System.out.println(
                                "❌ NOT FOUND -> Game: "
                                + gameName
                                + " | Provider: "
                                + excelProvider
                        );

                        writeStatusToExcel(
                                excelPath,
                                rowIndex + 1,
                                "Not Added"
                        );
                    }

                } catch (Exception e) {

                    System.out.println(
                            "❌ Error processing: "
                            + gameName
                            + " | Provider: "
                            + excelProvider
                    );

                    writeStatusToExcel(
                            excelPath,
                            rowIndex + 1,
                            "Error"
                    );
                }
            }

            System.out.println(
                    "========================================"
            );

            System.out.println(
                    "Process completed."
            );

            System.out.println(
                    "Check Excel Column C for status."
            );

            System.out.println(
                    "========================================"
            );

        } finally {

            // Keep browser open for checking results
            // driver.quit();

        }
    }
}