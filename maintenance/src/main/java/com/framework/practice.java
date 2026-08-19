package com.framework;

import java.time.Duration;
import java.util.List;

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
    // WAIT SETTINGS
    // =========================================================

    // Keep your normal wait smaller so a failed currency
    // does not take 50 seconds unnecessarily.
    private static final int NORMAL_WAIT = 20;

    // Modal close/backdrop wait.
    private static final int MODAL_WAIT = 8;

    // Save/toast can take longer because backend processing
    // may be delayed.
    private static final int SAVE_WAIT = 30;

    // =========================================================
    // MODAL LOCATORS
    // =========================================================

    /*
     * Exact Cancel element from your modal HTML:
     *
     * <a href="javascript:void(0);"
     *    class="btn btn-default mgr10"
     *    data-dismiss="modal">
     *    Cancel
     * </a>
     *
     * We scope it to a visible Bootstrap modal so another
     * Cancel button elsewhere does not get selected.
     */
    private static final By MODAL_CANCEL =
            By.xpath(
                    "//div[contains(@class,'modal') and contains(@class,'in')]"
                  + "//a[@data-dismiss='modal'"
                  + " and contains(@class,'btn-default')"
                  + " and normalize-space()='Cancel']"
            );

    // =========================================================
    // MAIN
    // =========================================================

    public static void main(String[] args) throws InterruptedException {

        // =========================================================
        // 1. READ EXCEL
        // =========================================================

        String excelPath =
                "C:\\Users\\Akashkumar\\Desktop\\NotificationData.xlsx";

        List<NotificationData> notificationList =
                ExcelReader.readNotificationData(excelPath);

        System.out.println(
                "Total Rows : " + notificationList.size()
        );

        // =========================================================
        // 2. START FROM CURRENCY INDEX
        // =========================================================

        int startFromIndex = 1;

        /*
         * THB=1
         * BDT=2
         * INR=3
         * PKR=4
         * SGD=5
         * AUD=6
         * HKD=7
         * IDR=8
         * MYR=9
         * NPR=10
         * PHP=11
         * MMK=12
         * USD=13
         * VND=14
         * LAK=15
         * TWD=16
         * PGK=17
         */

        // =========================================================
        // 3. START BROWSER
        // =========================================================

        WebDriver driver = new ChromeDriver();

        driver.manage().window().maximize();

        WebDriverWait wait =
                new WebDriverWait(
                        driver,
                        Duration.ofSeconds(NORMAL_WAIT)
                );

        JavascriptExecutor js =
                (JavascriptExecutor) driver;

        driver.manage().timeouts()
                .implicitlyWait(Duration.ofSeconds(5));

        try {

            // =====================================================
            // 4. OPEN APPLICATION
            // =====================================================

            driver.get(
                    "https://admin-centerbo.ibstest.site/system-notification"
            );

            // =====================================================
            // 5. LOGIN
            // =====================================================

            driver.findElement(By.id("mer_code"))
                    .sendKeys("masterbo");

            driver.findElement(By.id("username"))
                    .sendKeys("mbproviderteam");

            driver.findElement(By.id("password"))
                    .sendKeys("asdf1234");

            String captcha =
                    driver.findElement(
                            By.xpath(
                                    "//span[@class='input-group-addon captchaNum']"
                            )
                    ).getText();

            driver.findElement(By.id("captcha"))
                    .sendKeys(captcha);

            driver.findElement(By.id("btnLogin"))
                    .click();

            // =====================================================
            // 6. WAIT FOR LOGIN
            // =====================================================

            wait.until(
                    ExpectedConditions.presenceOfElementLocated(
                            By.xpath("//span[contains(text(),'System')]")
                    )
            );

            // =====================================================
            // 7. OPEN SYSTEM NOTIFICATION
            // =====================================================

            WebElement notification =
                    driver.findElement(
                            By.xpath(
                                    "//span[contains(text(),'3.1 System Notification')]"
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

            // =====================================================
            // 8. LOOP THROUGH EXCEL PROVIDERS
            // =====================================================

            for (NotificationData data : notificationList) {

                // -------------------------------------------------
                // Read provider safely
                // -------------------------------------------------

                String desiredProvider =
                        data.getProvider() == null
                                ? ""
                                : data.getProvider().trim();

                /*
                 * Prevent an empty Excel row from starting another
                 * useless provider cycle.
                 */
                if (desiredProvider.isEmpty()) {

                    System.out.println(
                            "Blank provider row found in Excel. "
                          + "Skipping this row."
                    );

                    continue;
                }

                String startDateValue =
                        data.getStartDate() == null
                                ? ""
                                : data.getStartDate().trim();

                String endDateValue =
                        data.getEndDate() == null
                                ? ""
                                : data.getEndDate().trim();

                String remarkValue =
                        data.getRemark() == null
                                ? ""
                                : data.getRemark().trim();

                System.out.println();
                System.out.println(
                        "=============================================="
                );

                System.out.println(
                        "STARTING PROVIDER"
                );

                System.out.println(
                        "Provider : " + desiredProvider
                );

                System.out.println(
                        "Start    : " + startDateValue
                );

                System.out.println(
                        "End      : " + endDateValue
                );

                System.out.println(
                        "Remark   : " + remarkValue
                );

                System.out.println(
                        "=============================================="
                );

                // =================================================
                // 9. OPEN ADD NOTIFICATION FOR THIS PROVIDER
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
                // 10. GET CURRENCY LIST
                // =================================================

                WebElement currencyDropdown =
                        wait.until(
                                ExpectedConditions
                                        .visibilityOfElementLocated(
                                                By.id("currencyA")
                                        )
                        );

                Select selectCurrency =
                        new Select(currencyDropdown);

                List<WebElement> currencies =
                        selectCurrency.getOptions();

                // =================================================
                // 11. LOOP THROUGH EVERY CURRENCY
                // =================================================

                for (int i = 1;
                     i < currencies.size();
                     i++) {

                    if (i < startFromIndex) {

                        System.out.println(
                                "Skipping currency index: "
                                        + i
                        );

                        continue;
                    }

                    String currencyName =
                            currencies.get(i)
                                    .getText()
                                    .trim();

                    if (currencyName.isEmpty()) {

                        currencyName =
                                currencies.get(i)
                                        .getAttribute("value")
                                        .trim();
                    }

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
                            "=============================================="
                    );

                    boolean currencySuccessful =
                            false;

                    try {

                        // =================================================
                        // 12. SELECT CURRENCY
                        // =================================================

                        currencyDropdown =
                                wait.until(
                                        ExpectedConditions
                                                .visibilityOfElementLocated(
                                                        By.id("currencyA")
                                                )
                                );

                        selectCurrency =
                                new Select(currencyDropdown);

                        selectCurrency.selectByIndex(i);

                        System.out.println(
                                "Currency selected: "
                                        + currencyName
                        );

                        Thread.sleep(500);

                        // =================================================
                        // 13. VERIFY CURRENCY
                        // =================================================

                        String actualCurrency =
                                new Select(
                                        wait.until(
                                                ExpectedConditions
                                                        .visibilityOfElementLocated(
                                                                By.id("currencyA")
                                                        )
                                        )
                                )
                                        .getFirstSelectedOption()
                                        .getText()
                                        .trim();

                        if (!actualCurrency.equals(
                                currencyName)) {

                            System.out.println(
                                    "CURRENCY VALIDATION FAILED"
                            );

                            System.out.println(
                                    "Expected : "
                                            + currencyName
                            );

                            System.out.println(
                                    "Actual   : "
                                            + actualCurrency
                            );

                            // Close current modal immediately.
                            closeModalUsingCancel(
                                    driver,
                                    wait,
                                    js
                            );

                            // Reopen only if this is NOT the last currency.
                            if (i < currencies.size() - 1) {

                                if (!clickAddNotification(
                                        driver,
                                        wait,
                                        js
                                )) {
                                    break;
                                }
                            }

                            continue;
                        }

                        System.out.println(
                                "Currency validation PASS"
                        );

                        // =================================================
                        // 14. SELECT ALL MERCHANTS
                        // =================================================

                        /*
                         * IMPORTANT:
                         * After each currency change, the application
                         * can rebuild merchantList.
                         *
                         * Therefore we find it AGAIN every time.
                         */

                        boolean merchantsSelected =
                                selectAllMerchants(
                                        driver,
                                        wait,
                                        js,
                                        currencyName
                                );

                        if (!merchantsSelected) {

                            System.out.println(
                                    "Merchant selection failed for: "
                                            + currencyName
                            );

                            closeModalUsingCancel(
                                    driver,
                                    wait,
                                    js
                            );

                            if (i < currencies.size() - 1) {

                                if (!clickAddNotification(
                                        driver,
                                        wait,
                                        js
                                )) {
                                    break;
                                }
                            }

                            continue;
                        }

                        // =================================================
                        // 15. SELECT PROVIDER
                        // =================================================

                        WebElement providerDropdown =
                                null;

                        boolean providerSelected =
                                false;

                        for (int retry = 0;
                             retry < 5;
                             retry++) {

                            try {

                                providerDropdown =
                                        wait.until(
                                                ExpectedConditions
                                                        .visibilityOfElementLocated(
                                                                By.id("providerA")
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
                                                        opt ->
                                                                opt.getText()
                                                                        .trim()
                                                                        .equals(
                                                                                desiredProvider
                                                                        )
                                                );

                                if (!providerExists) {

                                    System.out.println(
                                            "Provider NOT AVAILABLE"
                                    );

                                    System.out.println(
                                            "Provider : "
                                                    + desiredProvider
                                    );

                                    System.out.println(
                                            "Currency : "
                                                    + currencyName
                                    );

                                    break;
                                }

                                selectProvider
                                        .selectByVisibleText(
                                                desiredProvider
                                        );

                                // =================================================
                                // 16. VERIFY PROVIDER
                                // =================================================

                                String actualProvider =
                                        new Select(
                                                wait.until(
                                                        ExpectedConditions
                                                                .visibilityOfElementLocated(
                                                                        By.id("providerA")
                                                                )
                                                )
                                        )
                                                .getFirstSelectedOption()
                                                .getText()
                                                .trim();

                                if (!actualProvider.equals(
                                        desiredProvider)) {

                                    System.out.println(
                                            "PROVIDER VALIDATION FAILED"
                                    );

                                    break;
                                }

                                System.out.println(
                                        "Provider selected successfully: "
                                                + actualProvider
                                );

                                providerSelected = true;

                                break;

                            } catch (
                                    StaleElementReferenceException e) {

                                System.out.println(
                                        "Provider dropdown stale. Retry: "
                                                + (retry + 1)
                                );

                                Thread.sleep(300);

                            } catch (Exception e) {

                                System.out.println(
                                        "Provider not ready. Retry: "
                                                + (retry + 1)
                                );

                                Thread.sleep(300);
                            }
                        }

                        // =================================================
                        // PROVIDER NOT AVAILABLE / FAILED
                        // =================================================

                        if (!providerSelected) {

                            System.out.println(
                                    "Skipping currency because provider "
                                  + "could not be selected."
                            );

                            closeModalUsingCancel(
                                    driver,
                                    wait,
                                    js
                            );

                            if (i < currencies.size() - 1) {

                                if (!clickAddNotification(
                                        driver,
                                        wait,
                                        js
                                )) {

                                    break;
                                }
                            }

                            continue;
                        }

                        // =================================================
                        // 17. ENTER START DATE
                        // =================================================

                        WebElement startDate =
                                wait.until(
                                        ExpectedConditions
                                                .visibilityOfElementLocated(
                                                        By.id("start_dateA")
                                                )
                                );

                        startDate.clear();

                        startDate.sendKeys(
                                startDateValue
                        );

                        // =================================================
                        // 18. VERIFY START DATE
                        // =================================================

                        String actualStartDate =
                                startDate
                                        .getAttribute("value")
                                        .trim();

                        if (!actualStartDate.equals(
                                startDateValue)) {

                            System.out.println(
                                    "START DATE VALIDATION FAILED"
                            );

                            closeModalUsingCancel(
                                    driver,
                                    wait,
                                    js
                            );

                            if (i < currencies.size() - 1) {

                                if (!clickAddNotification(
                                        driver,
                                        wait,
                                        js
                                )) {
                                    break;
                                }
                            }

                            continue;
                        }

                        System.out.println(
                                "Start date validation PASS"
                        );

                        // =================================================
                        // 19. ENTER END DATE
                        // =================================================

                        WebElement endDate =
                                wait.until(
                                        ExpectedConditions
                                                .visibilityOfElementLocated(
                                                        By.id("end_dateA")
                                                )
                                );

                        endDate.clear();

                        endDate.sendKeys(
                                endDateValue
                        );

                        // =================================================
                        // 20. VERIFY END DATE
                        // =================================================

                        String actualEndDate =
                                endDate
                                        .getAttribute("value")
                                        .trim();

                        if (!actualEndDate.equals(
                                endDateValue)) {

                            System.out.println(
                                    "END DATE VALIDATION FAILED"
                            );

                            closeModalUsingCancel(
                                    driver,
                                    wait,
                                    js
                            );

                            if (i < currencies.size() - 1) {

                                if (!clickAddNotification(
                                        driver,
                                        wait,
                                        js
                                )) {
                                    break;
                                }
                            }

                            continue;
                        }

                        System.out.println(
                                "End date validation PASS"
                        );

                        // =================================================
                        // 21. ENTER REMARK
                        // =================================================

                        WebElement remark =
                                wait.until(
                                        ExpectedConditions
                                                .visibilityOfElementLocated(
                                                        By.id("remarkA")
                                                )
                                );

                        remark.clear();

                        remark.sendKeys(
                                remarkValue
                        );

                        // =================================================
                        // 22. VERIFY REMARK
                        // =================================================

                        String actualRemark =
                                remark
                                        .getAttribute("value")
                                        .trim();

                        if (!actualRemark.equals(
                                remarkValue)) {

                            System.out.println(
                                    "REMARK VALIDATION FAILED"
                            );

                            closeModalUsingCancel(
                                    driver,
                                    wait,
                                    js
                            );

                            if (i < currencies.size() - 1) {

                                if (!clickAddNotification(
                                        driver,
                                        wait,
                                        js
                                )) {
                                    break;
                                }
                            }

                            continue;
                        }

                        System.out.println(
                                "Remark validation PASS"
                        );

                        // =================================================
                        // 23. FINAL VALIDATION
                        // =================================================

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

                        // =================================================
                        // 24. DO NOT SAVE IF VALIDATION FAILED
                        // =================================================

                        if (!validationPassed) {

                            System.out.println(
                                    "DO NOT SAVE - VALIDATION FAILED"
                            );

                            closeModalUsingCancel(
                                    driver,
                                    wait,
                                    js
                            );

                            if (i < currencies.size() - 1) {

                                if (!clickAddNotification(
                                        driver,
                                        wait,
                                        js
                                )) {
                                    break;
                                }
                            }

                            continue;
                        }

                        System.out.println(
                                "ALL VALIDATIONS PASSED"
                        );

                        System.out.println(
                                "SAFE TO SAVE"
                        );

                        // =================================================
                        // 25. CLICK SAVE
                        // =================================================

                        WebElement saveButton =
                                wait.until(
                                        ExpectedConditions
                                                .elementToBeClickable(
                                                        By.id("btnAdd")
                                                )
                                );

                        js.executeScript(
                                "arguments[0].scrollIntoView({block:'center'});",
                                saveButton
                        );

                        System.out.println(
                                "Save button found."
                        );

                        saveButton.click();

                        System.out.println(
                                "Save button clicked."
                        );

                        // =================================================
                        // 26. VERIFY SUCCESS TOAST
                        // =================================================

                        try {

                            WebElement successMessage =
                                    new WebDriverWait(
                                            driver,
                                            Duration.ofSeconds(
                                                    SAVE_WAIT
                                            )
                                    ).until(
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

                                currencySuccessful = true;

                                System.out.println();
                                System.out.println(
                                        "======================================"
                                );

                                System.out.println(
                                        "SUBMIT SUCCESSFUL"
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
                                        "======================================"
                                );

                            } else {

                                System.out.println(
                                        "SUBMIT FAILED"
                                );

                                System.out.println(
                                        "Unexpected toast message: "
                                                + message
                                );
                            }

                        } catch (TimeoutException e) {

                            /*
                             * We do NOT call this a confirmed failure
                             * just because the toast was delayed.
                             *
                             * The form may already have been submitted.
                             */
                            System.out.println();
                            System.out.println(
                                    "SUCCESS TOAST NOT RECEIVED "
                                  + "WITHIN "
                                  + SAVE_WAIT
                                  + " SECONDS."
                            );

                            System.out.println(
                                    "Will close the modal and continue "
                                  + "without claiming definite failure."
                            );
                        }

                        // =================================================
                        // 27. CLOSE CURRENT MODAL USING CANCEL
                        // =================================================

                        closeModalUsingCancel(
                                driver,
                                wait,
                                js
                        );

                        // =================================================
                        // 28. ONLY REOPEN IF ANOTHER CURRENCY EXISTS
                        // =================================================

                        if (i < currencies.size() - 1) {

                            System.out.println(
                                    "Preparing next currency..."
                            );

                            if (!clickAddNotification(
                                    driver,
                                    wait,
                                    js
                            )) {

                                System.out.println(
                                        "Could not reopen Add Notification "
                                      + "for next currency."
                                );

                                break;
                            }

                            System.out.println(
                                    "Add Notification reopened."
                            );

                        } else {

                            /*
                             * THIS IS THE LAST CURRENCY FOR THIS PROVIDER.
                             *
                             * Do NOT reopen.
                             */
                            System.out.println(
                                    "Last currency completed for provider: "
                                            + desiredProvider
                            );

                            System.out.println(
                                    "Add Notification will NOT be reopened."
                            );
                        }

                    } catch (Exception e) {

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

                        /*
                         * IMPORTANT:
                         *
                         * Don't wait 50 seconds here.
                         * Explicitly click Cancel.
                         */
                        closeModalUsingCancel(
                                driver,
                                wait,
                                js
                        );

                        if (i < currencies.size() - 1) {

                            if (!clickAddNotification(
                                    driver,
                                    wait,
                                    js
                            )) {

                                System.out.println(
                                        "Could not recover for next currency."
                                );

                                break;
                            }
                        }
                    }
                }

                // =========================================================
                // 29. PROVIDER COMPLETED
                // =========================================================

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

            // =========================================================
            // 30. ALL PROVIDERS COMPLETED
            // =========================================================

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

            // =========================================================
            // CLOSE BROWSER ONLY ONCE
            // =========================================================

            driver.quit();
        }
    }

    // =============================================================
    // SELECT ALL MERCHANTS
    // =============================================================

    private static boolean selectAllMerchants(
            WebDriver driver,
            WebDriverWait wait,
            JavascriptExecutor js,
            String currencyName) {

        try {

            System.out.println(
                    "Waiting for merchants to load..."
            );

            /*
             * Find merchantList after EVERY currency selection.
             * The application can rebuild this element.
             */
            WebElement merchantSelect =
                    wait.until(
                            ExpectedConditions.presenceOfElementLocated(
                                    By.id("merchantList")
                            )
                    );

            /*
             * Wait until at least one merchant option exists.
             */
            wait.until(driver1 -> {

                try {

                    WebElement merchant =
                            driver1.findElement(
                                    By.id("merchantList")
                            );

                    Select merchantDropdown =
                            new Select(merchant);

                    return merchantDropdown
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
             * Find merchantList AGAIN because AJAX may have
             * replaced the original element.
             */
            merchantSelect =
                    wait.until(
                            ExpectedConditions.visibilityOfElementLocated(
                                    By.id("merchantList")
                            )
                    );

            /*
             * Select every merchant.
             */
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
                    """,
                    merchantSelect
            );

            /*
             * Verify every merchant is selected.
             */
            wait.until(driver1 -> {

                try {

                    WebElement merchant =
                            driver1.findElement(
                                    By.id("merchantList")
                            );

                    Select merchantDropdown =
                            new Select(merchant);

                    List<WebElement> options =
                            merchantDropdown.getOptions();

                    if (options.isEmpty()) {

                        return false;
                    }

                    for (WebElement option : options) {

                        if (!option.isSelected()) {

                            return false;
                        }
                    }

                    return true;

                } catch (Exception e) {

                    return false;
                }
            });

            System.out.println(
                    "----- All merchants selected successfully "
                  + "for "
                  + currencyName
                  + " -----"
            );

            return true;

        } catch (TimeoutException e) {

            System.out.println(
                    "Merchant list/options did not load for: "
                            + currencyName
            );

            return false;

        } catch (Exception e) {

            System.out.println(
                    "Merchant selection failed for "
                            + currencyName
                            + ": "
                            + e.getMessage()
            );

            return false;
        }
    }

    // =============================================================
    // CLOSE MODAL USING ACTUAL CANCEL BUTTON
    // =============================================================

    private static void closeModalUsingCancel(
            WebDriver driver,
            WebDriverWait wait,
            JavascriptExecutor js) {

        try {

            List<WebElement> cancelButtons =
                    driver.findElements(MODAL_CANCEL);

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

                } catch (StaleElementReferenceException ignored) {
                }
            }

            if (!clicked) {

                /*
                 * Modal may already be closed.
                 */
                System.out.println(
                        "No visible modal Cancel button found."
                );
            }

            /*
             * Wait only a short time for Bootstrap backdrop.
             */
            try {

                new WebDriverWait(
                        driver,
                        Duration.ofSeconds(MODAL_WAIT)
                ).until(
                        ExpectedConditions
                                .invisibilityOfElementLocated(
                                        By.cssSelector(
                                                ".modal-backdrop"
                                        )
                                )
                );

            } catch (TimeoutException e) {

                System.out.println(
                        "Modal backdrop still present after "
                                + MODAL_WAIT
                                + " seconds."
                );
            }

        } catch (Exception e) {

            System.out.println(
                    "Error closing modal: "
                            + e.getMessage()
            );
        }
    }

    // =============================================================
    // CLICK ADD NOTIFICATION
    // =============================================================

    private static boolean clickAddNotification(
            WebDriver driver,
            WebDriverWait wait,
            JavascriptExecutor js) {

        try {

            /*
             * First ensure old backdrop is gone.
             */
            try {

                new WebDriverWait(
                        driver,
                        Duration.ofSeconds(MODAL_WAIT)
                ).until(
                        ExpectedConditions
                                .invisibilityOfElementLocated(
                                        By.cssSelector(
                                                ".modal-backdrop"
                                        )
                                )
                );

            } catch (TimeoutException e) {

                /*
                 * If it still exists, actively click Cancel.
                 */
                closeModalUsingCancel(
                        driver,
                        wait,
                        js
                );
            }

            WebElement addButton =
                    wait.until(
                            ExpectedConditions.presenceOfElementLocated(
                                    By.id("btnAddNew")
                            )
                    );

            js.executeScript(
                    "arguments[0].scrollIntoView({block:'center'});",
                    addButton
            );

            try {

                wait.until(
                        ExpectedConditions.elementToBeClickable(
                                By.id("btnAddNew")
                        )
                ).click();

            } catch (Exception e) {

                System.out.println(
                        "Normal Add Notification click failed."
                );

                System.out.println(
                        "Using JavaScript click."
                );

                addButton =
                        wait.until(
                                ExpectedConditions.presenceOfElementLocated(
                                        By.id("btnAddNew")
                                )
                        );

                js.executeScript(
                        "arguments[0].click();",
                        addButton
                );
            }

            /*
             * currencyA is enough to prove the modal is open.
             *
             * We don't require providerA here because it may
             * be refreshed later based on the selected currency
             * and merchants.
             */
            wait.until(
                    ExpectedConditions.visibilityOfElementLocated(
                            By.id("currencyA")
                    )
            );

            System.out.println(
                    "Add Notification modal opened successfully."
            );

            return true;

        } catch (Exception e) {

            System.out.println(
                    "Failed to open Add Notification: "
                            + e.getMessage()
            );

            return false;
        }
    }
}