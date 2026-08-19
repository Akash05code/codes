package com.framework;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.Select;

public class NotificationValidator {

    /*
     * Verify Provider
     */
    public static boolean verifyProvider(
            WebDriver driver,
            String expectedProvider) {

        try {

            WebElement providerDropdown =
                    driver.findElement(By.id("providerA"));

            Select selectProvider =
                    new Select(providerDropdown);

            String actualProvider =
                    selectProvider.getFirstSelectedOption()
                                  .getText()
                                  .trim();

            boolean result =
                    actualProvider.equals(expectedProvider.trim());

            System.out.println(
                    "Provider Validation | Expected: "
                    + expectedProvider
                    + " | Actual: "
                    + actualProvider
                    + " | Result: "
                    + (result ? "PASS" : "FAIL")
            );

            return result;

        } catch (Exception e) {

            System.out.println(
                    "Provider Validation FAILED: "
                    + e.getMessage()
            );

            return false;
        }
    }


    /*
     * Verify Start Date
     */
    public static boolean verifyStartDate(
            WebDriver driver,
            String expectedStartDate) {

        try {

            WebElement startDate =
                    driver.findElement(By.id("start_dateA"));

            String actualStartDate =
                    startDate.getAttribute("value").trim();

            boolean result =
                    actualStartDate.equals(expectedStartDate.trim());

            System.out.println(
                    "Start Date Validation | Expected: "
                    + expectedStartDate
                    + " | Actual: "
                    + actualStartDate
                    + " | Result: "
                    + (result ? "PASS" : "FAIL")
            );

            return result;

        } catch (Exception e) {

            System.out.println(
                    "Start Date Validation FAILED: "
                    + e.getMessage()
            );

            return false;
        }
    }


    /*
     * Verify End Date
     */
    public static boolean verifyEndDate(
            WebDriver driver,
            String expectedEndDate) {

        try {

            WebElement endDate =
                    driver.findElement(By.id("end_dateA"));

            String actualEndDate =
                    endDate.getAttribute("value").trim();

            boolean result =
                    actualEndDate.equals(expectedEndDate.trim());

            System.out.println(
                    "End Date Validation | Expected: "
                    + expectedEndDate
                    + " | Actual: "
                    + actualEndDate
                    + " | Result: "
                    + (result ? "PASS" : "FAIL")
            );

            return result;

        } catch (Exception e) {

            System.out.println(
                    "End Date Validation FAILED: "
                    + e.getMessage()
            );

            return false;
        }
    }


    /*
     * Verify Remark
     */
    public static boolean verifyRemark(
            WebDriver driver,
            String expectedRemark) {

        try {

            WebElement remark =
                    driver.findElement(By.id("remarkA"));

            String actualRemark =
                    remark.getAttribute("value").trim();

            boolean result =
                    actualRemark.equals(expectedRemark.trim());

            System.out.println(
                    "Remark Validation | Expected: "
                    + expectedRemark
                    + " | Actual: "
                    + actualRemark
                    + " | Result: "
                    + (result ? "PASS" : "FAIL")
            );

            return result;

        } catch (Exception e) {

            System.out.println(
                    "Remark Validation FAILED: "
                    + e.getMessage()
            );

            return false;
        }
    }


    /*
     * Verify ALL fields
     */
    public static boolean verifyAll(
            WebDriver driver,
            String expectedProvider,
            String expectedStartDate,
            String expectedEndDate,
            String expectedRemark) {

        System.out.println();
        System.out.println("========== VALIDATION START ==========");

        boolean providerValid =
                verifyProvider(driver, expectedProvider);

        boolean startDateValid =
                verifyStartDate(driver, expectedStartDate);

        boolean endDateValid =
                verifyEndDate(driver, expectedEndDate);

        boolean remarkValid =
                verifyRemark(driver, expectedRemark);

        boolean allValid =
                providerValid
                && startDateValid
                && endDateValid
                && remarkValid;

        System.out.println("--------------------------------------");

        if (allValid) {

            System.out.println(
                    "ALL VALIDATIONS PASSED"
            );

        } else {

            System.out.println(
                    "VALIDATION FAILED - SAVE WILL NOT BE CLICKED"
            );
        }

        System.out.println("========== VALIDATION END ============");
        System.out.println();

        return allValid;
    }
}