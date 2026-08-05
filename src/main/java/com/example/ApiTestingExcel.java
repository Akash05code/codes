package com.example;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.*;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Scanner;

public class ApiTestingExcel {

    public static void main(String[] args) throws IOException {
        Properties config = new Properties();
       try (FileInputStream confFis = new FileInputStream("C:\\Users\\User\\Desktop\\vs code\\.vscode\\selenium\\config.properties")) {
            config.load(confFis);
        } catch (IOException e) {
            System.out.println("Could not load config.properties: " + e.getMessage());
            return;
        }

        String baseUrl = config.getProperty("site_url", "").trim();
        String loginEndpoint = config.getProperty("login_endpoint", "/auth/login-v2").trim();
        String ipHeader = config.getProperty("ip", "127.0.0.1").trim();
        String merCode = config.getProperty("mer_code", "").trim();
        String username = config.getProperty("username", "").trim();
        String password = config.getProperty("password", "").trim();

        String merchantId = config.getProperty("merchant_id", "1").trim();
String adminId = config.getProperty("admin_id", "1").trim();
String aid = config.getProperty("aid", "1").trim();

String gmtRaw = config.getProperty("gmt", "+00:00").trim();
if (gmtRaw.startsWith("\"") && gmtRaw.endsWith("\"") && gmtRaw.length() >= 2) {
    gmtRaw = gmtRaw.substring(1, gmtRaw.length() - 1);
}
String gmtQuoted = "\"" + gmtRaw + "\"";

String uid = config.getProperty("uid", "1").trim();

String currencyRaw = config.getProperty("currency", "").trim();
String currencyJson;
if (currencyRaw.startsWith("[") && currencyRaw.endsWith("]")) {
    
    currencyJson = currencyRaw;
} else if (currencyRaw.isEmpty()) {
    currencyJson = "[]";
} else {
    
    String[] parts = currencyRaw.split("\\s*,\\s*");
    StringBuilder sb = new StringBuilder("[");
    for (int k = 0; k < parts.length; k++) {
        String item = parts[k].trim();
        if (item.startsWith("\"") && item.endsWith("\"") && item.length() >= 2) {
            item = item.substring(1, item.length() - 1);
        }
        sb.append("\"").append(item).append("\"");
        if (k < parts.length - 1) sb.append(",");
    }
    sb.append("]");
    currencyJson = sb.toString();
}

String dateFromRaw = config.getProperty("date_from", "2025-01-01T00:00:00+08:00").trim();
if (dateFromRaw.startsWith("\"") && dateFromRaw.endsWith("\"") && dateFromRaw.length() >= 2) {
    dateFromRaw = dateFromRaw.substring(1, dateFromRaw.length() - 1);
}
String dateFromQuoted = "\"" + dateFromRaw + "\"";

String dateToRaw = config.getProperty("date_to", "2025-03-01T00:00:00+08:00").trim();
if (dateToRaw.startsWith("\"") && dateToRaw.endsWith("\"") && dateToRaw.length() >= 2) {
    dateToRaw = dateToRaw.substring(1, dateToRaw.length() - 1);
}
String dateToQuoted = "\"" + dateToRaw + "\"";
String startDateRaw = config.getProperty("start_date", "2025-01-01").trim();
if (startDateRaw.startsWith("\"") && startDateRaw.endsWith("\"") && startDateRaw.length() >= 2) {
    startDateRaw = startDateRaw.substring(1, startDateRaw.length() - 1);
}
String startDateQuoted = "\"" + startDateRaw + "\"";

String endDateRaw = config.getProperty("end_date", "2025-02-27").trim();
if (endDateRaw.startsWith("\"") && endDateRaw.endsWith("\"") && endDateRaw.length() >= 2) {
    endDateRaw = endDateRaw.substring(1, endDateRaw.length() - 1);
}
String endDateQuoted = "\"" + endDateRaw + "\"";


        Scanner scanner = new Scanner(System.in);
        System.out.print("Enter the module name: ");
        String sheetName = scanner.nextLine().trim();

        String excelPath = "C:\\Users\\User\\Desktop\\vs code\\.vscode\\selenium\\api_tests.xlsx";

        Response loginResponse = RestAssured
                .given()
                .contentType(ContentType.JSON)
                .header("ip", ipHeader)
                .queryParam("mer_code", merCode)
                .queryParam("username", username)
                .queryParam("password", password)
                .post(baseUrl + loginEndpoint);

        System.out.println("Login Response: " + loginResponse.asString());

        String token = null;
        try {
            token = loginResponse.jsonPath().getString("data.auth.token");
        } catch (Exception e) {
        }
        if (token == null || token.isEmpty()) {
            System.out.println(" Login failed! Token not found.");
            return;
        }
        System.out.println(" Token: " + token);

        FileInputStream fis = new FileInputStream(excelPath);
        Workbook workbook = new XSSFWorkbook(fis);
        Sheet sheet = workbook.getSheet(sheetName);
        if (sheet == null) {
            System.out.println(" Sheet '" + sheetName + "' not found!");
            workbook.close();
            fis.close();
            return;
        }

        CellStyle redStyle = workbook.createCellStyle();
        redStyle.setFillForegroundColor(IndexedColors.RED.getIndex());
        redStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);

        CellStyle orangeStyle = workbook.createCellStyle();
        orangeStyle.setFillForegroundColor(IndexedColors.ORANGE.getIndex());
        orangeStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);

        Row header = sheet.getRow(0);
        if (header == null) header = sheet.createRow(0);
        if (header.getPhysicalNumberOfCells() < 7) {
            header.createCell(0).setCellValue("Method");
            header.createCell(1).setCellValue("URL");
            header.createCell(2).setCellValue("Payload");
            header.createCell(3).setCellValue("Status");
            header.createCell(4).setCellValue("Success");
            header.createCell(5).setCellValue("Message");
            header.createCell(6).setCellValue("ResponseTime(ms)");
        }

        DataFormatter df = new DataFormatter();

        for (int i = 1; i <= sheet.getLastRowNum(); i++) {
            Row row = sheet.getRow(i);
            if (row == null) continue;

            clearRowStyle(row);

            String method = df.formatCellValue(row.getCell(0, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK)).trim().toUpperCase();
            String urlCell = df.formatCellValue(row.getCell(1, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK)).trim();
            String payloadRaw = df.formatCellValue(row.getCell(2, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK)).trim();

           String payload = normalizePayload(payloadRaw)
           
        .replace("${MERCHANT_ID}", merchantId)
        .replace("${ADMIN_ID}", adminId)
        .replace("${AID}", aid)
        .replace("${GMT}", gmtQuoted)
        .replace("${UID}", uid)
        .replace("${CURRENCY}", currencyJson)
        .replace("$[CURRENCY]", currencyJson)
        .replace("${DATEFROM}", dateFromQuoted)
        .replace("${DATETO}", dateToQuoted)
            .replace("${START_DATE}", startDateQuoted)
    .replace("${END_DATE}", endDateQuoted);

                    
            if (method.isEmpty() || urlCell.isEmpty()) continue;

            String url = urlCell;
            if (!url.startsWith("http")) {
                if (baseUrl.endsWith("/") && url.startsWith("/")) url = baseUrl.substring(0, baseUrl.length() - 1) + url;
                else if (!baseUrl.endsWith("/") && !url.startsWith("/")) url = baseUrl + "/" + url;
                else url = baseUrl + url;
            }

            Response response = null;

            try {
                switch (method) {
                    case "GET" -> {
                
                        if (!payload.isEmpty()) {
                            try {
                                JsonPath jp = new JsonPath(payload);
                                Map<String, Object> paramMap = jp.getMap("$");
                                RequestSpecification req = RestAssured.given()
                                        .contentType(ContentType.JSON)
                                        .header("ip", ipHeader)
                                        .header("Authorization", "Bearer " + token);
                                for (Map.Entry<String, Object> e : paramMap.entrySet()) {
                                    Object val = e.getValue();
                                    if (val instanceof List) {
                                        for (Object item : (List<?>) val) {
                                            req.queryParam(e.getKey(), item);
                                        }
                                    } else {
                                        req.queryParam(e.getKey(), val);
                                    }
                                }
                                response = req.get(url);
                            } catch (Exception ex) {
                                response = RestAssured.given()
                                        .contentType(ContentType.JSON)
                                        .header("ip", ipHeader)
                                        .header("Authorization", "Bearer " + token)
                                        .get(url);
                            }
                        } else {
                            response = RestAssured.given()
                                    .contentType(ContentType.JSON)
                                    .header("ip", ipHeader)
                                    .header("Authorization", "Bearer " + token)
                                    .get(url);
                        }
                    }

                    case "POST" -> response = RestAssured.given()
                            .contentType(ContentType.JSON)
                            .header("ip", ipHeader)
                            .header("Authorization", "Bearer " + token)
                            .body(payload)
                            .post(url);

                    case "PUT" -> response = RestAssured.given()
                            .contentType(ContentType.JSON)
                            .header("ip", ipHeader)
                            .header("Authorization", "Bearer " + token)
                            .body(payload)
                            .put(url);

                    default -> {
                        row.createCell(3).setCellValue("Unsupported method: " + method);
                        continue;
                    }
                }

                String raw = response.asString();
                String message = "";
                boolean isSuccess = false;

                String contentType = response.getContentType() == null ? "" : response.getContentType().toLowerCase();
                if (contentType.contains("application/json")) {
                    try {
                        if (response.jsonPath().get("success") != null) {
                            isSuccess = response.jsonPath().getBoolean("success");
                            message = response.jsonPath().getString("message");
                        } else if (response.jsonPath().get("error") != null) {
                            Object err = response.jsonPath().get("error");
                            if (err instanceof Boolean) isSuccess = !(Boolean) err;
                            else isSuccess = true;
                            message = response.jsonPath().getString("message");
                        } else {
                            message = raw;
                        }
                    } catch (Exception e) {
                        message = "Cannot parse response. Raw: " + raw;
                    }
                } else {
                    message = raw;
                }

                String status = String.valueOf(response.getStatusCode());
                long responseTime = response.getTime();

                row.createCell(3).setCellValue(status);
                row.createCell(4).setCellValue(String.valueOf(isSuccess));
                row.createCell(5).setCellValue(message);
                row.createCell(6).setCellValue(responseTime);

                if (!isSuccess || responseTime > 5000) {
                    CellStyle styleToApply = !isSuccess ? redStyle : orangeStyle;
                    if (!isSuccess && responseTime > 5000) styleToApply = redStyle;
                    colorEntireRow(row, styleToApply);
                }

            } catch (Exception e) {
                row.createCell(3).setCellValue("ERROR");
                row.createCell(4).setCellValue("false");
                row.createCell(5).setCellValue(e.getMessage());
                row.createCell(6).setCellValue(0);
                colorEntireRow(row, redStyle);
            }
        }

        fis.close();
        try (FileOutputStream fos = new FileOutputStream(excelPath)) {
            workbook.write(fos);
        }
        workbook.close();

        System.out.println(" API testing completed. Results saved to Excel sheet: " + sheetName);
    }

    private static void clearRowStyle(Row row) {
        for (int j = 0; j < row.getLastCellNum(); j++) {
            Cell cell = row.getCell(j, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK);
            CellStyle defaultStyle = row.getSheet().getWorkbook().createCellStyle();
            cell.setCellStyle(defaultStyle);
        }
    }

    private static void colorEntireRow(Row row, CellStyle style) {
        for (int j = 0; j < row.getLastCellNum(); j++) {
            Cell cell = row.getCell(j, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK);
            cell.setCellStyle(style);
        }
    }

    private static String normalizePayload(String payload) {
        if (payload == null) return "";
        String p = payload.trim();
        if (p.startsWith("'")) p = p.substring(1);
        p = p.replace("&#10;", "").replace("&#13;", "");

        if (p.startsWith("\"") && p.endsWith("\"")) {
            p = p.substring(1, p.length() - 1);
            p = p.replace("\"\"", "\"");
        }

        p = p.replace("\r", "").replace("\n", "").trim();

        p = p.replace("\\\"", "\"");

        return p;
    }
}
