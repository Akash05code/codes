package com.framework;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.time.Duration;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;

public class practice {

    // =========================================================
    // CONFIGURATION
    // =========================================================

    private static final String EXCEL_PATH =
            "C:\\Users\\Akashkumar\\Desktop\\NotificationData.xlsx";

    private static final String APPLICATION_URL =
            "https://admin-centerbo.ibstest.site/system-notification";

    /*
     * Normal Selenium wait.
     */
    private static final int NORMAL_WAIT = 20;

    /*
     * Modal close/open wait.
     */
    private static final int MODAL_WAIT = 10;

    /*
     * AJAX loader wait.
     */
    private static final int AJAX_WAIT = 15;

    /*
     * Save / toast wait.
     */
    private static final int SAVE_WAIT = 30;

    /*
     * Start currency index.
     *
     * THB = 1
     * BDT = 2
     * INR = 3
     * PKR = 4
     * SGD = 5
     * AUD = 6
     * HKD = 7
     * IDR = 8
     * MYR = 9
     * NPR = 10
     * PHP = 11
     * MMK = 12
     * USD = 13
     * VND = 14
     * LAK = 15
     * TWD = 16
     * PGK = 17
     */
    private static final int START_FROM_INDEX = 1;

    /*
     * Result sheet inside the same Excel file.
     */
    private static final String RESULT_SHEET =
            "ExecutionResults";

    // =========================================================
    // EXACT MODAL
    // =========================================================

    private static final By ADD_MAINTENANCE_MODAL =
            By.id("addMaintenance");

    // Currency inside THIS modal.
    private static final By CURRENCY_DROPDOWN =
            By.cssSelector(
                    "#addMaintenance #currencyA"
            );

    // Merchant select inside THIS modal.
    private static final By MERCHANT_DROPDOWN =
            By.cssSelector(
                    "#addMaintenance #merchantList"
            );

    // Provider select inside THIS modal.
    private static final By PROVIDER_DROPDOWN =
            By.cssSelector(
                    "#addMaintenance #providerA"
            );

    // Bootstrap backdrop.
    private static final By MODAL_BACKDROP =
            By.cssSelector(
                    ".modal-backdrop"
            );

    /*
     * Actual Cancel element from your supplied modal:
     *
     * <a ... class="btn btn-default mgr10"
     *    data-dismiss="modal">
     *    Cancel
     * </a>
     */
    private static final By MODAL_CANCEL =
            By.xpath(
                    "//div[@id='addMaintenance']"
                  + "//a[@data-dismiss='modal'"
                  + " and contains(@class,'btn-default')"
                  + " and normalize-space()='Cancel']"
            );

    // =========================================================
    // MAIN
    // =========================================================

    public static void main(String[] args)
            throws InterruptedException {

        // =====================================================
        // READ EXCEL
        // =====================================================

        List<NotificationData> notificationList =
                ExcelReader.readNotificationData(
                        EXCEL_PATH
                );

        System.out.println(
                "Total Rows : "
                        + notificationList.size()
        );

        // =====================================================
        // START DRIVER
        // =====================================================

        WebDriver driver =
                new ChromeDriver();

        driver.manage().window().maximize();

        driver.manage()
                .timeouts()
                .implicitlyWait(
                        Duration.ofSeconds(5)
                );

        WebDriverWait wait =
                new WebDriverWait(
                        driver,
                        Duration.ofSeconds(
                                NORMAL_WAIT
                        )
                );

        JavascriptExecutor js =
                (JavascriptExecutor) driver;

        try {

            // =================================================
            // OPEN APPLICATION
            // =================================================

            driver.get(
                    APPLICATION_URL
            );

            // =================================================
            // LOGIN
            // =================================================

            login(
                    driver,
                    wait
            );

            // =================================================
            // OPEN SYSTEM NOTIFICATION
            // =================================================

            openSystemNotification(
                    driver,
                    wait,
                    js
            );

            // =================================================
            // PROVIDER LOOP
            // =================================================

            for (NotificationData data :
                    notificationList) {

                String desiredProvider =
                        safeTrim(
                                data.getProvider()
                        );

                /*
                 * Ignore blank Excel rows.
                 */
                if (desiredProvider.isEmpty()) {

                    System.out.println(
                            "Blank provider row found."
                    );

                    System.out.println(
                            "Skipping Excel row."
                    );

                    continue;
                }

                String startDateValue =
                        safeTrim(
                                data.getStartDate()
                        );

                String endDateValue =
                        safeTrim(
                                data.getEndDate()
                        );

                String remarkValue =
                        safeTrim(
                                data.getRemark()
                        );

                /*
                 * Failed currencies for THIS provider.
                 *
                 * Currency -> Failure reason
                 */
                Map<String, String>
                        failedCurrencies =
                        new LinkedHashMap<>();

                System.out.println();
                System.out.println(
                        "##############################################"
                );

                System.out.println(
                        "STARTING PROVIDER"
                );

                System.out.println(
                        "Provider : "
                                + desiredProvider
                );

                System.out.println(
                        "Start    : "
                                + startDateValue
                );

                System.out.println(
                        "End      : "
                                + endDateValue
                );

                System.out.println(
                        "Remark   : "
                                + remarkValue
                );

                System.out.println(
                        "##############################################"
                );

                // =================================================
                // OPEN A FRESH MODAL
                // =================================================

                if (!clickAddNotification(
                        driver,
                        wait,
                        js
                )) {

                    System.out.println(
                            "Could not open Add Notification."
                    );

                    System.out.println(
                            "Skipping provider: "
                                    + desiredProvider
                    );

                    continue;
                }

                // =================================================
                // GET CURRENCIES FROM FRESH MODAL
                // =================================================

                List<String> currencyNames =
                        getCurrencyNames(
                                driver,
                                wait
                        );

                if (currencyNames.isEmpty()) {

                    System.out.println(
                            "No currencies found."
                    );

                    closeModalUsingCancel(
                            driver,
                            wait,
                            js
                    );

                    continue;
                }

                System.out.println(
                        "Currencies available: "
                                + currencyNames
                );

                // =================================================
                // NORMAL CURRENCY LOOP
                // =================================================

                for (int i =
                        START_FROM_INDEX;
                     i <= currencyNames.size();
                     i++) {

                    String currencyName =
                            currencyNames.get(
                                    i - 1
                            );

                    System.out.println();
                    System.out.println(
                            "=============================================="
                    );

                    System.out.println(
                            "Provider : "
                                    + desiredProvider
                    );

                    System.out.println(
                            "Currency : "
                                    + currencyName
                    );

                    System.out.println(
                            "Attempt  : 1"
                    );

                    System.out.println(
                            "=============================================="
                    );

                    try {

                        // =========================================
                        // 1. SELECT CURRENCY FROM FRESH MODAL
                        // =========================================

                        boolean currencySelected =
                                selectCurrency(
                                        driver,
                                        wait,
                                        i,
                                        currencyName
                                );

                        if (!currencySelected) {

                            recordFailure(
                                    failedCurrencies,
                                    EXCEL_PATH,
                                    desiredProvider,
                                    currencyName,
                                    "Currency selection failed"
                            );

                            moveToNextCurrency(
                                    driver,
                                    wait,
                                    js,
                                    i,
                                    currencyNames.size()
                            );

                            continue;
                        }

                        // =========================================
                        // 2. SELECT ALL FRESH MERCHANTS
                        // =========================================

                        boolean merchantsSelected =
                                selectAllMerchants(
                                        driver,
                                        wait,
                                        js,
                                        currencyName
                                );

                        if (!merchantsSelected) {

                            recordFailure(
                                    failedCurrencies,
                                    EXCEL_PATH,
                                    desiredProvider,
                                    currencyName,
                                    "Merchant list/options did not load"
                            );

                            moveToNextCurrency(
                                    driver,
                                    wait,
                                    js,
                                    i,
                                    currencyNames.size()
                            );

                            continue;
                        }

                        // =========================================
                        // 3. SELECT PROVIDER
                        // =========================================

                        boolean providerSelected =
                                selectProvider(
                                        driver,
                                        wait,
                                        desiredProvider,
                                        currencyName
                                );

                        if (!providerSelected) {

                            recordFailure(
                                    failedCurrencies,
                                    EXCEL_PATH,
                                    desiredProvider,
                                    currencyName,
                                    "Provider unavailable or selection failed"
                            );

                            moveToNextCurrency(
                                    driver,
                                    wait,
                                    js,
                                    i,
                                    currencyNames.size()
                            );

                            continue;
                        }

                        // =========================================
                        // 4. START DATE
                        // =========================================

                        boolean startDateOK =
                                enterAndVerify(
                                        driver,
                                        wait,
                                        By.id(
                                                "start_dateA"
                                        ),
                                        startDateValue
                                );

                        if (!startDateOK) {

                            recordFailure(
                                    failedCurrencies,
                                    EXCEL_PATH,
                                    desiredProvider,
                                    currencyName,
                                    "Start date validation failed"
                            );

                            moveToNextCurrency(
                                    driver,
                                    wait,
                                    js,
                                    i,
                                    currencyNames.size()
                            );

                            continue;
                        }

                        System.out.println(
                                "Start date validation PASS"
                        );

                        // =========================================
                        // 5. END DATE
                        // =========================================

                        boolean endDateOK =
                                enterAndVerify(
                                        driver,
                                        wait,
                                        By.id(
                                                "end_dateA"
                                        ),
                                        endDateValue
                                );

                        if (!endDateOK) {

                            recordFailure(
                                    failedCurrencies,
                                    EXCEL_PATH,
                                    desiredProvider,
                                    currencyName,
                                    "End date validation failed"
                            );

                            moveToNextCurrency(
                                    driver,
                                    wait,
                                    js,
                                    i,
                                    currencyNames.size()
                            );

                            continue;
                        }

                        System.out.println(
                                "End date validation PASS"
                        );

                        // =========================================
                        // 6. REMARK
                        // =========================================

                        boolean remarkOK =
                                enterAndVerify(
                                        driver,
                                        wait,
                                        By.id(
                                                "remarkA"
                                        ),
                                        remarkValue
                                );

                        if (!remarkOK) {

                            recordFailure(
                                    failedCurrencies,
                                    EXCEL_PATH,
                                    desiredProvider,
                                    currencyName,
                                    "Remark validation failed"
                            );

                            moveToNextCurrency(
                                    driver,
                                    wait,
                                    js,
                                    i,
                                    currencyNames.size()
                            );

                            continue;
                        }

                        System.out.println(
                                "Remark validation PASS"
                        );

                        // =========================================
                        // 7. FINAL VALIDATION
                        // =========================================

                        System.out.println();
                        System.out.println(
                                "======================================"
                        );

                        System.out.println(
                                "VALIDATING BEFORE SAVE"
                        );

                        System.out.println(
                                "Provider : "
                                        + desiredProvider
                        );

                        System.out.println(
                                "Currency : "
                                        + currencyName
                        );

                        System.out.println(
                                "Start    : "
                                        + startDateValue
                        );

                        System.out.println(
                                "End      : "
                                        + endDateValue
                        );

                        System.out.println(
                                "Remark   : "
                                        + remarkValue
                        );

                        System.out.println(
                                "======================================"
                        );

                        boolean validationPassed =
                                NotificationValidator.verifyAll(
                                        driver,
                                        desiredProvider,
                                        startDateValue,
                                        endDateValue,
                                        remarkValue
                                );

                        if (!validationPassed) {

                            recordFailure(
                                    failedCurrencies,
                                    EXCEL_PATH,
                                    desiredProvider,
                                    currencyName,
                                    "Final field validation failed"
                            );

                            moveToNextCurrency(
                                    driver,
                                    wait,
                                    js,
                                    i,
                                    currencyNames.size()
                            );

                            continue;
                        }

                        System.out.println(
                                "ALL VALIDATIONS PASSED"
                        );

                        // =========================================
                        // 8. AJAX BEFORE SAVE
                        // =========================================

                        System.out.println(
                                "Waiting for AJAX processing..."
                        );

                        if (!waitForAjaxLoaderToFinish(
                                driver
                        )) {

                            recordFailure(
                                    failedCurrencies,
                                    EXCEL_PATH,
                                    desiredProvider,
                                    currencyName,
                                    "AJAX loader did not finish before Save"
                            );

                            moveToNextCurrency(
                                    driver,
                                    wait,
                                    js,
                                    i,
                                    currencyNames.size()
                            );

                            continue;
                        }

                        System.out.println(
                                "AJAX processing completed."
                        );

                        // =========================================
                        // 9. SAVE
                        // =========================================

                        WebElement saveButton =
                                wait.until(
                                        ExpectedConditions
                                                .elementToBeClickable(
                                                        By.cssSelector(
                                                                "#addMaintenance #btnAdd"
                                                        )
                                                )
                                );

                        js.executeScript(
                                "arguments[0].scrollIntoView({block:'center'});",
                                saveButton
                        );

                        /*
                         * One final loader check immediately before
                         * Save.
                         */
                        if (!waitForAjaxLoaderToFinish(
                                driver
                        )) {

                            recordFailure(
                                    failedCurrencies,
                                    EXCEL_PATH,
                                    desiredProvider,
                                    currencyName,
                                    "AJAX loader appeared before Save"
                            );

                            moveToNextCurrency(
                                    driver,
                                    wait,
                                    js,
                                    i,
                                    currencyNames.size()
                            );

                            continue;
                        }

                        System.out.println(
                                "Save button found."
                        );

                        saveButton.click();

                        System.out.println(
                                "Save button clicked."
                        );

                        // =========================================
                        // 10. SAVE RESULT
                        // =========================================

                        SaveResult result =
                                waitForSaveResult(
                                        driver,
                                        desiredProvider,
                                        currencyName
                                );

                        if ("PASS".equals(
                                result.status
                        )) {

                            writeExecutionResult(
                                    EXCEL_PATH,
                                    desiredProvider,
                                    currencyName,
                                    "PASS",
                                    1,
                                    result.message
                            );

                        } else if ("UNCONFIRMED".equals(
                                result.status
                        )) {

                            /*
                             * Do NOT automatically retry a missing
                             * toast because you already observed that
                             * a notification may be saved even when
                             * the toast is delayed.
                             */
                            writeExecutionResult(
                                    EXCEL_PATH,
                                    desiredProvider,
                                    currencyName,
                                    "UNCONFIRMED",
                                    1,
                                    result.message
                            );

                        } else {

                            recordFailure(
                                    failedCurrencies,
                                    EXCEL_PATH,
                                    desiredProvider,
                                    currencyName,
                                    result.message
                            );
                        }

                        // =========================================
                        // 11. CLOSE THIS MODAL
                        // =========================================

                        closeModalUsingCancel(
                                driver,
                                wait,
                                js
                        );

                        // =========================================
                        // 12. OPEN NEXT FRESH MODAL ONLY IF NEEDED
                        // =========================================

                        if (i <
                                currencyNames.size()) {

                            if (!clickAddNotification(
                                    driver,
                                    wait,
                                    js
                            )) {

                                System.out.println(
                                        "Could not open fresh modal "
                                      + "for next currency."
                                );

                                break;
                            }

                        } else {

                            System.out.println(
                                    "Last currency completed for provider: "
                                            + desiredProvider
                            );

                            System.out.println(
                                    "No new modal will be opened."
                            );
                        }

                    } catch (Exception e) {

                        String reason =
                                "Unexpected error: "
                                        + e.getMessage();

                        System.out.println();
                        System.out.println(
                                "!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!"
                        );

                        System.out.println(
                                "ERROR PROCESSING CURRENCY"
                        );

                        System.out.println(
                                "Provider : "
                                        + desiredProvider
                        );

                        System.out.println(
                                "Currency : "
                                        + currencyName
                        );

                        System.out.println(
                                "Error    : "
                                        + e.getMessage()
                        );

                        System.out.println(
                                "!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!"
                        );

                        recordFailure(
                                failedCurrencies,
                                EXCEL_PATH,
                                desiredProvider,
                                currencyName,
                                reason
                        );

                        moveToNextCurrency(
                                driver,
                                wait,
                                js,
                                i,
                                currencyNames.size()
                        );
                    }
                }

                // =================================================
                // NORMAL CURRENCIES FINISHED
                // =================================================

                System.out.println();
                System.out.println(
                        "=============================================="
                );

                System.out.println(
                        "NORMAL CURRENCY PROCESSING COMPLETED"
                );

                System.out.println(
                        "Provider : "
                                + desiredProvider
                );

                if (failedCurrencies.isEmpty()) {

                    System.out.println(
                            "No definite failed currencies."
                    );

                } else {

                    System.out.println(
                            "Failed currencies:"
                    );

                    for (Map.Entry<String, String> entry :
                            failedCurrencies.entrySet()) {

                        System.out.println(
                                entry.getKey()
                                + " -> "
                                + entry.getValue()
                        );
                    }
                }

                System.out.println(
                        "=============================================="
                );

                // =================================================
                // RETRY FAILED CURRENCIES
                // =================================================

                if (!failedCurrencies.isEmpty()) {

                    retryFailedCurrencies(
                            driver,
                            wait,
                            js,
                            desiredProvider,
                            startDateValue,
                            endDateValue,
                            remarkValue,
                            failedCurrencies
                    );
                }

                // =================================================
                // PROVIDER COMPLETED
                // =================================================

                System.out.println();
                System.out.println(
                        "##############################################"
                );

                System.out.println(
                        "PROVIDER COMPLETED"
                );

                System.out.println(
                        "Provider : "
                                + desiredProvider
                );

                System.out.println(
                        "##############################################"
                );
            }

            // =====================================================
            // ALL PROVIDERS COMPLETED
            // =====================================================

            System.out.println();
            System.out.println(
                    "=============================================="
            );

            System.out.println(
                    "ALL EXCEL PROVIDERS COMPLETED"
            );

            System.out.println(
                    "=============================================="
            );

        } finally {

            driver.quit();
        }
    }

    // =========================================================
    // LOGIN
    // =========================================================

    private static void login(
            WebDriver driver,
            WebDriverWait wait) {

        wait.until(
                ExpectedConditions
                        .visibilityOfElementLocated(
                                By.id("mer_code")
                        )
        ).sendKeys(
                "masterbo"
        );

        driver.findElement(
                By.id("username")
        ).sendKeys(
                "mbproviderteam"
        );

        driver.findElement(
                By.id("password")
        ).sendKeys(
                "asdf1234"
        );

        String captcha =
                driver.findElement(
                        By.xpath(
                                "//span[@class='input-group-addon captchaNum']"
                        )
                ).getText().trim();

        driver.findElement(
                By.id("captcha")
        ).sendKeys(
                captcha
        );

        driver.findElement(
                By.id("btnLogin")
        ).click();

        wait.until(
                ExpectedConditions
                        .presenceOfElementLocated(
                                By.xpath(
                                        "//span[contains(text(),'System')]"
                                )
                        )
        );

        System.out.println(
                "Login successful."
        );
    }

    // =========================================================
    // OPEN SYSTEM NOTIFICATION
    // =========================================================

    private static void openSystemNotification(
            WebDriver driver,
            WebDriverWait wait,
            JavascriptExecutor js) {

        WebElement notification =
                wait.until(
                        ExpectedConditions
                                .presenceOfElementLocated(
                                        By.xpath(
                                                "//span[contains(text(),'3.1 System Notification')]"
                                        )
                                )
                );

        js.executeScript(
                "arguments[0].scrollIntoView({block:'center'});",
                notification
        );

        js.executeScript(
                "arguments[0].click();",
                notification
        );

        wait.until(
                ExpectedConditions
                        .presenceOfElementLocated(
                                By.id("btnAddNew")
                        )
        );

        System.out.println(
                "System Notification opened."
        );
    }

    // =========================================================
    // OPEN FRESH ADD NOTIFICATION MODAL
    // =========================================================

    private static boolean clickAddNotification(
            WebDriver driver,
            WebDriverWait wait,
            JavascriptExecutor js) {

        try {

            // =====================================================
            // 1. MAKE SURE OLD MODAL IS COMPLETELY CLOSED
            // =====================================================

            waitForModalCompletelyClosed(
                    driver
            );

            // =====================================================
            // 2. FIND ADD BUTTON
            // =====================================================

            WebElement addButton =
                    wait.until(
                            ExpectedConditions
                                    .presenceOfElementLocated(
                                            By.id("btnAddNew")
                                    )
                    );

            js.executeScript(
                    "arguments[0].scrollIntoView({block:'center'});",
                    addButton
            );

            // =====================================================
            // 3. CLICK ADD BUTTON
            // =====================================================

            try {

                wait.until(
                        ExpectedConditions
                                .elementToBeClickable(
                                        By.id("btnAddNew")
                                )
                ).click();

            } catch (Exception e) {

                System.out.println(
                        "Normal Add Notification click failed."
                );

                System.out.println(
                        "Using JavaScript click..."
                );

                addButton =
                        wait.until(
                                ExpectedConditions
                                        .presenceOfElementLocated(
                                                By.id("btnAddNew")
                                        )
                        );

                js.executeScript(
                        "arguments[0].click();",
                        addButton
                );
            }

            // =====================================================
            // 4. WAIT FOR THE ACTUAL NEW MODAL
            // =====================================================

            wait.until(
                    ExpectedConditions
                            .visibilityOfElementLocated(
                                    ADD_MAINTENANCE_MODAL
                            )
            );

            // =====================================================
            // 5. WAIT FOR FRESH CURRENCY DROPDOWN
            // =====================================================

            wait.until(
                    ExpectedConditions
                            .visibilityOfElementLocated(
                                    CURRENCY_DROPDOWN
                            )
            );

            // =====================================================
            // 6. VERIFY NEW MODAL IS RESET
            // =====================================================

            wait.until(driver1 -> {

                try {

                    WebElement currency =
                            driver1.findElement(
                                    CURRENCY_DROPDOWN
                            );

                    Select select =
                            new Select(
                                    currency
                            );

                    String selected =
                            select
                                    .getFirstSelectedOption()
                                    .getText()
                                    .trim();

                    return selected.equalsIgnoreCase(
                            "Please Select Currency"
                    );

                } catch (Exception e) {

                    return false;
                }
            });

            System.out.println(
                    "Fresh Add Notification modal opened."
            );

            return true;

        } catch (Exception e) {

            System.out.println(
                    "Failed to open fresh Add Notification:"
            );

            System.out.println(
                    e.getMessage()
            );

            return false;
        }
    }

    // =========================================================
    // WAIT UNTIL ACTUAL MODAL + BACKDROP ARE CLOSED
    // =========================================================

    private static void waitForModalCompletelyClosed(
            WebDriver driver) {

        try {

            WebDriverWait modalWait =
                    new WebDriverWait(
                            driver,
                            Duration.ofSeconds(
                                    MODAL_WAIT
                            )
                    );

            modalWait.until(
                    ExpectedConditions
                            .invisibilityOfElementLocated(
                                    ADD_MAINTENANCE_MODAL
                            )
            );

        } catch (TimeoutException e) {

            /*
             * Do not silently continue as if the modal is closed.
             * The caller will still try the Add button only after
             * this wait.
             */
            System.out.println(
                    "Add Maintenance modal did not disappear "
                  + "within "
                  + MODAL_WAIT
                  + " seconds."
            );
        }

        try {

            WebDriverWait backdropWait =
                    new WebDriverWait(
                            driver,
                            Duration.ofSeconds(
                                    MODAL_WAIT
                            )
                    );

            backdropWait.until(
                    ExpectedConditions
                            .invisibilityOfElementLocated(
                                    MODAL_BACKDROP
                            )
            );

        } catch (TimeoutException e) {

            System.out.println(
                    "Modal backdrop still visible after "
                            + MODAL_WAIT
                            + " seconds."
            );
        }

        sleep(300);
    }

    // =========================================================
    // CLOSE CURRENT MODAL USING REAL CANCEL
    // =========================================================

    private static void closeModalUsingCancel(
            WebDriver driver,
            WebDriverWait wait,
            JavascriptExecutor js) {

        try {

            /*
             * Cancel ONLY inside addMaintenance.
             */
            List<WebElement> cancelButtons =
                    driver.findElements(
                            MODAL_CANCEL
                    );

            boolean clicked = false;

            for (WebElement cancel :
                    cancelButtons) {

                try {

                    if (cancel.isDisplayed()) {

                        try {

                            cancel.click();

                        } catch (Exception e) {

                            js.executeScript(
                                    "arguments[0].click();",
                                    cancel
                            );
                        }

                        clicked = true;

                        System.out.println(
                                "Modal Cancel clicked."
                        );

                        break;
                    }

                } catch (
                        StaleElementReferenceException ignored) {
                }
            }

            if (!clicked) {

                System.out.println(
                        "No visible Cancel button found."
                );
            }

            /*
             * Wait for the actual modal and backdrop.
             */
            waitForModalCompletelyClosed(
                    driver
            );

        } catch (Exception e) {

            System.out.println(
                    "Error closing modal: "
                            + e.getMessage()
            );
        }
    }

    // =========================================================
    // GET CURRENCIES
    // =========================================================

    private static List<String> getCurrencyNames(
            WebDriver driver,
            WebDriverWait wait) {

        List<String> currencies =
                new ArrayList<>();

        WebElement dropdown =
                wait.until(
                        ExpectedConditions
                                .visibilityOfElementLocated(
                                        CURRENCY_DROPDOWN
                                )
                );

        Select select =
                new Select(
                        dropdown
                );

        List<WebElement> options =
                select.getOptions();

        for (int i = 1;
             i < options.size();
             i++) {

            String text =
                    options.get(i)
                            .getText()
                            .trim();

            if (text.isEmpty()) {

                text =
                        options.get(i)
                                .getAttribute(
                                        "value"
                                )
                                .trim();
            }

            if (!text.isEmpty()) {

                currencies.add(
                        text
                );
            }
        }

        return currencies;
    }

    // =========================================================
    // SELECT CURRENCY FROM FRESH MODAL
    // =========================================================

    private static boolean selectCurrency(
            WebDriver driver,
            WebDriverWait wait,
            int index,
            String expectedCurrency) {

        try {

            /*
             * Always locate the current modal's currency dropdown.
             */
            WebElement currencyDropdown =
                    wait.until(
                            ExpectedConditions
                                    .visibilityOfElementLocated(
                                            CURRENCY_DROPDOWN
                                    )
                    );

            Select select =
                    new Select(
                            currencyDropdown
                    );

            select.selectByIndex(
                    index
            );

            /*
             * Verify selected currency.
             */
            wait.until(driver1 -> {

                try {

                    WebElement dropdown =
                            driver1.findElement(
                                    CURRENCY_DROPDOWN
                            );

                    Select currentSelect =
                            new Select(
                                    dropdown
                            );

                    String actual =
                            currentSelect
                                    .getFirstSelectedOption()
                                    .getText()
                                    .trim();

                    return actual.equalsIgnoreCase(
                            expectedCurrency
                    );

                } catch (Exception e) {

                    return false;
                }
            });

            System.out.println(
                    "Currency selected: "
                            + expectedCurrency
            );

            System.out.println(
                    "Currency validation PASS"
            );

            /*
             * Currency change can trigger AJAX.
             * Do NOT move to merchants until it finishes.
             */
            if (!waitForAjaxLoaderToFinish(
                    driver
            )) {

                System.out.println(
                        "Currency AJAX did not finish."
                );

                return false;
            }

            return true;

        } catch (Exception e) {

            System.out.println(
                    "Currency selection failed: "
                            + expectedCurrency
            );

            System.out.println(
                    e.getMessage()
            );

            return false;
        }
    }

    // =========================================================
    // SELECT ALL MERCHANTS FROM FRESH MODAL
    // =========================================================

    private static boolean selectAllMerchants(
            WebDriver driver,
            WebDriverWait wait,
            JavascriptExecutor js,
            String currencyName) {

        try {

            System.out.println(
                    "Waiting for merchants to load for "
                            + currencyName
            );

            /*
             * merchantList is Select2-hidden-accessible in your
             * supplied HTML, so we use PRESENCE, not visibility.
             */
            wait.until(
                    ExpectedConditions
                            .presenceOfElementLocated(
                                    MERCHANT_DROPDOWN
                            )
            );

            /*
             * Wait until merchant OPTIONS are populated.
             */
            wait.until(driver1 -> {

                try {

                    WebElement merchant =
                            driver1.findElement(
                                    MERCHANT_DROPDOWN
                            );

                    Select select =
                            new Select(
                                    merchant
                            );

                    return select
                            .getOptions()
                            .size() > 0;

                } catch (StaleElementReferenceException e) {

                    return false;

                } catch (Exception e) {

                    return false;
                }
            });

            /*
             * IMPORTANT:
             * Re-find after AJAX.
             */
            WebElement merchant =
                    driver.findElement(
                            MERCHANT_DROPDOWN
                    );

            Select merchantSelect =
                    new Select(
                            merchant
                    );

            int merchantCount =
                    merchantSelect
                            .getOptions()
                            .size();

            System.out.println(
                    "Merchant options found: "
                            + merchantCount
            );

            if (merchantCount == 0) {

                return false;
            }

            // =====================================================
            // CLEAR PREVIOUS SELECTIONS
            // =====================================================

            js.executeScript(
                    """
                    var sel = arguments[0];

                    for (var i = 0;
                         i < sel.options.length;
                         i++) {

                        sel.options[i].selected = false;
                    }

                    sel.dispatchEvent(
                        new Event(
                            'change',
                            { bubbles: true }
                        )
                    );

                    if (window.jQuery) {
                        window.jQuery(sel).trigger('change');
                    }
                    """,
                    merchant
            );

            sleep(300);

            // =====================================================
            // RE-FIND AGAIN
            // =====================================================

            merchant =
                    driver.findElement(
                            MERCHANT_DROPDOWN
                    );

            // =====================================================
            // SELECT ALL
            // =====================================================

            js.executeScript(
                    """
                    var sel = arguments[0];

                    for (var i = 0;
                         i < sel.options.length;
                         i++) {

                        sel.options[i].selected = true;
                    }

                    sel.dispatchEvent(
                        new Event(
                            'change',
                            { bubbles: true }
                        )
                    );

                    if (window.jQuery) {
                        window.jQuery(sel).trigger('change');
                    }
                    """,
                    merchant
            );

            // =====================================================
            // VERIFY ALL MERCHANTS
            // =====================================================

            wait.until(driver1 -> {

                try {

                    WebElement currentMerchant =
                            driver1.findElement(
                                    MERCHANT_DROPDOWN
                            );

                    Select currentSelect =
                            new Select(
                                    currentMerchant
                            );

                    List<WebElement> options =
                            currentSelect
                                    .getOptions();

                    if (options.isEmpty()) {

                        return false;
                    }

                    int selectedCount = 0;

                    for (WebElement option :
                            options) {

                        if (option.isSelected()) {

                            selectedCount++;
                        }
                    }

                    System.out.println(
                            "Merchants selected: "
                                    + selectedCount
                                    + " / "
                                    + options.size()
                    );

                    return selectedCount ==
                            options.size();

                } catch (Exception e) {

                    return false;
                }
            });

            System.out.println(
                    "All merchants selected successfully for "
                            + currencyName
            );

            /*
             * Merchant selection can trigger AJAX.
             */
            if (!waitForAjaxLoaderToFinish(
                    driver
            )) {

                System.out.println(
                        "Merchant AJAX did not finish."
                );

                return false;
            }

            return true;

        } catch (TimeoutException e) {

            System.out.println(
                    "Merchant list/options did not load for "
                            + currencyName
            );

            return false;

        } catch (Exception e) {

            System.out.println(
                    "Merchant selection failed for "
                            + currencyName
            );

            System.out.println(
                    e.getMessage()
            );

            return false;
        }
    }

    // =========================================================
    // SELECT PROVIDER
    // =========================================================

    private static boolean selectProvider(
            WebDriver driver,
            WebDriverWait wait,
            String desiredProvider,
            String currencyName) {

        for (int retry = 0;
             retry < 5;
             retry++) {

            try {

                /*
                 * Wait until provider options exist.
                 */
                wait.until(driver1 -> {

                    try {

                        WebElement provider =
                                driver1.findElement(
                                        PROVIDER_DROPDOWN
                                );

                        Select select =
                                new Select(
                                        provider
                                );

                        return select
                                .getOptions()
                                .size() > 0;

                    } catch (Exception e) {

                        return false;
                    }
                });

                /*
                 * Re-find fresh provider dropdown.
                 */
                WebElement providerDropdown =
                        wait.until(
                                ExpectedConditions
                                        .visibilityOfElementLocated(
                                                PROVIDER_DROPDOWN
                                        )
                        );

                Select selectProvider =
                        new Select(
                                providerDropdown
                        );

                boolean providerExists =
                        selectProvider
                                .getOptions()
                                .stream()
                                .anyMatch(
                                        option ->
                                                option.getText()
                                                        .trim()
                                                        .equals(
                                                                desiredProvider
                                                        )
                                );

                if (!providerExists) {

                    System.out.println(
                            "Provider NOT AVAILABLE: "
                                    + desiredProvider
                    );

                    System.out.println(
                            "Currency: "
                                    + currencyName
                    );

                    return false;
                }

                selectProvider
                        .selectByVisibleText(
                                desiredProvider
                        );

                /*
                 * Verify provider selection.
                 */
                wait.until(driver1 -> {

                    try {

                        WebElement provider =
                                driver1.findElement(
                                        PROVIDER_DROPDOWN
                                );

                        Select currentSelect =
                                new Select(
                                        provider
                                );

                        String actual =
                                currentSelect
                                        .getFirstSelectedOption()
                                        .getText()
                                        .trim();

                        return actual.equals(
                                desiredProvider
                        );

                    } catch (Exception e) {

                        return false;
                    }
                });

                System.out.println(
                        "Provider selected successfully: "
                                + desiredProvider
                );

                return true;

            } catch (
                    StaleElementReferenceException e) {

                System.out.println(
                        "Provider dropdown stale. Retry: "
                                + (retry + 1)
                );

                sleep(300);

            } catch (Exception e) {

                System.out.println(
                        "Provider not ready. Retry: "
                                + (retry + 1)
                );

                sleep(300);
            }
        }

        System.out.println(
                "Provider selection failed: "
                        + desiredProvider
        );

        return false;
    }

    // =========================================================
    // ENTER + VERIFY TEXT
    // =========================================================

    private static boolean enterAndVerify(
            WebDriver driver,
            WebDriverWait wait,
            By locator,
            String expectedValue) {

        try {

            WebElement field =
                    wait.until(
                            ExpectedConditions
                                    .visibilityOfElementLocated(
                                            locator
                                    )
                    );

            field.clear();

            field.sendKeys(
                    expectedValue
            );

            String actual =
                    field
                            .getAttribute(
                                    "value"
                            )
                            .trim();

            return actual.equals(
                    expectedValue
            );

        } catch (Exception e) {

            return false;
        }
    }

    // =========================================================
    // AJAX LOADER
    // =========================================================

    private static boolean waitForAjaxLoaderToFinish(
            WebDriver driver) {

        try {

            WebDriverWait ajaxWait =
                    new WebDriverWait(
                            driver,
                            Duration.ofSeconds(
                                    AJAX_WAIT
                            )
                    );

            ajaxWait.until(
                    ExpectedConditions
                            .invisibilityOfElementLocated(
                                    By.cssSelector(
                                            ".ajaxLoader"
                                    )
                            )
            );

            return true;

        } catch (TimeoutException e) {

            System.out.println(
                    "AJAX loader did not disappear within "
                            + AJAX_WAIT
                            + " seconds."
            );

            return false;
        }
    }

    // =========================================================
    // SAVE RESULT
    // =========================================================

    private static SaveResult waitForSaveResult(
            WebDriver driver,
            String provider,
            String currency) {

        try {

            WebDriverWait saveWait =
                    new WebDriverWait(
                            driver,
                            Duration.ofSeconds(
                                    SAVE_WAIT
                            )
                    );

            WebElement successMessage =
                    saveWait.until(
                            ExpectedConditions
                                    .visibilityOfElementLocated(
                                            By.id(
                                                    "snackbar-msg"
                                            )
                                    )
                    );

            String message =
                    successMessage
                            .getText()
                            .trim();

            if ("Submit Successful"
                    .equalsIgnoreCase(
                            message
                    )) {

                System.out.println();
                System.out.println(
                        "======================================"
                );

                System.out.println(
                        "SUBMIT SUCCESSFUL"
                );

                System.out.println(
                        "Provider : "
                                + provider
                );

                System.out.println(
                        "Currency : "
                                + currency
                );

                System.out.println(
                        "======================================"
                );

                return new SaveResult(
                        "PASS",
                        "Submit Successful"
                );
            }

            return new SaveResult(
                    "FAIL",
                    "Unexpected toast: "
                            + message
            );

        } catch (TimeoutException e) {

            /*
             * Don't automatically retry a missing toast.
             * The backend may have processed the request.
             */
            System.out.println();
            System.out.println(
                    "SUCCESS TOAST NOT RECEIVED WITHIN "
                            + SAVE_WAIT
                            + " SECONDS."
            );

            System.out.println(
                    "Status: UNCONFIRMED"
            );

            return new SaveResult(
                    "UNCONFIRMED",
                    "Success toast not received within "
                            + SAVE_WAIT
                            + " seconds"
            );
        }
    }

    // =========================================================
    // MOVE TO NEXT CURRENCY
    // =========================================================

    private static void moveToNextCurrency(
            WebDriver driver,
            WebDriverWait wait,
            JavascriptExecutor js,
            int currentIndex,
            int totalCurrencies) {

        /*
         * Always close current modal first.
         */
        closeModalUsingCancel(
                driver,
                wait,
                js
        );

        /*
         * Only open a new modal when another currency exists.
         */
        if (currentIndex <
                totalCurrencies) {

            boolean opened =
                    clickAddNotification(
                            driver,
                            wait,
                            js
                    );

            if (!opened) {

                System.out.println(
                        "Could not open fresh modal "
                                + "for next currency."
                );
            }
        }
    }

    // =========================================================
    // RETRY FAILED CURRENCIES
    // =========================================================

    private static void retryFailedCurrencies(
            WebDriver driver,
            WebDriverWait wait,
            JavascriptExecutor js,
            String desiredProvider,
            String startDateValue,
            String endDateValue,
            String remarkValue,
            Map<String, String> failedCurrencies) {

        System.out.println();
        System.out.println(
                "**********************************************"
        );

        System.out.println(
                "STARTING RETRY OF FAILED CURRENCIES"
        );

        System.out.println(
                "Provider : "
                        + desiredProvider
        );

        System.out.println(
                "**********************************************"
        );

        List<String> retryCurrencies =
                new ArrayList<>(
                        failedCurrencies.keySet()
                );

        for (String failedCurrency :
                retryCurrencies) {

            System.out.println();
            System.out.println(
                    "----------------------------------------------"
            );

            System.out.println(
                    "RETRY ATTEMPT : 2"
            );

            System.out.println(
                    "Provider : "
                            + desiredProvider
            );

            System.out.println(
                    "Currency : "
                            + failedCurrency
            );

            System.out.println(
                    "Original Error : "
                            + failedCurrencies.get(
                                    failedCurrency
                            )
            );

            System.out.println(
                    "----------------------------------------------"
            );

            // =================================================
            // OPEN FRESH MODAL
            // =================================================

            if (!clickAddNotification(
                    driver,
                    wait,
                    js
            )) {

                writeExecutionResult(
                        EXCEL_PATH,
                        desiredProvider,
                        failedCurrency,
                        "RETRY-FAIL",
                        2,
                        "Could not open fresh Add Notification"
                );

                continue;
            }

            try {

                // =============================================
                // FIND FAILED CURRENCY INDEX
                // =============================================

                int retryIndex =
                        findCurrencyIndex(
                                driver,
                                wait,
                                failedCurrency
                        );

                if (retryIndex == -1) {

                    writeExecutionResult(
                            EXCEL_PATH,
                            desiredProvider,
                            failedCurrency,
                            "RETRY-FAIL",
                            2,
                            "Currency not found in dropdown"
                    );

                    closeModalUsingCancel(
                            driver,
                            wait,
                            js
                    );

                    continue;
                }

                // =============================================
                // SELECT FAILED CURRENCY
                // =============================================

                if (!selectCurrency(
                        driver,
                        wait,
                        retryIndex,
                        failedCurrency
                )) {

                    writeExecutionResult(
                            EXCEL_PATH,
                            desiredProvider,
                            failedCurrency,
                            "RETRY-FAIL",
                            2,
                            "Currency selection failed on retry"
                    );

                    closeModalUsingCancel(
                            driver,
                            wait,
                            js
                    );

                    continue;
                }

                // =============================================
                // MERCHANTS
                // =============================================

                if (!selectAllMerchants(
                        driver,
                        wait,
                        js,
                        failedCurrency
                )) {

                    writeExecutionResult(
                            EXCEL_PATH,
                            desiredProvider,
                            failedCurrency,
                            "RETRY-FAIL",
                            2,
                            "Merchant selection failed on retry"
                    );

                    closeModalUsingCancel(
                            driver,
                            wait,
                            js
                    );

                    continue;
                }

                // =============================================
                // PROVIDER
                // =============================================

                if (!selectProvider(
                        driver,
                        wait,
                        desiredProvider,
                        failedCurrency
                )) {

                    writeExecutionResult(
                            EXCEL_PATH,
                            desiredProvider,
                            failedCurrency,
                            "RETRY-FAIL",
                            2,
                            "Provider selection failed on retry"
                    );

                    closeModalUsingCancel(
                            driver,
                            wait,
                            js
                    );

                    continue;
                }

                // =============================================
                // START DATE
                // =============================================

                if (!enterAndVerify(
                        driver,
                        wait,
                        By.id("start_dateA"),
                        startDateValue
                )) {

                    writeExecutionResult(
                            EXCEL_PATH,
                            desiredProvider,
                            failedCurrency,
                            "RETRY-FAIL",
                            2,
                            "Start date failed on retry"
                    );

                    closeModalUsingCancel(
                            driver,
                            wait,
                            js
                    );

                    continue;
                }

                // =============================================
                // END DATE
                // =============================================

                if (!enterAndVerify(
                        driver,
                        wait,
                        By.id("end_dateA"),
                        endDateValue
                )) {

                    writeExecutionResult(
                            EXCEL_PATH,
                            desiredProvider,
                            failedCurrency,
                            "RETRY-FAIL",
                            2,
                            "End date failed on retry"
                    );

                    closeModalUsingCancel(
                            driver,
                            wait,
                            js
                    );

                    continue;
                }

                // =============================================
                // REMARK
                // =============================================

                if (!enterAndVerify(
                        driver,
                        wait,
                        By.id("remarkA"),
                        remarkValue
                )) {

                    writeExecutionResult(
                            EXCEL_PATH,
                            desiredProvider,
                            failedCurrency,
                            "RETRY-FAIL",
                            2,
                            "Remark failed on retry"
                    );

                    closeModalUsingCancel(
                            driver,
                            wait,
                            js
                    );

                    continue;
                }

                // =============================================
                // FINAL VALIDATION
                // =============================================

                boolean retryValidation =
                        NotificationValidator.verifyAll(
                                driver,
                                desiredProvider,
                                startDateValue,
                                endDateValue,
                                remarkValue
                        );

                if (!retryValidation) {

                    writeExecutionResult(
                            EXCEL_PATH,
                            desiredProvider,
                            failedCurrency,
                            "RETRY-FAIL",
                            2,
                            "Final validation failed on retry"
                    );

                    closeModalUsingCancel(
                            driver,
                            wait,
                            js
                    );

                    continue;
                }

                // =============================================
                // AJAX BEFORE RETRY SAVE
                // =============================================

                if (!waitForAjaxLoaderToFinish(
                        driver
                )) {

                    writeExecutionResult(
                            EXCEL_PATH,
                            desiredProvider,
                            failedCurrency,
                            "RETRY-FAIL",
                            2,
                            "AJAX loader did not finish on retry"
                    );

                    closeModalUsingCancel(
                            driver,
                            wait,
                            js
                    );

                    continue;
                }

                // =============================================
                // RETRY SAVE
                // =============================================

                WebElement retrySave =
                        wait.until(
                                ExpectedConditions
                                        .elementToBeClickable(
                                                By.cssSelector(
                                                        "#addMaintenance #btnAdd"
                                                )
                                        )
                        );

                retrySave.click();

                System.out.println(
                        "Retry Save clicked."
                );

                // =============================================
                // RETRY RESULT
                // =============================================

                SaveResult retryResult =
                        waitForSaveResult(
                                driver,
                                desiredProvider,
                                failedCurrency
                        );

                if ("PASS".equals(
                        retryResult.status
                )) {

                    writeExecutionResult(
                            EXCEL_PATH,
                            desiredProvider,
                            failedCurrency,
                            "RETRY-PASS",
                            2,
                            retryResult.message
                    );

                } else if ("UNCONFIRMED".equals(
                        retryResult.status
                )) {

                    writeExecutionResult(
                            EXCEL_PATH,
                            desiredProvider,
                            failedCurrency,
                            "RETRY-UNCONFIRMED",
                            2,
                            retryResult.message
                    );

                } else {

                    writeExecutionResult(
                            EXCEL_PATH,
                            desiredProvider,
                            failedCurrency,
                            "RETRY-FAIL",
                            2,
                            retryResult.message
                    );
                }

                closeModalUsingCancel(
                        driver,
                        wait,
                        js
                );

            } catch (Exception e) {

                writeExecutionResult(
                        EXCEL_PATH,
                        desiredProvider,
                        failedCurrency,
                        "RETRY-FAIL",
                        2,
                        "Retry exception: "
                                + e.getMessage()
                );

                System.out.println(
                        "Retry failed for "
                                + failedCurrency
                                + ": "
                                + e.getMessage()
                );

                closeModalUsingCancel(
                        driver,
                        wait,
                        js
                );
            }
        }
    }

    // =========================================================
    // FIND CURRENCY INDEX IN CURRENT FRESH MODAL
    // =========================================================

    private static int findCurrencyIndex(
            WebDriver driver,
            WebDriverWait wait,
            String currencyName) {

        try {

            WebElement dropdown =
                    wait.until(
                            ExpectedConditions
                                    .visibilityOfElementLocated(
                                            CURRENCY_DROPDOWN
                                    )
                    );

            Select select =
                    new Select(
                            dropdown
                    );

            List<WebElement> options =
                    select.getOptions();

            for (int i = 1;
                 i < options.size();
                 i++) {

                String text =
                        options.get(i)
                                .getText()
                                .trim();

                if (text.equals(
                        currencyName
                )) {

                    return i;
                }
            }

        } catch (Exception e) {

            System.out.println(
                    "Could not find currency index: "
                            + e.getMessage()
            );
        }

        return -1;
    }

    // =========================================================
    // RECORD FAILURE
    // =========================================================

    private static void recordFailure(
            Map<String, String> failedCurrencies,
            String excelPath,
            String provider,
            String currency,
            String reason) {

        if (!failedCurrencies.containsKey(
                currency
        )) {

            failedCurrencies.put(
                    currency,
                    reason
            );
        }

        writeExecutionResult(
                excelPath,
                provider,
                currency,
                "FAIL",
                1,
                reason
        );

        System.out.println(
                "RECORDED FAIL: "
                        + provider
                        + " | "
                        + currency
                        + " | "
                        + reason
        );
    }

    // =========================================================
    // WRITE EXECUTION RESULT
    // =========================================================

    private static synchronized void
            writeExecutionResult(
                    String excelPath,
                    String provider,
                    String currency,
                    String status,
                    int attempt,
                    String message) {

        try (
                FileInputStream input =
                        new FileInputStream(
                                excelPath
                        );

                Workbook workbook =
                        new XSSFWorkbook(
                                input
                        )
        ) {

            Sheet sheet =
                    workbook.getSheet(
                            RESULT_SHEET
                    );

            if (sheet == null) {

                sheet =
                        workbook.createSheet(
                                RESULT_SHEET
                        );

                Row header =
                        sheet.createRow(0);

                header.createCell(0)
                        .setCellValue(
                                "Provider"
                        );

                header.createCell(1)
                        .setCellValue(
                                "Currency"
                        );

                header.createCell(2)
                        .setCellValue(
                                "Status"
                        );

                header.createCell(3)
                        .setCellValue(
                                "Attempt"
                        );

                header.createCell(4)
                        .setCellValue(
                                "Message"
                        );
            }

            int nextRow =
                    sheet.getLastRowNum() + 1;

            Row row =
                    sheet.createRow(
                            nextRow
                    );

            row.createCell(0)
                    .setCellValue(
                            provider
                    );

            row.createCell(1)
                    .setCellValue(
                            currency
                    );

            row.createCell(2)
                    .setCellValue(
                            status
                    );

            row.createCell(3)
                    .setCellValue(
                            attempt
                    );

            row.createCell(4)
                    .setCellValue(
                            message
                    );

            try (
                    FileOutputStream output =
                            new FileOutputStream(
                                    excelPath
                            )
            ) {

                workbook.write(
                        output
                );
            }

            System.out.println(
                    "RESULT WRITTEN: "
                            + provider
                            + " | "
                            + currency
                            + " | "
                            + status
            );

        } catch (Exception e) {

            System.out.println(
                    "ERROR WRITING RESULT TO EXCEL: "
                            + e.getMessage()
            );
        }
    }

    // =========================================================
    // SAFE STRING
    // =========================================================

    private static String safeTrim(
            String value) {

        if (value == null) {

            return "";
        }

        return value.trim();
    }

    // =========================================================
    // SAFE SLEEP
    // =========================================================

    private static void sleep(
            long milliseconds) {

        try {

            Thread.sleep(
                    milliseconds
            );

        } catch (InterruptedException e) {

            Thread.currentThread()
                    .interrupt();

            System.out.println(
                    "Thread interrupted while waiting."
            );
        }
    }

    // =========================================================
    // SAVE RESULT CLASS
    // =========================================================

    private static class SaveResult {

        private final String status;

        private final String message;

        private SaveResult(
                String status,
                String message) {

            this.status = status;

            this.message = message;
        }
    }
}