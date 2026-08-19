package com.framework;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

public class ExcelReader {

    public static List<NotificationData> readNotificationData(String filePath) {

        List<NotificationData> dataList = new ArrayList<>();

        try (FileInputStream fis = new FileInputStream(filePath);
             Workbook workbook = new XSSFWorkbook(fis)) {

            Sheet sheet = workbook.getSheetAt(0);

            // Skip header row
            for (int i = 1; i <= sheet.getLastRowNum(); i++) {

                Row row = sheet.getRow(i);

                if (row == null) {
                    continue;
                }

                NotificationData data = new NotificationData();

                data.setProvider(getCellValue(row.getCell(0)));
                data.setStartDate(getCellValue(row.getCell(1)));
                data.setEndDate(getCellValue(row.getCell(2)));
                data.setRemark(getCellValue(row.getCell(3)));

                dataList.add(data);
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

        return dataList;
    }

   private static String getCellValue(Cell cell) {

    if (cell == null) {
        return "";
    }

    if (cell.getCellType() == CellType.NUMERIC && DateUtil.isCellDateFormatted(cell)) {

        java.time.LocalDateTime date =
                cell.getLocalDateTimeCellValue();

        java.time.format.DateTimeFormatter formatter =
                java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

        return date.format(formatter);
    }

    DataFormatter formatter = new DataFormatter();

    return formatter.formatCellValue(cell).trim();
}
}