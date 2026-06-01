package com.cts.util;

import com.cts.entity.Candidate;
import com.cts.entity.CandidateScore;
import com.cts.util.CandidateValidator.FieldError;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class CandidateValidatorTest {

    /** Build a fully-valid Candidate the tests can mutate one field at a time. */
    private Candidate validCandidate() {
        Candidate c = new Candidate();
        c.setAssociateId(2308322);
        // c.setCognizantCandidateId(100023); // commented out per project decision
        c.setCandidateName("John Doe");
        c.setCognizantEmailID("john.doe@cognizant.com");
        c.setGender("Male");
        c.setCohortCode("GENC-2024-12");
        c.setDeploymentLocation("Bangalore");
        c.setTrackName("Full Stack");
        c.setDoj(LocalDate.of(2024, 1, 15));

        CandidateScore s = new CandidateScore();
        s.setAttendanceScore(95.0);
        s.setLanguageScore("A1");
        s.setInterimScore("GREEN");
        s.setFinalScore("GREEN");
        c.setCandidateScore(s);
        return c;
    }

    private List<String> columns(List<FieldError> errors) {
        return errors.stream().map(FieldError::column).toList();
    }

    // ── Positive ─────────────────────────────────────────────────────────

    @Test
    @DisplayName("✓ Fully valid candidate produces no validation errors")
    void valid_candidate_passes() {
        List<FieldError> errors = CandidateValidator.validate(validCandidate());
        assertThat(errors).isEmpty();
    }

    @Test
    @DisplayName("✓ Optional score fields can be null without producing errors")
    void optional_score_fields_can_be_null() {
        Candidate c = validCandidate();
        c.getCandidateScore().setAttendanceScore(null);
        c.getCandidateScore().setLanguageScore(null);
        c.getCandidateScore().setInterimScore(null);
        c.getCandidateScore().setFinalScore(null);

        assertThat(CandidateValidator.validate(c)).isEmpty();
    }

    @ParameterizedTest(name = "✓ Gender ''{0}'' is accepted")
    @ValueSource(strings = {"Male", "female", "Other", "M", "F", "o"})
    void valid_gender_variations(String gender) {
        Candidate c = validCandidate();
        c.setGender(gender);
        assertThat(columns(CandidateValidator.validate(c))).doesNotContain("Gender");
    }

    @ParameterizedTest(name = "✓ Language grade ''{0}'' is accepted")
    @ValueSource(strings = {"A1", "A2", "B1", "B2", "C1", "C2", "D1", "D2", "E1", "E2", "F", "a1", "f"})
    void valid_language_grades(String grade) {
        Candidate c = validCandidate();
        c.getCandidateScore().setLanguageScore(grade);
        assertThat(columns(CandidateValidator.validate(c))).doesNotContain("Language Assessment Score");
    }

    @ParameterizedTest(name = "✓ RAG ''{0}'' is accepted")
    @ValueSource(strings = {"GREEN", "Amber", "red", "green"})
    void valid_rag_scores(String rag) {
        Candidate c = validCandidate();
        c.getCandidateScore().setInterimScore(rag);
        c.getCandidateScore().setFinalScore(rag);
        List<String> cols = columns(CandidateValidator.validate(c));
        assertThat(cols).doesNotContain("Interim RAG", "Final Attempt RAG");
    }

    // ── Negative ─────────────────────────────────────────────────────────

    @Nested
    class IdentityNegatives {

        @Test
        @DisplayName("✗ associateId null → error")
        void null_associate_id() {
            Candidate c = validCandidate();
            c.setAssociateId(null);
            assertThat(columns(CandidateValidator.validate(c))).contains("Associate Id");
        }

        @Test
        @DisplayName("✗ associateId zero/negative → error")
        void non_positive_associate_id() {
            Candidate c = validCandidate();
            c.setAssociateId(0);
            assertThat(columns(CandidateValidator.validate(c))).contains("Associate Id");

            c.setAssociateId(-5);
            assertThat(columns(CandidateValidator.validate(c))).contains("Associate Id");
        }

        // Cognizant Candidate ID tests commented out per project decision
        // @Test
        // @DisplayName("✗ cognizantCandidateId null → error")
        // void null_cognizant_id() {
        //     Candidate c = validCandidate();
        //     c.setCognizantCandidateId(null);
        //     assertThat(columns(CandidateValidator.validate(c))).contains("Cognizant Candidate ID");
        // }
        //
        // @Test
        // @DisplayName("✗ cognizantCandidateId non-positive → error")
        // void non_positive_cognizant_id() {
        //     Candidate c = validCandidate();
        //     c.setCognizantCandidateId(-1);
        //     assertThat(columns(CandidateValidator.validate(c))).contains("Cognizant Candidate ID");
        // }
    }

    @Nested
    class NameNegatives {
        @Test
        @DisplayName("✗ blank name → error")
        void blank_name() {
            Candidate c = validCandidate();
            c.setCandidateName("   ");
            assertThat(columns(CandidateValidator.validate(c))).contains("Name");
        }

        @Test
        @DisplayName("✗ name with digits → error")
        void numeric_name() {
            Candidate c = validCandidate();
            c.setCandidateName("John42");
            assertThat(columns(CandidateValidator.validate(c))).contains("Name");
        }

        @Test
        @DisplayName("✗ name with special chars → error")
        void special_char_name() {
            Candidate c = validCandidate();
            c.setCandidateName("John@Doe");
            assertThat(columns(CandidateValidator.validate(c))).contains("Name");
        }
    }

    @Nested
    class EmailNegatives {
        @Test
        @DisplayName("✗ blank email → error")
        void blank_email() {
            Candidate c = validCandidate();
            c.setCognizantEmailID("");
            assertThat(columns(CandidateValidator.validate(c))).contains("Cognizant Email ID");
        }

        @Test
        @DisplayName("✗ email with wrong domain → error")
        void wrong_domain_email() {
            Candidate c = validCandidate();
            c.setCognizantEmailID("john.doe@gmail.com");
            assertThat(columns(CandidateValidator.validate(c))).contains("Cognizant Email ID");
        }

        @Test
        @DisplayName("✗ malformed email → error")
        void malformed_email() {
            Candidate c = validCandidate();
            c.setCognizantEmailID("not-an-email");
            assertThat(columns(CandidateValidator.validate(c))).contains("Cognizant Email ID");
        }
    }

    @Nested
    class CategoricalNegatives {
        @Test
        @DisplayName("✗ unknown gender → error")
        void invalid_gender() {
            Candidate c = validCandidate();
            c.setGender("Unknown");
            assertThat(columns(CandidateValidator.validate(c))).contains("Gender");
        }

        @Test
        @DisplayName("✗ blank required text fields → all reported")
        void blank_required_fields() {
            Candidate c = validCandidate();
            c.setCohortCode("");
            c.setDeploymentLocation("");
            c.setTrackName("");
            assertThat(columns(CandidateValidator.validate(c)))
                    .contains("Cohort Code", "Deployment Location", "Track name (As Curriculum)");
        }

        @Test
        @DisplayName("✗ null DOJ → error")
        void null_doj() {
            Candidate c = validCandidate();
            c.setDoj(null);
            assertThat(columns(CandidateValidator.validate(c))).contains("DOJ");
        }

        @Test
        @DisplayName("✗ unknown RAG values → both reported")
        void invalid_rag() {
            Candidate c = validCandidate();
            c.getCandidateScore().setInterimScore("BLUE");
            c.getCandidateScore().setFinalScore("PURPLE");
            assertThat(columns(CandidateValidator.validate(c)))
                    .contains("Interim RAG", "Final Attempt RAG");
        }

        @Test
        @DisplayName("✗ unknown language grade → error")
        void invalid_language_grade() {
            Candidate c = validCandidate();
            c.getCandidateScore().setLanguageScore("Z9");
            assertThat(columns(CandidateValidator.validate(c)))
                    .contains("Language Assessment Score");
        }
    }

    @Nested
    class ScoreRangeNegatives {
        @Test
        @DisplayName("✗ attendance score > 100 → error")
        void attendance_too_high() {
            Candidate c = validCandidate();
            c.getCandidateScore().setAttendanceScore(150.0);
            assertThat(columns(CandidateValidator.validate(c))).contains("Attendance Health Score");
        }

        @Test
        @DisplayName("✗ attendance score < 0 → error")
        void attendance_negative() {
            Candidate c = validCandidate();
            c.getCandidateScore().setAttendanceScore(-1.0);
            assertThat(columns(CandidateValidator.validate(c))).contains("Attendance Health Score");
        }

        @Test
        @DisplayName("✗ attendance score out of range → error")
        void attendance_out_of_range() {
            Candidate c = validCandidate();
            c.getCandidateScore().setAttendanceScore(250.0);
            assertThat(columns(CandidateValidator.validate(c))).contains("Attendance Health Score");
        }
    }

    @Test
    @DisplayName("✗ null candidate is gracefully reported, not NPE")
    void null_candidate() {
        List<FieldError> errors = CandidateValidator.validate(null);
        assertThat(errors).isNotEmpty();
        assertThat(errors.get(0).column()).isEqualTo("candidate");
    }

    @Test
    @DisplayName("✗ candidateScore null → error")
    void null_score() {
        Candidate c = validCandidate();
        c.setCandidateScore(null);
        assertThat(columns(CandidateValidator.validate(c))).contains("CandidateScore");
    }

    @Test
    @DisplayName("✗ multiple invalid fields produce multiple FieldErrors")
    void multiple_errors_aggregated() {
        Candidate c = validCandidate();
        c.setAssociateId(null);
        c.setCandidateName("");
        c.setGender("xx");
        c.getCandidateScore().setFinalScore("RAINBOW");

        List<FieldError> errors = CandidateValidator.validate(c);
        assertThat(errors).hasSizeGreaterThanOrEqualTo(4);
        assertThat(columns(errors))
                .contains("Associate Id", "Name", "Gender", "Final Attempt RAG");
    }
}
