package com.cts.util;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import com.cts.entity.Candidate;

public class CandidateExcelHelper {

    private static String getCellAsString(Cell cell) {
        if (cell == null) return "";

        return switch (cell.getCellType()) {
            case STRING -> cell.getStringCellValue().trim();
            case NUMERIC -> String.valueOf((long) cell.getNumericCellValue());
            case BOOLEAN -> String.valueOf(cell.getBooleanCellValue());
            default -> "";
        };
    }

    private static Integer getCellAsInteger(Cell cell) {
        if (cell == null) return null;

        if (cell.getCellType() == CellType.NUMERIC) {
            return (int) cell.getNumericCellValue();
        }
        if (cell.getCellType() == CellType.STRING && !cell.getStringCellValue().isBlank()) {
            return Integer.parseInt(cell.getStringCellValue().trim());
        }
        return null;
    }

    public static List<Candidate> excelToCandidates(InputStream is) {
        List<Candidate> candidates = new ArrayList<>();

        try (Workbook workbook = new XSSFWorkbook(is)) {
            Sheet sheet = workbook.getSheetAt(0);

            // ✅ Actual data starts after header rows
            for (int i = 1; i <= sheet.getLastRowNum(); i++) {

                Row row = sheet.getRow(i);
                if (row == null) continue;

                String name = getCellAsString(row.getCell(5));
                if (name.isEmpty()) continue; // skip empty rows

                Candidate candidate = new Candidate();

                candidate.setCognizantCandidateId(getCellAsInteger(row.getCell(3)));
                candidate.setAssociateId(getCellAsInteger(row.getCell(4)));
                candidate.setCandidateName(getCellAsString(row.getCell(5)));
                candidate.setCognizantEmailID(getCellAsString(row.getCell(7)));
                candidate.setGender(getCellAsString(row.getCell(8)));

                candidate.setDeploymentLocation(getCellAsString(row.getCell(26)));
//                System.out.println("A  " + row.getCell(41));
//                System.out.println("B   " + row.getCell(36));
                candidate.setTrackName(getCellAsString(row.getCell(35)));
                candidate.setCohortCode(getCellAsString(row.getCell(40)));


                candidates.add(candidate);
            }

        } catch (Exception e) {
            throw new RuntimeException("Failed to parse Excel file", e);
        }

        return candidates;
    }
}