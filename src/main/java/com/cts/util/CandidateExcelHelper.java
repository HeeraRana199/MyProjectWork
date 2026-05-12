package com.cts.util;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
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

    // Define required column headers (case-insensitive) - Specific columns as requested
    private static final Set<String> REQUIRED_HEADERS = Set.of(
        "demand id",
        "rrid",
        "superset id",
        "cognizant candidate id",
        "associate id",
        "name",
        "email id",
        "cognizant email id",
        "gender",
        "csd/non intern/ interns",
        "joiners type",
        "circuit/non circuit",
        "hire mode",
        "grade",
        "category - pipeline report",
        "sub category- pipeline report",
        "type of hire supply",
        "training type",
        "type of hire dd",
        "doj",
        "joining location",
        "location preference 1",
        "location preference 2",
        "cog intake demand month",
        "bu intake demand month",
        "deployment location",
        "sl",
        "bu",
        "sub bu",
        "previous sl",
        "previous bu",
        "previous sub bu",
        "deployed bu",
        "assigned project id",
        "assigned project name",
        "track name (as curriculum)",
        "activity code",
        "lp as per milestone tracker",
        "approach name",
        "candidate cluster",
        "cohort code",
        "cohort start date",
        "cohort training start date",
        "coach id",
        "coach name",
        "stage 1/delta end date",
        "stage 2 end date",
        "stage 3 end date",
        "stage 4 end date",
        "release date as per curriculum",
        "revised - tentative release date",
        "reason for release change",
        "actual release date",
        "tentative/actual release month",
        "schedule variance",
        "exit date/break start date",
        "break resumption date",
        "exit initiated date",
        "performance health score",
        "technical training status",
        "final status",
        "on hold reason",
        "exit reason",
        "csd conversion reason",
        "moved to fte reason",
        "bgv-remarks",
        "latest status updated by",
        "latest status update date/time",
        "technical training remedial start date",
        "technical training remedial end date",
        "sme remedial start date",
        "sme remedial end date",
        "tentative/actual supply date",
        "proposed project id",
        "proposed project name",
        "brief description",
        "external trainer id",
        "external trainer name",
        "internal trainer 1 id",
        "internal trainer 2 id",
        "internal trainer 3 id",
        "internal trainer 4 id",
        "internal trainer 5 id",
        "internal trainer 6 id",
        "rto location",
        "asl updated date",
        "sm poc",
        "asl/exit process phase",
        "bu project allocation date",
        "sl engagement category",
        "platform cohort vs non platform cohort",
        "is stage 1 applicable",
        "sl lead id",
        "bu pm id",
        "house name",
        "breach 1 category",
        "breach 1 category remarks",
        "breach 2 category",
        "breach 2 category remarks",
        "breach 3 category",
        "breach 3 category remarks",
        "current location",
        "location change",
        "older track name",
        "interim technical sme id",
        "interim project and/or sba sme id",
        "final technical evaluation attempt 1 sme id",
        "final project and/or sba attempt 1 sme id",
        "final technical evaluation attempt 2 sme id",
        "final project and/or sba attempt 2 sme id",
        "interim technical score",
        "interim project and/or sba score",
        "final technical evaluation attempt 1 score",
        "final project and/or sba attempt 1 score",
        "final technical evaluation attempt 2 score",
        "final project and/or sba attempt 2 score",
        "interim evaluation planned date",
        "interim sme id",
        "interim rag",
        "interim evaluation actual date",
        "interim evaluation feedback",
        "final attempt 1 planned date",
        "final attempt 1 sme id",
        "final attempt 1 rag",
        "final attempt 1 actual date",
        "final attempt 1 evaluation feedback",
        "final attempt 2 sme id",
        "final attempt 2 rag",
        "final attempt 2 actual date",
        "final attempt 2 evaluation feedback",
        "final special evaluation sme id",
        "final special evaluation rag",
        "final special evaluation actual date",
        "final special evaluation feedback",
        "final reattempt special evaluation sme id",
        "final reattempt special evaluation rag",
        "final reattempt special evaluation actual date",
        "final reattempt special evaluation feedback",
        "stage 1 evaluation sme id",
        "stage 1 evaluation rag",
        "stage 1 evaluation actual date",
        "stage 1 evaluation feedback",
        "phs-rag",
        "qualifier/stage 1/delta attempt 1 planned date",
        "qualifier/stage 1/delta attempt 1 completion date",
        "qualifier/stage 1/delta attempt 1 score",
        "qualifier/stage 1/delta attempt 1 status",
        "qualifier/stage 1/delta attempt 2 planned date",
        "qualifier/stage 1/delta attempt 2 completion date",
        "qualifier/stage1/delta attempt 2 score",
        "qualifier/stage 1/delta attempt 2 status",
        "qualifier/stage 1/delta attempt 3 planned date",
        "qualifier/stage 1/delta attempt 3 completion date",
        "qualifier/stage 1/delta attempt 3 score",
        "qualifier/stage 1/delta attempt 3 status",
        "attendance health score",
        "language assessment score",
        "average of handson",
        "average of assess type-1",
        "average of assess type-2"
    );

    // Map required headers to their key identifiers for flexible matching
    private static final java.util.Map<String, String[]> HEADER_KEYWORDS = new java.util.HashMap<>();
    static {
        HEADER_KEYWORDS.put("demand id", new String[]{"demand", "id"});
        HEADER_KEYWORDS.put("rrid", new String[]{"rrid"});
        HEADER_KEYWORDS.put("superset id", new String[]{"superset", "id"});
        HEADER_KEYWORDS.put("cognizant candidate id", new String[]{"cognizant", "candidate", "id"});
        HEADER_KEYWORDS.put("associate id", new String[]{"associate", "id"});
        HEADER_KEYWORDS.put("name", new String[]{"name"});
        HEADER_KEYWORDS.put("email id", new String[]{"email", "id"});
        HEADER_KEYWORDS.put("cognizant email id", new String[]{"cognizant", "email", "id"});
        HEADER_KEYWORDS.put("gender", new String[]{"gender"});
        HEADER_KEYWORDS.put("csd/non intern/ interns", new String[]{"csd", "non", "intern", "interns"});
        HEADER_KEYWORDS.put("joiners type", new String[]{"joiners", "type"});
        HEADER_KEYWORDS.put("circuit/non circuit", new String[]{"circuit", "non", "circuit"});
        HEADER_KEYWORDS.put("hire mode", new String[]{"hire", "mode"});
        HEADER_KEYWORDS.put("grade", new String[]{"grade"});
        HEADER_KEYWORDS.put("category - pipeline report", new String[]{"category", "pipeline", "report"});
        HEADER_KEYWORDS.put("sub category- pipeline report", new String[]{"sub", "category", "pipeline", "report"});
        HEADER_KEYWORDS.put("type of hire supply", new String[]{"type", "hire", "supply"});
        HEADER_KEYWORDS.put("training type", new String[]{"training", "type"});
        HEADER_KEYWORDS.put("type of hire dd", new String[]{"type", "hire", "dd"});
        HEADER_KEYWORDS.put("doj", new String[]{"doj"});
        HEADER_KEYWORDS.put("joining location", new String[]{"joining", "location"});
        HEADER_KEYWORDS.put("location preference 1", new String[]{"location", "preference", "1"});
        HEADER_KEYWORDS.put("location preference 2", new String[]{"location", "preference", "2"});
        HEADER_KEYWORDS.put("cog intake demand month", new String[]{"cog", "intake", "demand", "month"});
        HEADER_KEYWORDS.put("bu intake demand month", new String[]{"bu", "intake", "demand", "month"});
        HEADER_KEYWORDS.put("deployment location", new String[]{"deployment", "location"});
        HEADER_KEYWORDS.put("sl", new String[]{"sl"});
        HEADER_KEYWORDS.put("bu", new String[]{"bu"});
        HEADER_KEYWORDS.put("sub bu", new String[]{"sub", "bu"});
        HEADER_KEYWORDS.put("previous sl", new String[]{"previous", "sl"});
        HEADER_KEYWORDS.put("previous bu", new String[]{"previous", "bu"});
        HEADER_KEYWORDS.put("previous sub bu", new String[]{"previous", "sub", "bu"});
        HEADER_KEYWORDS.put("deployed bu", new String[]{"deployed", "bu"});
        HEADER_KEYWORDS.put("assigned project id", new String[]{"assigned", "project", "id"});
        HEADER_KEYWORDS.put("assigned project name", new String[]{"assigned", "project", "name"});
        HEADER_KEYWORDS.put("track name (as curriculum)", new String[]{"track", "name", "curriculum"});
        HEADER_KEYWORDS.put("activity code", new String[]{"activity", "code"});
        HEADER_KEYWORDS.put("lp as per milestone tracker", new String[]{"lp", "milestone", "tracker"});
        HEADER_KEYWORDS.put("approach name", new String[]{"approach", "name"});
        HEADER_KEYWORDS.put("candidate cluster", new String[]{"candidate", "cluster"});
        HEADER_KEYWORDS.put("cohort code", new String[]{"cohort", "code"});
        HEADER_KEYWORDS.put("cohort start date", new String[]{"cohort", "start", "date"});
        HEADER_KEYWORDS.put("cohort training start date", new String[]{"cohort", "training", "start", "date"});
        HEADER_KEYWORDS.put("coach id", new String[]{"coach", "id"});
        HEADER_KEYWORDS.put("coach name", new String[]{"coach", "name"});
        HEADER_KEYWORDS.put("stage 1/delta end date", new String[]{"stage", "1", "delta", "end", "date"});
        HEADER_KEYWORDS.put("stage 2 end date", new String[]{"stage", "2", "end", "date"});
        HEADER_KEYWORDS.put("stage 3 end date", new String[]{"stage", "3", "end", "date"});
        HEADER_KEYWORDS.put("stage 4 end date", new String[]{"stage", "4", "end", "date"});
        HEADER_KEYWORDS.put("release date as per curriculum", new String[]{"release", "date", "curriculum"});
        HEADER_KEYWORDS.put("revised - tentative release date", new String[]{"revised", "tentative", "release", "date"});
        HEADER_KEYWORDS.put("reason for release change", new String[]{"reason", "release", "change"});
        HEADER_KEYWORDS.put("actual release date", new String[]{"actual", "release", "date"});
        HEADER_KEYWORDS.put("tentative/actual release month", new String[]{"tentative", "actual", "release", "month"});
        HEADER_KEYWORDS.put("schedule variance", new String[]{"schedule", "variance"});
        HEADER_KEYWORDS.put("exit date/break start date", new String[]{"exit", "date", "break", "start", "date"});
        HEADER_KEYWORDS.put("break resumption date", new String[]{"break", "resumption", "date"});
        HEADER_KEYWORDS.put("exit initiated date", new String[]{"exit", "initiated", "date"});
        HEADER_KEYWORDS.put("performance health score", new String[]{"performance", "health", "score"});
        HEADER_KEYWORDS.put("technical training status", new String[]{"technical", "training", "status"});
        HEADER_KEYWORDS.put("final status", new String[]{"final", "status"});
        HEADER_KEYWORDS.put("on hold reason", new String[]{"hold", "reason"});
        HEADER_KEYWORDS.put("exit reason", new String[]{"exit", "reason"});
        HEADER_KEYWORDS.put("csd conversion reason", new String[]{"csd", "conversion", "reason"});
        HEADER_KEYWORDS.put("moved to fte reason", new String[]{"moved", "fte", "reason"});
        HEADER_KEYWORDS.put("bgv-remarks", new String[]{"bgv", "remarks"});
        HEADER_KEYWORDS.put("latest status updated by", new String[]{"latest", "status", "updated", "by"});
        HEADER_KEYWORDS.put("latest status update date/time", new String[]{"latest", "status", "update", "date", "time"});
        HEADER_KEYWORDS.put("technical training remedial start date", new String[]{"technical", "training", "remedial", "start", "date"});
        HEADER_KEYWORDS.put("technical training remedial end date", new String[]{"technical", "training", "remedial", "end", "date"});
        HEADER_KEYWORDS.put("sme remedial start date", new String[]{"sme", "remedial", "start", "date"});
        HEADER_KEYWORDS.put("sme remedial end date", new String[]{"sme", "remedial", "end", "date"});
        HEADER_KEYWORDS.put("tentative/actual supply date", new String[]{"tentative", "actual", "supply", "date"});
        HEADER_KEYWORDS.put("proposed project id", new String[]{"proposed", "project", "id"});
        HEADER_KEYWORDS.put("proposed project name", new String[]{"proposed", "project", "name"});
        HEADER_KEYWORDS.put("brief description", new String[]{"brief", "description"});
        HEADER_KEYWORDS.put("external trainer id", new String[]{"external", "trainer", "id"});
        HEADER_KEYWORDS.put("external trainer name", new String[]{"external", "trainer", "name"});
        HEADER_KEYWORDS.put("internal trainer 1 id", new String[]{"internal", "trainer", "1", "id"});
        HEADER_KEYWORDS.put("internal trainer 2 id", new String[]{"internal", "trainer", "2", "id"});
        HEADER_KEYWORDS.put("internal trainer 3 id", new String[]{"internal", "trainer", "3", "id"});
        HEADER_KEYWORDS.put("internal trainer 4 id", new String[]{"internal", "trainer", "4", "id"});
        HEADER_KEYWORDS.put("internal trainer 5 id", new String[]{"internal", "trainer", "5", "id"});
        HEADER_KEYWORDS.put("internal trainer 6 id", new String[]{"internal", "trainer", "6", "id"});
        HEADER_KEYWORDS.put("rto location", new String[]{"rto", "location"});
        HEADER_KEYWORDS.put("asl updated date", new String[]{"asl", "updated", "date"});
        HEADER_KEYWORDS.put("sm poc", new String[]{"sm", "poc"});
        HEADER_KEYWORDS.put("asl/exit process phase", new String[]{"asl", "exit", "process", "phase"});
        HEADER_KEYWORDS.put("bu project allocation date", new String[]{"bu", "project", "allocation", "date"});
        HEADER_KEYWORDS.put("sl engagement category", new String[]{"sl", "engagement", "category"});
        HEADER_KEYWORDS.put("platform cohort vs non platform cohort", new String[]{"platform", "cohort", "non", "platform", "cohort"});
        HEADER_KEYWORDS.put("is stage 1 applicable", new String[]{"stage", "1", "applicable"});
        HEADER_KEYWORDS.put("sl lead id", new String[]{"sl", "lead", "id"});
        HEADER_KEYWORDS.put("bu pm id", new String[]{"bu", "pm", "id"});
        HEADER_KEYWORDS.put("house name", new String[]{"house", "name"});
        HEADER_KEYWORDS.put("breach 1 category", new String[]{"breach", "1", "category"});
        HEADER_KEYWORDS.put("breach 1 category remarks", new String[]{"breach", "1", "category", "remarks"});
        HEADER_KEYWORDS.put("breach 2 category", new String[]{"breach", "2", "category"});
        HEADER_KEYWORDS.put("breach 2 category remarks", new String[]{"breach", "2", "category", "remarks"});
        HEADER_KEYWORDS.put("breach 3 category", new String[]{"breach", "3", "category"});
        HEADER_KEYWORDS.put("breach 3 category remarks", new String[]{"breach", "3", "category", "remarks"});
        HEADER_KEYWORDS.put("current location", new String[]{"current", "location"});
        HEADER_KEYWORDS.put("location change", new String[]{"location", "change"});
        HEADER_KEYWORDS.put("older track name", new String[]{"older", "track", "name"});
        HEADER_KEYWORDS.put("interim technical sme id", new String[]{"interim", "technical", "sme", "id"});
        HEADER_KEYWORDS.put("interim project and/or sba sme id", new String[]{"interim", "project", "sba", "sme", "id"});
        HEADER_KEYWORDS.put("final technical evaluation attempt 1 sme id", new String[]{"final", "technical", "evaluation", "attempt", "1", "sme", "id"});
        HEADER_KEYWORDS.put("final project and/or sba attempt 1 sme id", new String[]{"final", "project", "sba", "attempt", "1", "sme", "id"});
        HEADER_KEYWORDS.put("final technical evaluation attempt 2 sme id", new String[]{"final", "technical", "evaluation", "attempt", "2", "sme", "id"});
        HEADER_KEYWORDS.put("final project and/or sba attempt 2 sme id", new String[]{"final", "project", "sba", "attempt", "2", "sme", "id"});
        HEADER_KEYWORDS.put("interim technical score", new String[]{"interim", "technical", "score"});
        HEADER_KEYWORDS.put("interim project and/or sba score", new String[]{"interim", "project", "sba", "score"});
        HEADER_KEYWORDS.put("final technical evaluation attempt 1 score", new String[]{"final", "technical", "evaluation", "attempt", "1", "score"});
        HEADER_KEYWORDS.put("final project and/or sba attempt 1 score", new String[]{"final", "project", "sba", "attempt", "1", "score"});
        HEADER_KEYWORDS.put("final technical evaluation attempt 2 score", new String[]{"final", "technical", "evaluation", "attempt", "2", "score"});
        HEADER_KEYWORDS.put("final project and/or sba attempt 2 score", new String[]{"final", "project", "sba", "attempt", "2", "score"});
        HEADER_KEYWORDS.put("interim evaluation planned date", new String[]{"interim", "evaluation", "planned", "date"});
        HEADER_KEYWORDS.put("interim sme id", new String[]{"interim", "sme", "id"});
        HEADER_KEYWORDS.put("interim rag", new String[]{"interim", "rag"});
        HEADER_KEYWORDS.put("interim evaluation actual date", new String[]{"interim", "evaluation", "actual", "date"});
        HEADER_KEYWORDS.put("interim evaluation feedback", new String[]{"interim", "evaluation", "feedback"});
        HEADER_KEYWORDS.put("final attempt 1 planned date", new String[]{"final", "attempt", "1", "planned", "date"});
        HEADER_KEYWORDS.put("final attempt 1 sme id", new String[]{"final", "attempt", "1", "sme", "id"});
        HEADER_KEYWORDS.put("final attempt 1 rag", new String[]{"final", "attempt", "1", "rag"});
        HEADER_KEYWORDS.put("final attempt 1 actual date", new String[]{"final", "attempt", "1", "actual", "date"});
        HEADER_KEYWORDS.put("final attempt 1 evaluation feedback", new String[]{"final", "attempt", "1", "evaluation", "feedback"});
        HEADER_KEYWORDS.put("final attempt 2 sme id", new String[]{"final", "attempt", "2", "sme", "id"});
        HEADER_KEYWORDS.put("final attempt 2 rag", new String[]{"final", "attempt", "2", "rag"});
        HEADER_KEYWORDS.put("final attempt 2 actual date", new String[]{"final", "attempt", "2", "actual", "date"});
        HEADER_KEYWORDS.put("final attempt 2 evaluation feedback", new String[]{"final", "attempt", "2", "evaluation", "feedback"});
        HEADER_KEYWORDS.put("final special evaluation sme id", new String[]{"final", "special", "evaluation", "sme", "id"});
        HEADER_KEYWORDS.put("final special evaluation rag", new String[]{"final", "special", "evaluation", "rag"});
        HEADER_KEYWORDS.put("final special evaluation actual date", new String[]{"final", "special", "evaluation", "actual", "date"});
        HEADER_KEYWORDS.put("final special evaluation feedback", new String[]{"final", "special", "evaluation", "feedback"});
        HEADER_KEYWORDS.put("final reattempt special evaluation sme id", new String[]{"final", "reattempt", "special", "evaluation", "sme", "id"});
        HEADER_KEYWORDS.put("final reattempt special evaluation rag", new String[]{"final", "reattempt", "special", "evaluation", "rag"});
        HEADER_KEYWORDS.put("final reattempt special evaluation actual date", new String[]{"final", "reattempt", "special", "evaluation", "actual", "date"});
        HEADER_KEYWORDS.put("final reattempt special evaluation feedback", new String[]{"final", "reattempt", "special", "evaluation", "feedback"});
        HEADER_KEYWORDS.put("stage 1 evaluation sme id", new String[]{"stage", "1", "evaluation", "sme", "id"});
        HEADER_KEYWORDS.put("stage 1 evaluation rag", new String[]{"stage", "1", "evaluation", "rag"});
        HEADER_KEYWORDS.put("stage 1 evaluation actual date", new String[]{"stage", "1", "evaluation", "actual", "date"});
        HEADER_KEYWORDS.put("stage 1 evaluation feedback", new String[]{"stage", "1", "evaluation", "feedback"});
        HEADER_KEYWORDS.put("phs-rag", new String[]{"phs", "rag"});
        HEADER_KEYWORDS.put("qualifier/stage 1/delta attempt 1 planned date", new String[]{"qualifier", "stage", "1", "delta", "attempt", "1", "planned", "date"});
        HEADER_KEYWORDS.put("qualifier/stage 1/delta attempt 1 completion date", new String[]{"qualifier", "stage", "1", "delta", "attempt", "1", "completion", "date"});
        HEADER_KEYWORDS.put("qualifier/stage 1/delta attempt 1 score", new String[]{"qualifier", "stage", "1", "delta", "attempt", "1", "score"});
        HEADER_KEYWORDS.put("qualifier/stage 1/delta attempt 1 status", new String[]{"qualifier", "stage", "1", "delta", "attempt", "1", "status"});
        HEADER_KEYWORDS.put("qualifier/stage 1/delta attempt 2 planned date", new String[]{"qualifier", "stage", "1", "delta", "attempt", "2", "planned", "date"});
        HEADER_KEYWORDS.put("qualifier/stage 1/delta attempt 2 completion date", new String[]{"qualifier", "stage", "1", "delta", "attempt", "2", "completion", "date"});
        HEADER_KEYWORDS.put("qualifier/stage1/delta attempt 2 score", new String[]{"qualifier", "stage1", "delta", "attempt", "2", "score"});
        HEADER_KEYWORDS.put("qualifier/stage 1/delta attempt 2 status", new String[]{"qualifier", "stage", "1", "delta", "attempt", "2", "status"});
        HEADER_KEYWORDS.put("qualifier/stage 1/delta attempt 3 planned date", new String[]{"qualifier", "stage", "1", "delta", "attempt", "3", "planned", "date"});
        HEADER_KEYWORDS.put("qualifier/stage 1/delta attempt 3 completion date", new String[]{"qualifier", "stage", "1", "delta", "attempt", "3", "completion", "date"});
        HEADER_KEYWORDS.put("qualifier/stage 1/delta attempt 3 score", new String[]{"qualifier", "stage", "1", "delta", "attempt", "3", "score"});
        HEADER_KEYWORDS.put("qualifier/stage 1/delta attempt 3 status", new String[]{"qualifier", "stage", "1", "delta", "attempt", "3", "status"});
        HEADER_KEYWORDS.put("attendance health score", new String[]{"attendance", "health", "score"});
        HEADER_KEYWORDS.put("language assessment score", new String[]{"language", "assessment", "score"});
        HEADER_KEYWORDS.put("average of handson", new String[]{"average", "handson"});
        HEADER_KEYWORDS.put("average of assess type-1", new String[]{"average", "assess", "type", "1"});
        HEADER_KEYWORDS.put("average of assess type-2", new String[]{"average", "assess", "type", "2"});
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
            Row headerRow = sheet.getRow(0);

            if (headerRow == null) {
                throw new RuntimeException("No header row found in Excel file");
            }

            // Build a map of header names to column indices
            Map<String, Integer> headerIndexMap = new HashMap<>();
            for (Cell cell : headerRow) {
                String header = getCellAsString(cell).toLowerCase().trim();
                if (!header.isEmpty()) {
                    headerIndexMap.put(header, cell.getColumnIndex());
                }
            }

            // ✅ Actual data starts after header rows
            for (int i = 1; i <= sheet.getLastRowNum(); i++) {

                Row row = sheet.getRow(i);
                if (row == null) continue;

                // Get the name column (using header map for flexibility)
                String name = getCellAsStringByHeader(row, headerIndexMap, "name");
                if (name.isEmpty()) continue; // skip empty rows

                Candidate candidate = new Candidate();

                try {
                    // Extract basic candidate information using header map
                    candidate.setCognizantCandidateId(getCellAsIntegerByHeader(row, headerIndexMap, "cognizant candidate id"));
                    candidate.setAssociateId(getCellAsIntegerByHeader(row, headerIndexMap, "associate id"));
                    candidate.setCandidateName(getCellAsStringByHeader(row, headerIndexMap, "name"));
                    candidate.setCognizantEmailID(getCellAsStringByHeader(row, headerIndexMap, "cognizant email id"));
                    candidate.setGender(getCellAsStringByHeader(row, headerIndexMap, "gender"));
                    candidate.setDeploymentLocation(getCellAsStringByHeader(row, headerIndexMap, "deployment location"));
                    candidate.setTrackName(getCellAsStringByHeader(row, headerIndexMap, "track name (as curriculum)"));
                    candidate.setCohortCode(getCellAsStringByHeader(row, headerIndexMap, "cohort code"));
                    candidate.setDoj(getCellAsLocalDateByHeader(row, headerIndexMap, "doj"));


                    // Create and set CandidateScore
                    CandidateScore candidateScore = new CandidateScore();
                    candidateScore.setPerformanceScore(getCellAsDoubleByHeader(row, headerIndexMap, "performance health score"));
                    candidateScore.setAttendanceScore(getCellAsDoubleByHeader(row, headerIndexMap, "attendance health score"));
                    candidateScore.setLanguageScore(getCellAsStringByHeader(row, headerIndexMap, "language assessment score"));
                    candidateScore.setInterimScore(getCellAsStringByHeader(row, headerIndexMap, "interim rag"));
                    String finalRag1=getCellAsStringByHeader(row, headerIndexMap, "final attempt 1 rag");
                    String finalRag2=getCellAsStringByHeader(row, headerIndexMap, "final attempt 2 rag");
                    String finalRag= finalRag2.length()==0?finalRag1:finalRag2;
                    System.out.println(finalRag+"  --- "+finalRag1+"  ---  "+ finalRag2);
                    System.out.println(finalRag.length()+"  --- "+finalRag1.length()+"  ---  "+ finalRag2.length());
                    candidateScore.setFinalScore(finalRag);
                    candidateScore.setInterimEvaluationFeedback(getCellAsStringByHeader(row, headerIndexMap, "interim evaluation feedback"));
                    candidateScore.setFinalEvaluationFeedback(getCellAsStringByHeader(row, headerIndexMap, "final attempt 1 evaluation feedback"));
                    if(finalRag.equalsIgnoreCase("Green")){
                        candidateScore.setReadiness("Ready");
                    }

                    // Set bidirectional relationship
                    candidateScore.setCandidate(candidate);
                    candidate.setCandidateScore(candidateScore);

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

    // Helper method to get cell value as String by header name
    private static String getCellAsStringByHeader(Row row, Map<String, Integer> headerIndexMap, String headerName) {
        Integer colIndex = headerIndexMap.get(headerName);
        if (colIndex == null) {
            return "";
        }
        Cell cell = row.getCell(colIndex);
        return getCellAsString(cell);
    }

    // Helper method to get cell value as Integer by header name
    private static Integer getCellAsIntegerByHeader(Row row, Map<String, Integer> headerIndexMap, String headerName) {
        Integer colIndex = headerIndexMap.get(headerName);
        if (colIndex == null) {
            return null;
        }
        Cell cell = row.getCell(colIndex);
        return getCellAsInteger(cell);
    }
    private static LocalDate getCellAsLocalDateByHeader(
            Row row,
            Map<String, Integer> headerIndexMap,
            String headerName) {

        Integer colIndex = headerIndexMap.get(headerName);
        if (colIndex == null) {
            return null;
        }

        Cell cell = row.getCell(colIndex);
        return getCellAsLocalDate(cell);
    }

    // Helper method to get cell value as Double by header name
    private static Double getCellAsDoubleByHeader(Row row, Map<String, Integer> headerIndexMap, String headerName) {
        Integer colIndex = headerIndexMap.get(headerName);
        if (colIndex == null) {
            return null;
        }
        Cell cell = row.getCell(colIndex);
        return getCellAsDouble(cell);
    }



    private static LocalDate getCellAsLocalDate(Cell cell) {
        if (cell == null) return null;

        if (cell.getCellType() == CellType.NUMERIC && DateUtil.isCellDateFormatted(cell)) {
            return cell.getDateCellValue()
                    .toInstant()
                    .atZone(ZoneId.systemDefault())
                    .toLocalDate();
        }

        // Optional: handle string dates if Excel has text
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
