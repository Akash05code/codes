package com.framework;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.List;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

public class ExcelResultWriter {

    private final String excelPath;

    public ExcelResultWriter(String excelPath) {
        this.excelPath = excelPath;
    }

    public synchronized void writeResult(
            String provider,
            String currency,
            String status,
            int attempt,
            String message) {

        try (
                FileInputStream input =
                        new FileInputStream(excelPath);

                Workbook workbook =
                        new XSSFWorkbook(input)
        ) {

            Sheet sheet =
                    workbook.getSheet("ExecutionResults");

            if (sheet == null) {

                sheet =
                        workbook.createSheet(
                                "ExecutionResults"
                        );

                Row header =
                        sheet.createRow(0);

                header.createCell(0)
                        .setCellValue("Provider");

                header.createCell(1)
                        .setCellValue("Currency");

                header.createCell(2)
                        .setCellValue("Status");

                header.createCell(3)
                        .setCellValue("Attempt");

                header.createCell(4)
                        .setCellValue("Message");

            }

            int nextRow =
                    sheet.getLastRowNum() + 1;

            if (sheet.getPhysicalNumberOfRows() == 0) {
                nextRow = 0;
            }

            Row row =
                    sheet.createRow(nextRow);

            row.createCell(0)
                    .setCellValue(provider);

            row.createCell(1)
                    .setCellValue(currency);

            row.createCell(2)
                    .setCellValue(status);

            row.createCell(3)
                    .setCellValue(attempt);

            row.createCell(4)
                    .setCellValue(message);

            sheet.autoSizeColumn(0);
            sheet.autoSizeColumn(1);
            sheet.autoSizeColumn(2);
            sheet.autoSizeColumn(3);
            sheet.autoSizeColumn(4);

            try (
                    FileOutputStream output =
                            new FileOutputStream(excelPath)
            ) {

                workbook.write(output);
            }

            System.out.println(
                    "Excel result written: "
                            + provider
                            + " | "
                            + currency
                            + " | "
                            + status
            );

        } catch (Exception e) {

            System.out.println(
                    "ERROR WRITING EXCEL RESULT: "
                            + e.getMessage()
            );
        }
    }
}