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
public class GameLauncher2 {

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

        // Initialize headers
        COMMON_HEADERS.put("merchantOpLink", MERCHANT_LINK);
        COMMON_HEADERS.put("merchantCode", MERCHANT_CODE);
        COMMON_HEADERS.put("Authorization", LOGIN_TOKEN);
        COMMON_HEADERS.put("ip-web", "{\"HTTP_CF_CONNECTING_IP\":\"194.169.170.42\",\"HTTP_CF_IPCOUNTRY\":\"SG\",\"HTTP_CLIENT_IP\":\"194.169.170.42\",\"REMOTE_ADDR\":\"194.169.170.42\",\"protocol\":\"http\"}");
        COMMON_HEADERS.put("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/139.0.0.0 Safari/537.36");
        COMMON_HEADERS.put("device", "desktop");
        COMMON_HEADERS.put("accept", "application/json");
        COMMON_HEADERS.put("accept-encoding", "gzip, deflate, br, zstd");
        COMMON_HEADERS.put("accept-language", "en-US,en;q=0.9");
        COMMON_HEADERS.put("origin", MERCHANT_LINK);
        COMMON_HEADERS.put("referer", MERCHANT_LINK);
        COMMON_HEADERS.put("sec-ch-ua", "\"Chromium\";v=\"140\", \"Not=A?Brand\";v=\"24\", \"Google Chrome\";v=\"140\"");
        COMMON_HEADERS.put("sec-ch-ua-mobile", "?0");
        COMMON_HEADERS.put("sec-ch-ua-platform", "\"Windows\"");
        COMMON_HEADERS.put("sec-fetch-dest", "empty");
        COMMON_HEADERS.put("sec-fetch-mode", "cors");
        COMMON_HEADERS.put("sec-fetch-site", "same-origin");

        RestAssured.config = RestAssured.config().httpClient(
                io.restassured.config.HttpClientConfig.httpClientConfig()
                        .setParam("http.connection.timeout", 20000)
                        .setParam("http.socket.timeout", 20000)
        );

        String username = "TESTACC";
        String password = "cFg5LAk7DBBWKCdPq3KTAA==";

        // Login once before starting
        Response loginResponse = performLogin(username, password);
        if (!isLoginSuccessful(loginResponse)) {
            throw new RuntimeException("Login failed: " + loginResponse.asString());
        }

        String userId = loginResponse.jsonPath().getString("data.uid");
        String token = loginResponse.jsonPath().getString("data.token");

        Map<String, String> loginCookies = loginResponse.getCookies();
        if (loginCookies != null && !loginCookies.isEmpty()) {
            COMMON_HEADERS.put("cookie", buildCookieString(loginCookies));
        }

        System.out.println("✅ Login Successful! UID: " + userId + ", Token: " + token);

        // Auto detect last completed row
        int startRow = findNextUnprocessedRow(sheet);
        System.out.println("▶️ Starting from row: " + (startRow + 1));

        processExcel(sheet, userId, token, username, password, startRow, workbook);

        try (FileOutputStream fos = new FileOutputStream(EXCEL_PATH)) {
            workbook.write(fos);
        }
        workbook.close();
        System.out.println("✅ Excel update completed.");
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
    if (response == null) {
        System.out.println("⚠️ Login response is null!");
        return false;
    }

    int statusCode = response.getStatusCode();
    String body = response.getBody() != null ? response.getBody().asString().trim() : "";

    // ✅ Check if response is JSON before parsing
    if (body.isEmpty() || (!body.startsWith("{") && !body.startsWith("["))) {
        System.out.println("⚠️ Login response is not JSON or empty. Status: " + statusCode);
        return false;
    }

    try {
        // Attempt JSON parse safely
        Boolean status = response.jsonPath().getBoolean("status");
        if (status != null && status) {
            return true;
        }

        // Some APIs return "true"/"false" as string
        String statusStr = response.jsonPath().getString("status");
        return "true".equalsIgnoreCase(statusStr) || "200".equalsIgnoreCase(statusStr);

    } catch (Exception e) {
        System.out.println("⚠️ Failed to parse login JSON: " + e.getMessage());
        return false;
    }
}


    private static void processExcel(Sheet sheet, String userId, String token, String username, String password, int startRow, Workbook workbook) {
        DataFormatter formatter = new DataFormatter();

        for (int i = startRow; i <= sheet.getLastRowNum(); i++) {

            Row row = sheet.getRow(i);
            if (row == null || row.getCell(0) == null || row.getCell(1) == null) continue;

            String providerIdStr = formatter.formatCellValue(row.getCell(0)).trim();
            String gameIdStr = formatter.formatCellValue(row.getCell(2)).trim();
            if (providerIdStr.isEmpty() || gameIdStr.isEmpty()) continue;

            // Auto-refresh token every 25 rows
            if (i > startRow && i % 25 == 0) {
                Response newLogin = performLogin(username, password);
                if (isLoginSuccessful(newLogin)) {
                    token = newLogin.jsonPath().getString("data.token");
                    COMMON_HEADERS.put("Authorization", token);
                    System.out.println("🔁 Token refreshed at row " + (i + 1));
                }
            }

            String language = LANG_COUNTRY.contains("-") ? LANG_COUNTRY.split("-")[0] : LANG_COUNTRY;
            int providerId = Integer.parseInt(providerIdStr);
            int gameId = Integer.parseInt(gameIdStr);

            Response response = launchGame(userId, token, CURRENCY, providerId, gameId, language, LANG_COUNTRY);
            boolean isRestricted = checkAccessRestriction(response);
            updateRowWithResponse(row, response, isRestricted);

            // ✅ Save progress after each row
            try (FileOutputStream fos = new FileOutputStream(EXCEL_PATH)) {
                workbook.write(fos);
            } catch (IOException e) {
                System.err.println("⚠️ Failed to save progress for row " + (i + 1) + ": " + e.getMessage());
            }

            System.out.println("✅ Row " + (i + 1) + " processed successfully.");
        }
    }

    private static int findNextUnprocessedRow(Sheet sheet) {
        for (int i = 10; i <= sheet.getLastRowNum(); i++) { // skip header config rows
            Row row = sheet.getRow(i);
            if (row == null) continue;
            Cell statusCell = row.getCell(4);
            if (statusCell == null || statusCell.toString().trim().isEmpty()) {
                return i;
            }
        }
        return 10;
    }

    private static Response launchGame(String userId, String token, String currency, int providerId, int gameId, String language, String langCountry) {
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
        if (body == null || body.isEmpty()) return false;

        try {
            String msg = response.jsonPath().getString("msg");
            if (msg != null && (msg.toLowerCase().contains("access restricted")
                    || msg.toLowerCase().contains("not allowed")
                    || msg.toLowerCase().contains("geo")
                    || msg.toLowerCase().contains("jurisdiction"))) {
                return true;
            }
        } catch (Exception e) {
            return body.toLowerCase().contains("restricted");
        }
        return false;
    }

    private static void updateRowWithResponse(Row row, Response response, boolean isRestricted) {
        String st = safeJsonGet(response, "status");
        String cdStr = safeJsonGet(response, "code");
        String msg = safeJsonGet(response, "msg");

        String url = "N/A";
        if ("true".equalsIgnoreCase(st)) {
            String dataUrl = safeJsonGet(response, "data.url");
            if (dataUrl != null) url = dataUrl;
        }

        writeCell(row, 4, st);
        writeCell(row, 5, cdStr);
        writeCell(row, 6, msg);
        writeCell(row, 7, url);
        writeCell(row, 8, isRestricted ? "ACCESS RESTRICTED" : "OK");
    }

    private static void writeCell(Row row, int colIndex, String value) {
        Cell cell = row.getCell(colIndex);
        if (cell == null) cell = row.createCell(colIndex);
        cell.setCellValue(value != null ? value : "null");
    }

    private static String safeJsonGet(Response response, String path) {
        try {
            if (response == null || response.getBody() == null) return null;
            String body = response.getBody().asString();
            if (body == null || body.isEmpty() || !body.trim().startsWith("{")) return null;
            return response.jsonPath().getString(path);
        } catch (Exception e) {
            return null;
        }
    }

    private static String buildCookieString(Map<String, String> cookies) {
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<String, String> e : cookies.entrySet()) {
            if (sb.length() > 0) sb.append("; ");
            sb.append(e.getKey()).append("=").append(e.getValue());
        }
        return sb.toString();
    }
}