package com.cts.util;

import com.cts.entity.Candidate;
import com.cts.entity.CandidateScore;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * Validates a single Candidate (and its CandidateScore) parsed from Excel.
 * Returns a list of column-scoped errors so the ingestion log knows exactly
 * which field failed and why.
 */
public final class CandidateValidator {

    private CandidateValidator() {}

    /** A column-level validation failure. */
    public static record FieldError(String column, String message) {
        @Override
        public String toString() {
            return column + ": " + message;
        }
    }

    private static final Set<String> VALID_GENDERS =
            Set.of("male", "female", "other", "m", "f", "o");

    private static final Set<String> VALID_RAG =
            Set.of("green", "amber", "red");

    /** Language proficiency grades from CandidateScore (A1 best → F fail). */
    private static final Set<String> VALID_LANGUAGE_GRADES =
            Set.of("a1", "a2", "b1", "b2", "c1", "c2", "d1", "d2", "e1", "e2", "f");

    /** Cognizant emails — relaxed to allow .com and .co subdomains. */
    private static final Pattern COGNIZANT_EMAIL =
            Pattern.compile("^[A-Za-z0-9._%+-]+@cognizant\\.[A-Za-z.]{2,}$");

    /** Names: letters, spaces, hyphens, apostrophes, dots — i.e. real human names. */
    private static final Pattern NAME = Pattern.compile("^[A-Za-z .'\\-]+$");

    private static boolean isBlank(String s) {
        return s == null || s.trim().isEmpty();
    }

    public static List<FieldError> validate(Candidate candidate) {
        List<FieldError> errors = new ArrayList<>();
        if (candidate == null) {
            errors.add(new FieldError("candidate", "row is empty"));
            return errors;
        }

        // ── identity fields ────────────────────────────────────────────────
        if (candidate.getAssociateId() == null) {
            errors.add(new FieldError("Associate Id", "must not be empty"));
        } else if (candidate.getAssociateId() <= 0) {
            errors.add(new FieldError("Associate Id", "must be a positive integer"));
        }

        // Cognizant Candidate ID validation commented out per project decision
        // if (candidate.getCognizantCandidateId() == null) {
        //     errors.add(new FieldError("Cognizant Candidate ID", "must not be empty"));
        // } else if (candidate.getCognizantCandidateId() <= 0) {
        //     errors.add(new FieldError("Cognizant Candidate ID", "must be a positive integer"));
        // }

        // ── name ───────────────────────────────────────────────────────────
        if (isBlank(candidate.getCandidateName())) {
            errors.add(new FieldError("Name", "must not be empty"));
        } else if (!NAME.matcher(candidate.getCandidateName().trim()).matches()) {
            errors.add(new FieldError("Name", "must contain only letters, spaces, hyphens, apostrophes"));
        }

        // ── email ──────────────────────────────────────────────────────────
        if (isBlank(candidate.getCognizantEmailID())) {
            errors.add(new FieldError("Cognizant Email ID", "must not be empty"));
        } else if (!COGNIZANT_EMAIL.matcher(candidate.getCognizantEmailID().trim()).matches()) {
            errors.add(new FieldError("Cognizant Email ID",
                    "must be a valid Cognizant email (e.g. firstname.lastname@cognizant.com)"));
        }

        // ── gender ─────────────────────────────────────────────────────────
        if (isBlank(candidate.getGender())) {
            errors.add(new FieldError("Gender", "must not be empty"));
        } else if (!VALID_GENDERS.contains(candidate.getGender().trim().toLowerCase())) {
            errors.add(new FieldError("Gender", "must be one of: Male, Female, Other (got '"
                    + candidate.getGender() + "')"));
        }

        // ── cohort / location / track / DOJ ────────────────────────────────
        if (isBlank(candidate.getCohortCode())) {
            errors.add(new FieldError("Cohort Code", "must not be empty"));
        }
        if (isBlank(candidate.getDeploymentLocation())) {
            errors.add(new FieldError("Deployment Location", "must not be empty"));
        }
        if (isBlank(candidate.getTrackName())) {
            errors.add(new FieldError("Track name (As Curriculum)", "must not be empty"));
        }
        if (candidate.getDoj() == null) {
            errors.add(new FieldError("DOJ", "must not be empty / unparseable"));
        }

        // ── CandidateScore (optional fields with allowed values) ───────────
        CandidateScore score = candidate.getCandidateScore();
        if (score == null) {
            errors.add(new FieldError("CandidateScore", "must not be null"));
        } else {
            if (score.getAttendanceScore() != null &&
                    (score.getAttendanceScore() < 0 || score.getAttendanceScore() > 100)) {
                errors.add(new FieldError("Attendance Health Score", "must be between 0 and 100"));
            }
            if (!isBlank(score.getInterimScore()) &&
                    !VALID_RAG.contains(score.getInterimScore().trim().toLowerCase())) {
                errors.add(new FieldError("Interim RAG",
                        "must be one of: Green, Amber, Red (got '" + score.getInterimScore() + "')"));
            }
            if (!isBlank(score.getFinalScore()) &&
                    !VALID_RAG.contains(score.getFinalScore().trim().toLowerCase())) {
                errors.add(new FieldError("Final Attempt RAG",
                        "must be one of: Green, Amber, Red (got '" + score.getFinalScore() + "')"));
            }
            if (!isBlank(score.getLanguageScore()) &&
                    !VALID_LANGUAGE_GRADES.contains(score.getLanguageScore().trim().toLowerCase())) {
                errors.add(new FieldError("Language Assessment Score",
                        "must be one of: A1..E2, F (got '" + score.getLanguageScore() + "')"));
            }
        }

        return errors;
    }
}
