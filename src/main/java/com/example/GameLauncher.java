package com.example;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.*;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;
 
public class GameLauncher {

    private static String LOGIN_URL;
    private static String GAME_URL;
    private static String MERCHANT_LINK;
    private static String MERCHANT_CODE;
    private static String LOGIN_TOKEN;
    private static String CURRENCY;
    private static String LANG_COUNTRY;

    private static final String EXCEL_PATH = "C:\\Users\\User\\Desktop\\vs code\\.vscode\\selenium\\game_result.xlsx";

    private static final Map<String, String> COMMON_HEADERS = new HashMap<>();

    public static void main(String[] args) throws IOException {

        FileInputStream fis = new FileInputStream(EXCEL_PATH);
        Workbook workbook = new XSSFWorkbook(fis);
        fis.close();

        Scanner sc = new Scanner(System.in);
        System.out.print("Enter the Merchant name to test: ");
        String sheetName = sc.nextLine().trim();
        sc.close();

        Sheet sheet = workbook.getSheet(sheetName);
        if (sheet == null) {
            workbook.close();
            throw new RuntimeException("Sheet \"" + sheetName + "\" not found in Excel file!");
        }

        readConfig(sheet);

        COMMON_HEADERS.put("merchantOpLink", MERCHANT_LINK);
        COMMON_HEADERS.put("merchantCode", MERCHANT_CODE);
        COMMON_HEADERS.put("Authorization", LOGIN_TOKEN);
        COMMON_HEADERS.put("ip-web", "{\"HTTP_CF_CONNECTING_IP\":\"194.169.170.42\",\"HTTP_CF_IPCOUNTRY\":\"SG\",\"HTTP_CLIENT_IP\":\"194.169.170.42\",\"HTTP_FORWARDED\":\"\",\"HTTP_X_FORWARDED\":\"\",\"HTTP_FORWARDED_FOR\":\"194.169.170.42\",\"HTTP_X_FORWARDED_FOR\":\"194.169.170.42\",\"REMOTE_ADDR\":\"194.169.170.42\",\"protocol\":\"http\"}");
        COMMON_HEADERS.put("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/139.0.0.0 Safari/537.36");
        COMMON_HEADERS.put("device", "desktop");

        RestAssured.config = RestAssured.config().httpClient(
                io.restassured.config.HttpClientConfig.httpClientConfig()
                        .setParam("http.connection.timeout", 20000)
                        .setParam("http.socket.timeout", 20000)
        );

        String username = "TESTACC";
        String password = "cFg5LAk7DBBWKCdPq3KTAA==";

        Response loginResponse = performLogin(username, password);
        if (!isLoginSuccessful(loginResponse)) {
            throw new RuntimeException("Login failed: " + loginResponse.asString());
        }

        String userId = loginResponse.jsonPath().getString("data.uid");
        String token = loginResponse.jsonPath().getString("data.token");

        System.out.println("Login Successful! UID: " + userId + ", Token: " + token);

        processExcel(sheet, userId, token);

        try (FileOutputStream fos = new FileOutputStream(EXCEL_PATH)) {
            workbook.write(fos);
        }
        workbook.close();
        System.out.println("Excel update completed.");
    }

    private static void readConfig(Sheet sheet) {
        DataFormatter formatter = new DataFormatter();

        MERCHANT_LINK = formatter.formatCellValue(sheet.getRow(0).getCell(1)).trim();
        LOGIN_URL = formatter.formatCellValue(sheet.getRow(1).getCell(1)).trim();
        CURRENCY = formatter.formatCellValue(sheet.getRow(2).getCell(1)).trim();
        LANG_COUNTRY = formatter.formatCellValue(sheet.getRow(3).getCell(1)).trim();
        GAME_URL = formatter.formatCellValue(sheet.getRow(4).getCell(1)).trim();
        MERCHANT_CODE = formatter.formatCellValue(sheet.getRow(5).getCell(1)).trim();
        LOGIN_TOKEN = formatter.formatCellValue(sheet.getRow(6).getCell(1)).trim();
    }

    private static Response performLogin(String username, String password) {
        return RestAssured.given()
                .contentType(ContentType.JSON)
                .headers(COMMON_HEADERS)
                .queryParam("username", username)
                .queryParam("password", password)
                .queryParam("merchantOpLink", MERCHANT_LINK)
                .queryParam("mobile_check", "1")
                .queryParam("language", "en")
                .queryParam("langCountry", LANG_COUNTRY)
                .queryParam("has_encrypt", "1")
                .queryParam("merchant_id", "1")
                .post(LOGIN_URL);
    }

    private static boolean isLoginSuccessful(Response response) {
        return response.statusCode() == 200 && Boolean.TRUE.equals(response.jsonPath().getBoolean("status"));
    }

    private static void processExcel(Sheet sheet, String userId, String token) {
        DataFormatter formatter = new DataFormatter();

     
int startRow = 8; //change if you want to start from different
for (int i = startRow; i <= sheet.getLastRowNum(); i++) {

            Row row = sheet.getRow(i);

            if (row == null || row.getCell(0) == null || row.getCell(1) == null) {
                System.out.println("Skipping row " + (i + 1) + " because row or cells are empty.");
                continue;
            }

            String providerIdStr = formatter.formatCellValue(row.getCell(0)).trim();
            String gameIdStr = formatter.formatCellValue(row.getCell(2)).trim();

            if (providerIdStr.isEmpty() || gameIdStr.isEmpty()) {
                System.out.println("Skipping row " + (i + 1) + " due to missing fields");
                continue;
            }

            String language = LANG_COUNTRY.contains("-") ? LANG_COUNTRY.split("-")[0] : LANG_COUNTRY;

            int providerId = Integer.parseInt(providerIdStr);
            int gameId = Integer.parseInt(gameIdStr);

            Response response = launchGame(userId, token, CURRENCY, providerId, gameId, language, LANG_COUNTRY);

            boolean isRestricted = checkAccessRestriction(response);

      updateRowWithResponse(row, response, isRestricted);

String st = safeJsonGet(response, "status");
String cd = safeJsonGet(response, "code");
String mg = safeJsonGet(response, "msg");
String validationResult = getCellValue(row, 8);
String urlValue = getCellValue(row, 7);

System.out.println("\n------------------------------");
System.out.println("✅ Game Test Completed - Row " + (i + 1));
System.out.println("Provider ID : " + formatter.formatCellValue(row.getCell(0)));
System.out.println("Game ID     : " + formatter.formatCellValue(row.getCell(2)));
System.out.println("Status      : " + (st != null ? st : "N/A"));
System.out.println("Code        : " + (cd != null ? cd : "N/A"));
System.out.println("Message     : " + (mg != null ? mg : "N/A"));
System.out.println("Restricted  : " + isRestricted);
System.out.println("Game URL    : " + urlValue);
System.out.println("Result      : " + validationResult);
System.out.println("------------------------------\n");

try (FileOutputStream fos = new FileOutputStream(EXCEL_PATH)) {
    sheet.getWorkbook().write(fos);
} catch (IOException e) {
    System.err.println("Failed to save Excel progress for row " + (i + 1) + ": " + e.getMessage());
}
        }
    }

    private static Response launchGame(String userId, String token, String currency, int providerId, int gameId,
                                       String language, String langCountry) {
        return RestAssured.given()
                .contentType(ContentType.JSON)
                .headers(COMMON_HEADERS)
                .queryParam("currency", currency)
                .queryParam("provider_id", providerId)
                .queryParam("game_id", gameId)
                .queryParam("language", language)
                .queryParam("langCountry", langCountry)
                .queryParam("merchant_id", "1")
                .queryParam("uid", userId)
                .queryParam("token", token)
                .post(GAME_URL);
    }

    private static boolean checkAccessRestriction(Response response) {
        String body = response.asString();
        if (body == null || body.isEmpty()) {
            return false;
        }

        try {
            String msg = response.jsonPath().getString("msg");
            if (msg != null && (msg.toLowerCase().contains("access restricted")
                    || msg.toLowerCase().contains("not allowed")
                    || msg.toLowerCase().contains("geo")
                    || msg.toLowerCase().contains("something went wrong")
                    || msg.toLowerCase().contains("maintenance"))) {
                return true;
            }
        } catch (Exception e) {
            String lower = body.toLowerCase();
            if (lower.contains("restricted")
                    || lower.contains("not allowed")
                    || lower.contains("jurisdiction")
                    || lower.contains("geo")
                    || lower.contains("maintenance")
                    || lower.contains("something went wrong")) {
                return true;
            }
        }
        return false;
    }

    private static void updateRowWithResponse(Row row, Response response, boolean isRestricted) {
        String st = safeJsonGet(response, "status");
        String cdStr = safeJsonGet(response, "code");
        String msg = safeJsonGet(response, "msg");

        Boolean status = null;
        Integer code = null;

        if (st != null) {
            status = Boolean.valueOf(st);
        }

        if (cdStr != null) {
            try {
                code = Integer.valueOf(cdStr);
            } catch (NumberFormatException ignored) {
                code = null;
            }
        }

        String url = "N/A";
        if (Boolean.TRUE.equals(status)) {
            String dataUrl = safeJsonGet(response, "data.url");
            if (dataUrl != null) url = dataUrl;
        }

        // write base columns
        writeCell(row, 4, status != null ? status.toString() : "null");
        writeCell(row, 5, code != null ? code.toString() : "null");
        writeCell(row, 6, msg != null ? msg : "null");
        writeCell(row, 7, url);

        if (code != null && code != 200) {
            writeCell(row, 8, "SKIPPED-Not opening(" + code + ")");
            return;
        }

       
        if (isRestricted) {
            writeCell(row, 8, "ACCESS RESTRICTED");
        } else if (!"N/A".equals(url)) {
            String validation = validateGameUrl(url);
            writeCell(row, 8, validation);
        } else {
            writeCell(row, 8, "NO URL");
        }

       
        String restrictionVal = getCellValue(row, 8);
        String msgVal = getCellValue(row, 6);
        if ("ACCESS RESTRICTED".equalsIgnoreCase(restrictionVal) ||
                (msgVal != null && msgVal.toLowerCase().contains("something went wrong"))) {

            CellStyle redStyle = row.getSheet().getWorkbook().createCellStyle();
            Font font = row.getSheet().getWorkbook().createFont();
            font.setColor(IndexedColors.RED.getIndex());
            redStyle.setFont(font);

            for (int c = 0; c <= 8; c++) {
                Cell cell = row.getCell(c);
                if (cell != null) {
                    cell.setCellStyle(redStyle);
                }
            }
        }
    }

    private static void writeCell(Row row, int colIndex, String value) {
        Cell cell = row.getCell(colIndex);
        if (cell == null) cell = row.createCell(colIndex);
        cell.setCellValue(value);
    }

    private static String getCellValue(Row row, int colIndex) {
        Cell cell = row.getCell(colIndex);
        return (cell != null) ? cell.toString() : "";
    }

    private static String safeJsonGet(Response response, String path) {
        try {
            if (response == null || response.getBody() == null) {
                return null;
            }
            String body = response.getBody().asString();
            if (body == null || body.isEmpty() || !body.trim().startsWith("{")) {
                // Not JSON
                return null;
            }
            return response.jsonPath().getString(path);
        } catch (Exception e) {
            return null;
        }
    }

    private static String validateGameUrl(String gameUrl) {
        try {
            Response pageResponse = RestAssured.given().get(gameUrl);
            String html = pageResponse.getBody().asString().toLowerCase();

            if (html.length() < 500) {
                return "POSSIBLY BROKEN(working fine -loading late)";
            }

            if (containsRestriction(html)) {
                return "ACCESS RESTRICTED";
            }

            if (html.contains("<iframe") || html.contains("<canvas") || html.contains("<script")) {
                return "GOOD";
            }

            return "UNKNOWN(Access restriction-found)";
        } catch (Exception e) {
            return "ERROR CHECKING URL-pop-up error ";
        }
    }

    private static boolean containsRestriction(String html) {
        return html.contains("restricted")
                || html.contains("blocked")
                || html.contains("jurisdiction")
                || html.contains("not available")
                || html.contains("sorry the game is not available")
                || html.contains("geo")
                || html.contains("maintenance");
    }
}