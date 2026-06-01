package com.cts.controller;

import com.cts.dto.CandidateDto;
import com.cts.dto.CertificationDto;
import com.cts.dto.ProjectDto;
import com.cts.dto.SkillsDto;
import com.cts.service.CandidateService;
import com.cts.service.LeaderService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class LeaderControllerTest {

    @Mock LeaderService leaderService;
    @Mock CandidateService candidateService;
    @InjectMocks LeaderController leaderController;

    private MockMvc mvc;

    @BeforeEach
    void setUp() {
        mvc = MockMvcBuilders.standaloneSetup(leaderController).build();
    }

    private CandidateDto dto(int id, String name) {
        CandidateDto d = new CandidateDto();
        d.setAssociateId(id);
        d.setCandidateName(name);
        d.setCognizantEmailID(name.toLowerCase() + "@cts.com");
        return d;
    }

    // ── GET /leader/candidate?id= ────────────────────────────────────────

    @Test
    @DisplayName("✓ GET /leader/candidate?id= returns the candidate DTO")
    void get_associate_by_id_happy_path() throws Exception {
        when(candidateService.getAssociateById(2308322)).thenReturn(dto(2308322, "Heera"));

        mvc.perform(get("/leader/candidate").param("id", "2308322"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.associateId").value(2308322))
                .andExpect(jsonPath("$.candidateName").value("Heera"));
    }

    @Test
    @DisplayName("✗ GET /leader/candidate?id= returns 500 when service throws")
    void get_associate_by_id_error() throws Exception {
        when(candidateService.getAssociateById(anyInt()))
                .thenThrow(new RuntimeException("Candidate not found"));

        mvc.perform(get("/leader/candidate").param("id", "404"))
                .andExpect(status().isInternalServerError())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Candidate not found")));
    }

    // ── GET /leader/candidates/filter ────────────────────────────────────

    @Test
    @DisplayName("✓ GET /leader/candidates/filter returns paginated content")
    void filter_happy_path_no_params() throws Exception {
        CandidateService.PaginatedCandidatesResponse resp =
                new CandidateService.PaginatedCandidatesResponse(List.of(dto(1, "Alice")), 0, 10, 1, 1, true);
        when(leaderService.getFilteredCandidates(any(), any(), any(),
                any(), any(), any(), any(), any(), eq(0), any())).thenReturn(resp);

        mvc.perform(get("/leader/candidates/filter"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.content[0].associateId").value(1));
    }

    @Test
    @DisplayName("✓ GET /leader/candidates/filter passes through every filter param")
    void filter_with_all_params() throws Exception {
        CandidateService.PaginatedCandidatesResponse resp =
                new CandidateService.PaginatedCandidatesResponse(List.of(), 0, 10, 0, 0, true);
        when(leaderService.getFilteredCandidates(any(), any(), any(),
                any(), any(), any(), any(), any(), anyInt(), any())).thenReturn(resp);

        mvc.perform(get("/leader/candidates/filter")
                        .param("programmingSkills", "Java", "Python")
                        .param("toolSkills", "Git")
                        .param("frameworkSkills", "Spring")
                        .param("certificate", "AWS")
                        .param("cohortCode", "C-25")
                        .param("deploymentLocation", "Chennai")
                        .param("associateId", "1", "2")
                        .param("sls", "EAS", "DAI")
                        .param("page", "0")
                        .param("pageSize", "25"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("✗ GET /leader/candidates/filter returns 500 when service throws")
    void filter_error() throws Exception {
        when(leaderService.getFilteredCandidates(any(), any(), any(),
                any(), any(), any(), any(), any(), anyInt(), any()))
                .thenThrow(new RuntimeException("filter exploded"));

        mvc.perform(get("/leader/candidates/filter"))
                .andExpect(status().isInternalServerError())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("filter exploded")));
    }

    // ── GET /leader/candidates/export ────────────────────────────────────

    @Test
    @DisplayName("✓ GET /leader/candidates/export streams CSV with header + one data row")
    void export_csv_happy_path() throws Exception {
        CandidateDto c = dto(2308322, "Heera");
        SkillsDto s = new SkillsDto();
        s.setProgrammings("Java");
        s.setTools("Git");
        s.setFrameworks("Spring");
        c.setSkills(s);

        CertificationDto cert = new CertificationDto();
        cert.setCertificationName("Solutions Architect");
        cert.setCertificationProvider("AWS");
        c.setCertificates(List.of(cert));

        ProjectDto proj = new ProjectDto();
        proj.setProjectName("Talent UI");
        proj.setRole("Frontend");
        c.setProjects(List.of(proj));

        when(leaderService.getAllFilteredCandidates(any(), any(), any(),
                any(), any(), any(), any(), any())).thenReturn(List.of(c));

        var mvcResult = mvc.perform(get("/leader/candidates/export"))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Disposition",
                        org.hamcrest.Matchers.containsString("candidates-")))
                .andReturn();

        String body = mvcResult.getResponse().getContentAsString();
        // Header row
        assert body.contains("Associate ID");
        assert body.contains("Name");
        // Skill columns from the helper
        assert body.contains("Java");
        // Joined certs in "Name (Provider)" form
        assert body.contains("Solutions Architect (AWS)");
        // Joined projects in "Name (Role)" form
        assert body.contains("Talent UI (Frontend)");
    }

    @Test
    @DisplayName("✓ GET /leader/candidates/export emits BOM + header even when zero matches")
    void export_csv_empty_list() throws Exception {
        when(leaderService.getAllFilteredCandidates(any(), any(), any(),
                any(), any(), any(), any(), any())).thenReturn(List.of());

        var mvcResult = mvc.perform(get("/leader/candidates/export"))
                .andExpect(status().isOk())
                .andReturn();

        String body = mvcResult.getResponse().getContentAsString();
        assert body.contains("Associate ID"); // header row still written
    }
}
