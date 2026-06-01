package com.cts.controller;

import com.cts.entity.IngestionError;
import com.cts.entity.IngestionLog;
import com.cts.entity.User;
import com.cts.exceptions.CandidateNotFoundException;
import com.cts.service.AuthService;
import com.cts.service.CandidateService;
import com.cts.service.IngestionLogService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class AdminControllerTest {

    @Mock CandidateService candidateService;
    @Mock AuthService authService;
    @Mock IngestionLogService ingestionLogService;
    @InjectMocks AdminController adminController;

    private MockMvc mvc;
    private final ObjectMapper om = new ObjectMapper();

    @TempDir Path tempDir;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(adminController, "uploadDir", tempDir.toString());
        mvc = MockMvcBuilders.standaloneSetup(adminController).build();
    }

    // ── POST /admin/leaderRegister ──────────────────────────────────────

    @Test
    @DisplayName("✓ POST /admin/leaderRegister returns 201 with new leader")
    void leader_register_happy_path() throws Exception {
        User input = new User();
        input.setEmail("leader@cts.com");
        input.setPassword("p");

        User saved = new User();
        saved.setUserId(10L);
        saved.setEmail("leader@cts.com");
        saved.setRole(User.Role.ROLE_LEADER);
        when(authService.leaderRegister(any(User.class))).thenReturn(saved);

        mvc.perform(post("/admin/leaderRegister")
                        .contentType(APPLICATION_JSON)
                        .content(om.writeValueAsString(input)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.userId").value(10))
                .andExpect(jsonPath("$.role").value("ROLE_LEADER"));
    }

    @Test
    @DisplayName("✗ POST /admin/leaderRegister returns 409 on duplicate email")
    void leader_register_duplicate_email() throws Exception {
        when(authService.leaderRegister(any(User.class)))
                .thenThrow(new DataIntegrityViolationException("duplicate key"));

        mvc.perform(post("/admin/leaderRegister")
                        .contentType(APPLICATION_JSON)
                        .content("{\"email\":\"dup@cts.com\",\"password\":\"x\"}"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("A leader with that email already exists."));
    }

    @Test
    @DisplayName("✗ POST /admin/leaderRegister returns 500 on unexpected error")
    void leader_register_internal_error() throws Exception {
        when(authService.leaderRegister(any(User.class)))
                .thenThrow(new RuntimeException("boom"));

        mvc.perform(post("/admin/leaderRegister")
                        .contentType(APPLICATION_JSON)
                        .content("{\"email\":\"x@y.com\",\"password\":\"x\"}"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.containsString("boom")));
    }

    // ── GET /admin/leaders ──────────────────────────────────────────────

    @Test
    @DisplayName("✓ GET /admin/leaders returns the leader list")
    void list_leaders_happy_path() throws Exception {
        User l1 = new User(); l1.setUserId(1L); l1.setEmail("a@cts.com"); l1.setRole(User.Role.ROLE_LEADER);
        User l2 = new User(); l2.setUserId(2L); l2.setEmail("b@cts.com"); l2.setRole(User.Role.ROLE_LEADER);
        when(authService.listLeaders()).thenReturn(List.of(l2, l1));

        mvc.perform(get("/admin/leaders"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].userId").value(2))
                .andExpect(jsonPath("$[1].userId").value(1));
    }

    @Test
    @DisplayName("✗ GET /admin/leaders returns 500 when service throws")
    void list_leaders_error() throws Exception {
        when(authService.listLeaders()).thenThrow(new RuntimeException("db dead"));

        mvc.perform(get("/admin/leaders"))
                .andExpect(status().isInternalServerError());
    }

    // ── DELETE /admin/leader/{userId} ───────────────────────────────────

    @Test
    @DisplayName("✓ DELETE /admin/leader/{id} returns 200 with userId echoed")
    void delete_leader_happy_path() throws Exception {
        mvc.perform(delete("/admin/leader/{id}", 42))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").value(42))
                .andExpect(jsonPath("$.message").value("Leader deleted"));

        verify(authService).deleteLeader(42L);
    }

    @Test
    @DisplayName("✗ DELETE /admin/leader/{id} returns 400 when the account isn't a leader")
    void delete_leader_bad_request() throws Exception {
        doThrow(new IllegalArgumentException("User is not a leader"))
                .when(authService).deleteLeader(99L);

        mvc.perform(delete("/admin/leader/{id}", 99))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("User is not a leader"));
    }

    @Test
    @DisplayName("✗ DELETE /admin/leader/{id} returns 500 on unexpected error")
    void delete_leader_internal_error() throws Exception {
        doThrow(new RuntimeException("boom")).when(authService).deleteLeader(99L);

        mvc.perform(delete("/admin/leader/{id}", 99))
                .andExpect(status().isInternalServerError());
    }

    // ── POST /admin/candidate/upload ────────────────────────────────────

    @Test
    @DisplayName("✓ POST /admin/candidate/upload returns 200 with stats when rows were processed")
    void upload_excel_happy_path() throws Exception {
        CandidateService.ExcelUploadResult result = new CandidateService.ExcelUploadResult();
        result.setTotalRecords(10);
        result.setSavedRecords(8);
        result.setMergedRecords(1);
        result.setRejectedRecords(1);
        when(candidateService.saveCandidatesFromExcel(any(), any())).thenReturn(result);

        MockMultipartFile file = new MockMultipartFile(
                "file", "candidates.xlsx",
                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
                new byte[]{1, 2, 3});

        mvc.perform(multipart("/admin/candidate/upload").file(file))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalRecords").value(10))
                .andExpect(jsonPath("$.savedRecords").value(8))
                .andExpect(jsonPath("$.rejectedRecords").value(1));
    }

    @Test
    @DisplayName("✗ POST /admin/candidate/upload returns 400 when no rows were parseable")
    void upload_excel_empty_result_is_400() throws Exception {
        CandidateService.ExcelUploadResult result = new CandidateService.ExcelUploadResult();
        result.setTotalRecords(0);
        result.getErrors().add("Missing column: Email");
        when(candidateService.saveCandidatesFromExcel(any(), any())).thenReturn(result);

        MockMultipartFile file = new MockMultipartFile(
                "file", "bad.xlsx",
                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
                new byte[]{0});

        mvc.perform(multipart("/admin/candidate/upload").file(file))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.totalRecords").value(0))
                .andExpect(jsonPath("$.errors[0]").value("Missing column: Email"));
    }

    @Test
    @DisplayName("✗ POST /admin/candidate/upload returns 500 when service throws unexpectedly")
    void upload_excel_internal_error() throws Exception {
        when(candidateService.saveCandidatesFromExcel(any(), any()))
                .thenThrow(new RuntimeException("io error"));

        MockMultipartFile file = new MockMultipartFile(
                "file", "x.xlsx",
                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
                new byte[]{0});

        mvc.perform(multipart("/admin/candidate/upload").file(file))
                .andExpect(status().isInternalServerError())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("io error")));
    }

    // ── GET /admin/candidate?id= ────────────────────────────────────────

    @Test
    @DisplayName("✓ GET /admin/candidate?id= returns the candidate DTO")
    void get_associate_by_id_happy_path() throws Exception {
        com.cts.dto.CandidateDto dto = new com.cts.dto.CandidateDto();
        dto.setAssociateId(2308322);
        dto.setCandidateName("Heera");
        when(candidateService.getAssociateById(2308322)).thenReturn(dto);

        mvc.perform(get("/admin/candidate").param("id", "2308322"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.associateId").value(2308322))
                .andExpect(jsonPath("$.candidateName").value("Heera"));
    }

    @Test
    @DisplayName("✗ GET /admin/candidate?id= returns 500 when the service throws (not-found)")
    void get_associate_by_id_not_found_returns_500() throws Exception {
        when(candidateService.getAssociateById(anyInt()))
                .thenThrow(new CandidateNotFoundException("Candidate not found"));

        mvc.perform(get("/admin/candidate").param("id", "404"))
                .andExpect(status().isInternalServerError())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Candidate not found")));
    }

    // ── GET /admin/allcandidates ────────────────────────────────────────

    @Test
    @DisplayName("✓ GET /admin/allcandidates returns paginated content")
    void list_candidates_happy_path() throws Exception {
        CandidateService.PaginatedCandidatesResponse resp =
                new CandidateService.PaginatedCandidatesResponse(List.of(), 0, 10, 0, 0, true);
        when(candidateService.getAllCandidatesPaginated(0, null)).thenReturn(resp);

        mvc.perform(get("/admin/allcandidates"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.currentPage").value(0))
                .andExpect(jsonPath("$.pageSize").value(10))
                .andExpect(jsonPath("$.last").value(true));
    }

    @Test
    @DisplayName("✓ GET /admin/allcandidates honors explicit pageSize")
    void list_candidates_with_size() throws Exception {
        CandidateService.PaginatedCandidatesResponse resp =
                new CandidateService.PaginatedCandidatesResponse(List.of(), 1, 25, 0, 0, true);
        when(candidateService.getAllCandidatesPaginated(1, 25)).thenReturn(resp);

        mvc.perform(get("/admin/allcandidates").param("page", "1").param("pageSize", "25"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.pageSize").value(25));
    }

    @Test
    @DisplayName("✗ GET /admin/allcandidates returns 500 when service throws")
    void list_candidates_error() throws Exception {
        when(candidateService.getAllCandidatesPaginated(anyInt(), any()))
                .thenThrow(new RuntimeException("db dead"));

        mvc.perform(get("/admin/allcandidates"))
                .andExpect(status().isInternalServerError());
    }

    // ── GET /admin/profile-photo/{id} ───────────────────────────────────

    @Test
    @DisplayName("✓ GET /admin/profile-photo/{id} serves the JPG when it exists")
    void profile_photo_existing() throws Exception {
        Files.write(tempDir.resolve("123.jpg"), new byte[]{(byte) 0xFF, (byte) 0xD8});

        mvc.perform(get("/admin/profile-photo/{id}", 123))
                .andExpect(status().isOk())
                .andExpect(content().contentType("image/jpeg"));
    }

    @Test
    @DisplayName("✓ GET /admin/profile-photo/{id} falls back to default jpg when not found")
    void profile_photo_fallback_default() throws Exception {
        Files.write(tempDir.resolve("000000.jpg"), new byte[]{1});

        mvc.perform(get("/admin/profile-photo/{id}", 999))
                .andExpect(status().isOk())
                .andExpect(content().contentType("image/jpeg"));
    }

    // ── GET /admin/ingestion-logs ───────────────────────────────────────

    @Test
    @DisplayName("✓ GET /admin/ingestion-logs returns the log list")
    void list_ingestion_logs_happy_path() throws Exception {
        IngestionLog log = new IngestionLog();
        log.setId(1L);
        log.setFileName("a.xlsx");
        log.setStatus(IngestionLog.Status.SUCCESS);
        when(ingestionLogService.getAllLogs()).thenReturn(List.of(log));

        mvc.perform(get("/admin/ingestion-logs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].status").value("SUCCESS"));
    }

    @Test
    @DisplayName("✗ GET /admin/ingestion-logs returns 500 on failure")
    void list_ingestion_logs_error() throws Exception {
        when(ingestionLogService.getAllLogs()).thenThrow(new RuntimeException("dead"));

        mvc.perform(get("/admin/ingestion-logs"))
                .andExpect(status().isInternalServerError());
    }

    // ── GET /admin/ingestion-logs/{id} ──────────────────────────────────

    @Test
    @DisplayName("✓ GET /admin/ingestion-logs/{id} returns log + errors envelope")
    void get_ingestion_log_details_happy_path() throws Exception {
        IngestionLog log = new IngestionLog(); log.setId(7L);
        IngestionError err = new IngestionError(); err.setId(11L);
        when(ingestionLogService.getLog(7L)).thenReturn(log);
        when(ingestionLogService.getErrorsForLog(7L)).thenReturn(List.of(err));

        mvc.perform(get("/admin/ingestion-logs/{id}", 7))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.log.id").value(7))
                .andExpect(jsonPath("$.errors[0].id").value(11));
    }

    @Test
    @DisplayName("✗ GET /admin/ingestion-logs/{id} returns 404 when log doesn't exist")
    void get_ingestion_log_details_not_found() throws Exception {
        when(ingestionLogService.getLog(404L)).thenReturn(null);

        mvc.perform(get("/admin/ingestion-logs/{id}", 404))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Ingestion log not found"));
    }

    // ── DELETE /admin/candidate/{id} ────────────────────────────────────

    @Test
    @DisplayName("✓ DELETE /admin/candidate/{id} returns 200")
    void delete_candidate_happy_path() throws Exception {
        mvc.perform(delete("/admin/candidate/{id}", 2308322))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("deleted")));

        verify(candidateService).deleteCandidateById(2308322);
    }

    @Test
    @DisplayName("✗ DELETE /admin/candidate/{id} returns 500 when service throws")
    void delete_candidate_error() throws Exception {
        doThrow(new CandidateNotFoundException("not found"))
                .when(candidateService).deleteCandidateById(404);

        mvc.perform(delete("/admin/candidate/{id}", 404))
                .andExpect(status().isInternalServerError())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("not found")));
    }
}
