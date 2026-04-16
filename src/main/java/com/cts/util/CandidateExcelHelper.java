package com.cts.util;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import com.cts.entity.Candidate;

public class CandidateExcelHelper {

    // Define required column headers (case-insensitive)
    private static final Set<String> REQUIRED_HEADERS = Set.of(
        "cognizant candidate id",
        "associate id",
        "candidate name",
        "cognizant email id",
        "gender",
        "deployment location",
        "track name",
        "cohort code"
    );

    // Map required headers to their key identifiers for flexible matching
    private static final java.util.Map<String, String[]> HEADER_KEYWORDS = java.util.Map.of(
        "cognizant candidate id", new String[]{"cognizant", "candidate", "id"},
        "associate id", new String[]{"associate", "id"},
        "candidate name", new String[]{"candidate", "name"},
        "cognizant email id", new String[]{"cognizant", "email", "id"},
        "gender", new String[]{"gender"},
        "deployment location", new String[]{"deployment", "location"},
        "track name", new String[]{"track", "name"},
        "cohort code", new String[]{"cohort", "code"}
    );

    public static class ValidationResult {
        private boolean valid;
        private String message;
        private List<String> missingHeaders;
        private List<String> dataValidationErrors;

        public ValidationResult(boolean valid, String message) {
            this.valid = valid;
            this.message = message;
            this.missingHeaders = new ArrayList<>();
            this.dataValidationErrors = new ArrayList<>();
        }

        // Getters and setters
        public boolean isValid() { return valid; }
        public String getMessage() { return message; }
        public List<String> getMissingHeaders() { return missingHeaders; }
        public List<String> getDataValidationErrors() { return dataValidationErrors; }
        public void setValid(boolean valid) { this.valid = valid; }
        public void setMessage(String message) { this.message = message; }
        public void addMissingHeader(String header) { this.missingHeaders.add(header); }
        public void addDataValidationError(String error) { this.dataValidationErrors.add(error); }
    }

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
            try {
                return Integer.parseInt(cell.getStringCellValue().trim());
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("Invalid integer format: " + cell.getStringCellValue());
            }
        }
        return null;
    }

    public static ValidationResult validateExcelSchema(InputStream is) {
        ValidationResult result = new ValidationResult(true, "Schema validation passed");

        try (Workbook workbook = new XSSFWorkbook(is)) {
            Sheet sheet = workbook.getSheetAt(0);
            Row headerRow = sheet.getRow(0);

            if (headerRow == null) {
                result.setValid(false);
                result.setMessage("No header row found in Excel file");
                return result;
            }

            // Collect all headers from the file
            List<String> fileHeaders = new ArrayList<>();
            for (Cell cell : headerRow) {
                String header = getCellAsString(cell).toLowerCase().trim();
                if (!header.isEmpty()) {
                    fileHeaders.add(header);
                }
            }

            // Check for missing required headers with flexible matching
            for (String required : REQUIRED_HEADERS) {
                boolean found = false;
                String[] requiredWords = HEADER_KEYWORDS.get(required);

                if (requiredWords != null) {
                    for (String fileHeader : fileHeaders) {
                        // Check if any of the required words are present in the file header
                        // This allows for variations like "Name" matching "candidate name"
                        for (String word : requiredWords) {
                            if (fileHeader.contains(word)) {
                                found = true;
                                break;
                            }
                        }
                        if (found) break;
                    }
                }

                if (!found) {
                    result.setValid(false);
                    result.addMissingHeader(required);
                }
            }

            if (!result.isValid()) {
                result.setMessage("Missing required columns: " + String.join(", ", result.getMissingHeaders()));
            }

        } catch (Exception e) {
            result.setValid(false);
            result.setMessage("Error validating Excel schema: " + e.getMessage());
        }

        return result;
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

                try {
                    candidate.setCognizantCandidateId(getCellAsInteger(row.getCell(3)));
                    candidate.setAssociateId(getCellAsInteger(row.getCell(4)));
                    candidate.setCandidateName(getCellAsString(row.getCell(5)));
                    candidate.setCognizantEmailID(getCellAsString(row.getCell(7)));
                    candidate.setGender(getCellAsString(row.getCell(8)));

                    candidate.setDeploymentLocation(getCellAsString(row.getCell(26)));
                    candidate.setTrackName(getCellAsString(row.getCell(35)));
                    candidate.setCohortCode(getCellAsString(row.getCell(40)));

                    candidates.add(candidate);
                } catch (IllegalArgumentException e) {
                    // Skip invalid rows but could collect errors
                    continue;
                }
            }

        } catch (Exception e) {
            throw new RuntimeException("Failed to parse Excel file", e);
        }

        return candidates;
    }
}