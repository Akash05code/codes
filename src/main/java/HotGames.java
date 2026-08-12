

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
     public static void clearPriorityGames(WebDriver driver, WebDriverWait wait)
        throws InterruptedException {

    JavascriptExecutor js = (JavascriptExecutor) driver;

    System.out.println("======================================");
    System.out.println("Clearing Priority Game list...");
    System.out.println("======================================");

    /*
     * Find the Priority Game section.
     *
     * The exact heading text may be:
     * Priority Game
     * Priority Games
     *
     * We first try to locate the section using its visible text.
     */

    List<WebElement> prioritySections = driver.findElements(
            By.xpath("//*[contains(normalize-space(),'Priority Game')]")
    );

    if (prioritySections.isEmpty()) {
        System.out.println("⚠ Priority Game section not found.");
        return;
    }

    WebElement priorityText = prioritySections.get(0);

    /*
     * Move upward through the DOM until we find a reasonably large
     * container containing the priority game cards.
     */
    WebElement priorityContainer = null;

    try {
        priorityContainer = priorityText.findElement(
                By.xpath("./ancestor::div[contains(@class,'flex')][1]")
        );
    } catch (Exception e) {
        System.out.println("⚠ Could not identify Priority Game container.");
        return;
    }

    /*
     * Find the left-arrow/remove buttons.
     *
     * Your HTML contains:
     *
     * <button class="btn !p-1 !text-xl">
     *
     * and the SVG path contains rotate(180).
     */
    List<WebElement> removeButtons = priorityContainer.findElements(
            By.xpath(".//button[contains(@class,'!text-xl')]")
    );

    System.out.println(
            "Priority buttons detected: " + removeButtons.size()
    );

    int removedCount = 0;

    /*
     * Remove one game at a time.
     *
     * We always re-find the buttons because after clicking one,
     * Vue updates the DOM and old WebElements can become stale.
     */
    while (true) {

        List<WebElement> buttons = priorityContainer.findElements(
                By.xpath(".//button[contains(@class,'!text-xl')]")
        );

        if (buttons.isEmpty()) {
            break;
        }

        boolean removed = false;

        for (WebElement button : buttons) {

            try {

                js.executeScript(
                        "arguments[0].scrollIntoView({block:'center'});",
                        button
                );

                try {
                    button.click();
                } catch (Exception e) {
                    js.executeScript(
                            "arguments[0].click();",
                            button
                    );
                }

                removedCount++;
                removed = true;

                Thread.sleep(500);

                break;

            } catch (StaleElementReferenceException e) {
                // DOM changed, find the button again
                break;

            } catch (Exception e) {
                System.out.println(
                        "⚠ Could not remove one priority game."
                );
            }
        }

        if (!removed) {
            break;
        }
    }

    System.out.println(
            "✅ Priority Game list cleared. Removed: "
                    + removedCount + " game(s)"
    );

    System.out.println("======================================");
}

    // ============================================================
    // MAIN
    // ============================================================
    public static void main(String[] args) throws Exception {

        // ========================================================
        // Excel file
        // ========================================================
        String excelPath =
                "C:\\Users\\AKASHKUMAR\\Desktop\\Allgames.xlsx";

        WebDriver driver = new ChromeDriver();

        driver.manage().timeouts().implicitlyWait(
                Duration.ofSeconds(5)
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
                            By.xpath("//button[text()='THB']")
                    )
            ).click();

            // ====================================================
            // CATEGORY
            // ====================================================
            wait.until(
                    ExpectedConditions.elementToBeClickable(
                            By.xpath("//button[text()='slot']")
                    )
            ).click();

            System.out.println("THB clicked successfully");
           // clearPriorityGames(driver, wait);

           Thread.sleep(40000);

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