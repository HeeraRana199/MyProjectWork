package com.cts.util;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.time.LocalDate;
import java.time.ZoneId;

import lombok.Data;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import com.cts.entity.Candidate;
import com.cts.entity.CandidateScore;

public class CandidateExcelHelper {

    /**
     * Canonical reference list of required column headers in the exact casing
     * the upstream Excel template uses. Schema validation matches incoming
     * file headers against this list (case-insensitive, whitespace-tolerant).
     * Any header in this list that's not present in the uploaded file is
     * reported as a missing-column error using the original casing below.
     *
     * Only columns whose values are actually consumed by the application
     * (read into the Candidate / CandidateScore entities and rendered in the
     * frontend talent card / leader filter / CSV export) are kept active.
     * The remainder of the upstream template columns are commented out below
     * so uploads no longer need to carry them.
     */
    public static final String[] REQUIRED_HEADERS = {
            // ── Identity (read into Candidate) ─────────────────────────────
            // "Cognizant Candidate ID",  // unused — commented out per project decision
            "Associate Id",
            "Name",
            "Cognizant Email ID",
            "Gender",
            "DOJ",

            // ── Placement (read into Candidate) ────────────────────────────
            "Deployment Location",
            "SL",
            "Track name (As Curriculum)",
            "Cohort Code",

            // ── Scores / RAG (read into CandidateScore) ────────────────────
            "Interim RAG",
            // "Interim Evaluation Feedback",  // unused — commented out per project decision
            "Final Attempt 1 RAG",
            "Final Attempt 1 Evaluation Feedback",
            "Final Attempt 2 RAG",
            "Attendance Health Score",
            "Language Assessment Score"

            // ── Unused columns (kept here as documentation only) ───────────
            // Frontend never reads these; backend never parses them. Re-enable
            // by uncommenting the entry if a future feature needs the field.
            //
            // "DEMAND ID",
            // "RRID",
            // "SuperSet ID",
            // "Email ID",
            // "CSD/Non Intern/ Interns",
            // "Joiners Type",
            // "Circuit/Non Circuit",
            // "Hire Mode",
            // "Grade",
            // "Category - Pipeline Report",
            // "Sub Category- Pipeline Report",
            // "Type Of Hire Supply",
            // "Training Type",
            // "Type of Hire DD",
            // "Joining Location",
            // "Location Preference 1",
            // "Location Preference 2",
            // "Cog Intake Demand Month",
            // "BU Intake Demand Month",
            // "BU",
            // "Sub BU",
            // "Previous SL",
            // "Previous BU",
            // "Previous Sub BU",
            // "Deployed BU",
            // "Assigned Project ID",
            // "Assigned Project Name",
            // "Activity Code",
            // "LP as per Milestone Tracker",
            // "Approach Name",
            // "Candidate Cluster",
            // "Cohort Start Date",
            // "Cohort Training Start Date",
            // "Coach ID",
            // "Coach Name",
            // "Stage 1/Delta End date",
            // "Stage 2 End date",
            // "Stage 3 End date",
            // "Stage 4 End date",
            // "Release date As per Curriculum",
            // "Revised - Tentative Release Date",
            // "Reason for Release change",
            // "Actual Release Date",
            // "Tentative/Actual Release Month",
            // "Schedule Variance",
            // "Exit date/Break Start Date",
            // "Break Resumption date",
            // "Exit Initiated date",
            // "Technical Training Status",
            // "Final Status",
            // "On Hold Reason",
            // "Exit Reason",
            // "CSD Conversion Reason",
            // "Moved to FTE Reason",
            // "BGV-Remarks",
            // "Latest status updated by",
            // "Latest status update date/time",
            // "Technical Training Remedial Start Date",
            // "Technical Training Remedial End date",
            // "SME Remedial Start Date",
            // "SME Remedial End date",
            // "Tentative/Actual Supply Date",
            // "Proposed Project ID",
            // "Proposed Project Name",
            // "Brief Description",
            // "External Trainer ID",
            // "External Trainer Name",
            // "Internal Trainer 1 ID",
            // "Internal Trainer 2 ID",
            // "Internal Trainer 3 ID",
            // "Internal Trainer 4 ID",
            // "Internal Trainer 5 ID",
            // "Internal Trainer 6 ID",
            // "RTO Location",
            // "ASL Updated Date",
            // "SM POC",
            // "ASL/Exit Process phase",
            // "BU Project Allocation Date",
            // "SL Engagement category",
            // "Platform Cohort Vs Non Platform cohort",
            // "Is Stage 1 Applicable",
            // "SL Lead Id",
            // "BU PM ID",
            // "House name",
            // "Breach 1 category",
            // "Breach 1 category Remarks",
            // "Breach 2 category",
            // "Breach 2 category Remarks",
            // "Breach 3 category",
            // "Breach 3 category Remarks",
            // "Current Location",
            // "Location Change",
            // "Older track Name",
            // "Interim Technical SME ID",
            // "Interim Project and/or SBA SME ID",
            // "Final Technical Evaluation Attempt 1 SME ID",
            // "Final Project and/or SBA Attempt 1 SME ID",
            // "Final Technical Evaluation Attempt 2 SME ID",
            // "Final Project and/or SBA Attempt 2 SME ID",
            // "Interim Technical Score",
            // "Interim Project and/or SBA Score",
            // "Final Technical Evaluation Attempt 1 Score",
            // "Final Project and/or SBA Attempt 1 Score",
            // "Final Technical Evaluation Attempt 2 Score",
            // "Final Project and/or SBA Attempt 2 Score",
            // "Interim Evaluation Planned Date",
            // "Interim SME ID",
            // "Interim Evaluation Actual Date",
            // "Final Attempt 1 Planned Date",
            // "Final Attempt 1 SME ID",
            // "Final Attempt 1 Actual Date",
            // "Final Attempt 2 SME ID",
            // "Final Attempt 2 Actual Date",
            // "Final Attempt 2 Evaluation Feedback",
            // "Final special evaluation SME ID",
            // "Final special evaluation RAG",
            // "Final special evaluation Actual Date",
            // "Final special evaluation Feedback",
            // "Final reattempt special evaluation SME ID",
            // "Final reattempt special evaluation RAG",
            // "Final reattempt special evaluation Actual Date",
            // "Final reattempt special evaluation Feedback",
            // "Stage 1 evaluation SME ID",
            // "Stage 1 evaluation RAG",
            // "Stage 1 evaluation Actual Date",
            // "Stage 1 evaluation Feedback",
            // "PHS-RAG",
            // "Qualifier/Stage 1/Delta Attempt 1 Planned date",
            // "Qualifier/Stage 1/Delta Attempt 1 Completion date",
            // "Qualifier/Stage 1/Delta Attempt 1 Score",
            // "Qualifier/Stage 1/Delta Attempt 1 Status",
            // "Qualifier/Stage 1/Delta Attempt 2 Planned date",
            // "Qualifier/Stage 1/Delta Attempt 2 Completion date",
            // "Qualifier/Stage1/Delta Attempt 2 Score",
            // "Qualifier/Stage 1/Delta Attempt 2 Status",
            // "Qualifier/Stage 1/Delta Attempt 3 Planned date",
            // "Qualifier/Stage 1/Delta Attempt 3 Completion date",
            // "Qualifier/Stage 1/Delta Attempt 3 Score",
            // "Qualifier/Stage 1/Delta Attempt 3 Status",
            // "Average of HandsOn",
            // "Average of Assess Type-1",
            // "Average of Assess Type-2"
    };

    /**
     * Normalize a header string for comparison: lowercase, trim, collapse any
     * run of whitespace down to a single space. Tolerates Excel quirks such as
     * trailing spaces, double-spaces from copy-paste, or mixed casing.
     */
    private static String normalize(String s) {
        if (s == null) return "";
        return s.toLowerCase().trim().replaceAll("\\s+", " ");
    }

    @Data
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

    private static Double getCellAsDouble(Cell cell) {
        if (cell == null) return null;

        if (cell.getCellType() == CellType.NUMERIC) {
            return cell.getNumericCellValue();
        }
        if (cell.getCellType() == CellType.STRING && !cell.getStringCellValue().isBlank()) {
            try {
                return Double.parseDouble(cell.getStringCellValue().trim());
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("Invalid double format: " + cell.getStringCellValue());
            }
        }
        return null;
    }

    /**
     * Strict schema validation. Every header in REQUIRED_HEADERS must be present
     * in the file (matched case-insensitively, whitespace-collapsed). Missing
     * headers are reported with their canonical casing so the operator can fix
     * the source spreadsheet quickly.
     */
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

            // Build the set of normalized headers present in the file
            Set<String> fileHeaders = new LinkedHashSet<>();
            for (Cell cell : headerRow) {
                String header = normalize(getCellAsString(cell));
                if (!header.isEmpty()) {
                    fileHeaders.add(header);
                }
            }

            // Every required header must be present (strict match on normalized form)
            for (String required : REQUIRED_HEADERS) {
                if (!fileHeaders.contains(normalize(required))) {
                    result.setValid(false);
                    result.addMissingHeader(required);
                }
            }

            if (!result.isValid()) {
                result.setMessage(
                        "Missing " + result.getMissingHeaders().size() +
                        " required column(s): " + String.join(", ", result.getMissingHeaders())
                );
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
            Row headerRow = sheet.getRow(0);

            if (headerRow == null) {
                throw new RuntimeException("No header row found in Excel file");
            }

            // Build a map of normalized header names to column indices so the
            // lookups below match regardless of original casing / spacing.
            Map<String, Integer> headerIndexMap = new HashMap<>();
            for (Cell cell : headerRow) {
                String header = normalize(getCellAsString(cell));
                if (!header.isEmpty()) {
                    headerIndexMap.put(header, cell.getColumnIndex());
                }
            }

            // ✅ Actual data starts after header row
            for (int i = 1; i <= sheet.getLastRowNum(); i++) {

                Row row = sheet.getRow(i);
                if (row == null) continue;

                String name = getCellAsStringByHeader(row, headerIndexMap, "name");
                if (name.isEmpty()) continue; // skip empty rows

                Candidate candidate = new Candidate();

                try {
                    // candidate.setCognizantCandidateId(getCellAsIntegerByHeader(row, headerIndexMap, "cognizant candidate id"));
                    candidate.setAssociateId(getCellAsIntegerByHeader(row, headerIndexMap, "associate id"));
                    candidate.setCandidateName(getCellAsStringByHeader(row, headerIndexMap, "name"));
                    candidate.setCognizantEmailID(getCellAsStringByHeader(row, headerIndexMap, "cognizant email id"));
                    candidate.setGender(getCellAsStringByHeader(row, headerIndexMap, "gender"));
                    candidate.setDeploymentLocation(getCellAsStringByHeader(row, headerIndexMap, "deployment location"));
                    candidate.setTrackName(getCellAsStringByHeader(row, headerIndexMap, "track name (as curriculum)"));
                    candidate.setCohortCode(getCellAsStringByHeader(row, headerIndexMap, "cohort code"));
                    candidate.setSl(getCellAsStringByHeader(row, headerIndexMap, "sl"));
                    candidate.setDoj(getCellAsLocalDateByHeader(row, headerIndexMap, "doj"));

                    CandidateScore candidateScore = new CandidateScore();
                    candidateScore.setAttendanceScore(getCellAsDoubleByHeader(row, headerIndexMap, "attendance health score"));
                    candidateScore.setLanguageScore(getCellAsStringByHeader(row, headerIndexMap, "language assessment score"));
                    candidateScore.setInterimScore(getCellAsStringByHeader(row, headerIndexMap, "interim rag"));
                    String finalRag1 = getCellAsStringByHeader(row, headerIndexMap, "final attempt 1 rag");
                    String finalRag2 = getCellAsStringByHeader(row, headerIndexMap, "final attempt 2 rag");
                    String finalRag = finalRag2.length() == 0 ? finalRag1 : finalRag2;
                    candidateScore.setFinalScore(finalRag);
                    // candidateScore.setInterimEvaluationFeedback(getCellAsStringByHeader(row, headerIndexMap, "interim evaluation feedback"));
                    candidateScore.setFinalEvaluationFeedback(getCellAsStringByHeader(row, headerIndexMap, "final attempt 1 evaluation feedback"));
                    if (finalRag.equalsIgnoreCase("Green")) {
                        candidateScore.setReadiness("Ready");
                    }

                    candidateScore.setCandidate(candidate);
                    candidate.setCandidateScore(candidateScore);

                    candidates.add(candidate);
                } catch (IllegalArgumentException e) {
                    continue;
                }
            }

        } catch (Exception e) {
            throw new RuntimeException("Failed to parse Excel file", e);
        }

        return candidates;
    }

    // ── Header-name lookup helpers (caller passes normalized header name) ──

    private static String getCellAsStringByHeader(Row row, Map<String, Integer> headerIndexMap, String headerName) {
        Integer colIndex = headerIndexMap.get(normalize(headerName));
        if (colIndex == null) return "";
        return getCellAsString(row.getCell(colIndex));
    }

    private static Integer getCellAsIntegerByHeader(Row row, Map<String, Integer> headerIndexMap, String headerName) {
        Integer colIndex = headerIndexMap.get(normalize(headerName));
        if (colIndex == null) return null;
        return getCellAsInteger(row.getCell(colIndex));
    }

    private static LocalDate getCellAsLocalDateByHeader(Row row, Map<String, Integer> headerIndexMap, String headerName) {
        Integer colIndex = headerIndexMap.get(normalize(headerName));
        if (colIndex == null) return null;
        return getCellAsLocalDate(row.getCell(colIndex));
    }

    private static Double getCellAsDoubleByHeader(Row row, Map<String, Integer> headerIndexMap, String headerName) {
        Integer colIndex = headerIndexMap.get(normalize(headerName));
        if (colIndex == null) return null;
        return getCellAsDouble(row.getCell(colIndex));
    }

    private static LocalDate getCellAsLocalDate(Cell cell) {
        if (cell == null) return null;

        if (cell.getCellType() == CellType.NUMERIC && DateUtil.isCellDateFormatted(cell)) {
            return cell.getDateCellValue()
                    .toInstant()
                    .atZone(ZoneId.systemDefault())
                    .toLocalDate();
        }

        if (cell.getCellType() == CellType.STRING && !cell.getStringCellValue().isBlank()) {
            try {
                return LocalDate.parse(cell.getStringCellValue().trim());
            } catch (Exception e) {
                throw new IllegalArgumentException("Invalid DOJ date format: " + cell.getStringCellValue());
            }
        }

        return null;
    }
}
