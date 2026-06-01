package com.cts.controller;

import com.cts.dto.CandidateDto;
import com.cts.entity.Achievement;
import com.cts.entity.Certification;
import com.cts.entity.Project;
import com.cts.entity.Skills;
import com.cts.exceptions.CandidateNotFoundException;
import com.cts.service.AchievementService;
import com.cts.service.CandidateService;
import com.cts.service.CertificationService;
import com.cts.service.ProjectService;
import com.cts.service.SkillsService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class TraineeControllerTest {

    @Mock CandidateService candidateService;
    @Mock CertificationService certificationService;
    @Mock ProjectService projectService;
    @Mock SkillsService skillsService;
    @Mock AchievementService achievementService;
    @InjectMocks TraineeController traineeController;

    private MockMvc mvc;

    @TempDir Path tempDir;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(traineeController, "uploadDir", tempDir.toString());
        mvc = MockMvcBuilders.standaloneSetup(traineeController).build();
    }

    // ── GET /trainee/candidate ──────────────────────────────────────────

    @Test
    @DisplayName("✓ GET /trainee/candidate?id= returns DTO")
    void get_candidate_happy_path() throws Exception {
        CandidateDto d = new CandidateDto();
        d.setAssociateId(2308322);
        d.setCandidateName("Heera");
        when(candidateService.getAssociateById(2308322)).thenReturn(d);

        mvc.perform(get("/trainee/candidate").param("id", "2308322"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.candidateName").value("Heera"));
    }

    @Test
    @DisplayName("✗ GET /trainee/candidate?id= returns 500 when not found")
    void get_candidate_error() throws Exception {
        when(candidateService.getAssociateById(anyInt()))
                .thenThrow(new CandidateNotFoundException("not found"));

        mvc.perform(get("/trainee/candidate").param("id", "404"))
                .andExpect(status().isInternalServerError());
    }

    // ── Certification CRUD ──────────────────────────────────────────────

    @Test
    @DisplayName("✓ POST /trainee/certificate/{associateId} returns 201 ApiResponse")
    void register_certification_happy_path() throws Exception {
        Certification persisted = new Certification();
        persisted.setCertificationId("AWS-001");
        when(certificationService.registerCertification(any(Certification.class), eq(2308322)))
                .thenReturn(persisted);

        mvc.perform(post("/trainee/certificate/{id}", 2308322)
                        .contentType(APPLICATION_JSON)
                        .content("{\"certificationId\":\"AWS-001\",\"certificationName\":\"SA\",\"certificationProvider\":\"AWS\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.code").value(201))
                .andExpect(jsonPath("$.message").value("Certificate is added"));
    }

    @Test
    @DisplayName("✗ POST /trainee/certificate/{associateId} returns 500 when service throws")
    void register_certification_error() throws Exception {
        when(certificationService.registerCertification(any(), anyInt()))
                .thenThrow(new RuntimeException("boom"));

        mvc.perform(post("/trainee/certificate/{id}", 404)
                        .contentType(APPLICATION_JSON)
                        .content("{\"certificationId\":\"X\",\"certificationName\":\"Y\",\"certificationProvider\":\"Z\"}"))
                .andExpect(status().isInternalServerError());
    }

    @Test
    @DisplayName("✓ GET /trainee/certificate/{id} returns the certificate")
    void get_certification_happy_path() throws Exception {
        Certification c = new Certification();
        c.setCertificationId("AZ-900");
        c.setCertificationName("Azure Fundamentals");
        when(certificationService.getCertification("AZ-900")).thenReturn(c);

        mvc.perform(get("/trainee/certificate/{id}", "AZ-900"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.certificationName").value("Azure Fundamentals"));
    }

    @Test
    @DisplayName("✗ GET /trainee/certificate/{id} returns 500 when not found")
    void get_certification_error() throws Exception {
        when(certificationService.getCertification(anyString()))
                .thenThrow(new RuntimeException("not found"));

        mvc.perform(get("/trainee/certificate/{id}", "MISSING"))
                .andExpect(status().isInternalServerError());
    }

    @Test
    @DisplayName("✓ PATCH /trainee/certificate/{id} returns updated certificate")
    void update_certification_happy_path() throws Exception {
        Certification c = new Certification();
        c.setCertificationId("AWS-001");
        c.setCertificationName("New Name");
        when(certificationService.updateCertification(any(Certification.class), eq("AWS-001")))
                .thenReturn(c);

        mvc.perform(patch("/trainee/certificate/{id}", "AWS-001")
                        .contentType(APPLICATION_JSON)
                        .content("{\"certificationName\":\"New Name\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.certificationName").value("New Name"));
    }

    @Test
    @DisplayName("✗ PATCH /trainee/certificate/{id} returns 500 when service throws")
    void update_certification_error() throws Exception {
        when(certificationService.updateCertification(any(), anyString()))
                .thenThrow(new RuntimeException("boom"));

        mvc.perform(patch("/trainee/certificate/{id}", "X")
                        .contentType(APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isInternalServerError());
    }

    @Test
    @DisplayName("✓ DELETE /trainee/certificate/{id} returns 200")
    void delete_certification_happy_path() throws Exception {
        mvc.perform(delete("/trainee/certificate/{id}", "AWS-001"))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("deleted")));

        verify(certificationService).deleteCertification("AWS-001");
    }

    @Test
    @DisplayName("✗ DELETE /trainee/certificate/{id} returns 500 when service throws")
    void delete_certification_error() throws Exception {
        doThrow(new RuntimeException("not found"))
                .when(certificationService).deleteCertification("MISSING");

        mvc.perform(delete("/trainee/certificate/{id}", "MISSING"))
                .andExpect(status().isInternalServerError());
    }

    // ── Project CRUD ────────────────────────────────────────────────────

    @Test
    @DisplayName("✓ POST /trainee/project/{associateId} returns 201 ApiResponse")
    void add_project_happy_path() throws Exception {
        Project p = new Project();
        p.setProjectId(101);
        p.setProjectName("Talent UI");
        when(projectService.addProject(any(Project.class), eq(2308322))).thenReturn(p);

        mvc.perform(post("/trainee/project/{id}", 2308322)
                        .contentType(APPLICATION_JSON)
                        .content("{\"projectName\":\"Talent UI\",\"tech\":\"React\",\"outcome\":\"OK\",\"role\":\"Frontend\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.code").value(201))
                .andExpect(jsonPath("$.message").value("Project is added"));
    }

    @Test
    @DisplayName("✗ POST /trainee/project/{associateId} returns 500 when service throws")
    void add_project_error() throws Exception {
        when(projectService.addProject(any(), anyInt())).thenThrow(new RuntimeException("boom"));

        mvc.perform(post("/trainee/project/{id}", 999)
                        .contentType(APPLICATION_JSON)
                        .content("{\"projectName\":\"x\",\"tech\":\"y\",\"outcome\":\"z\",\"role\":\"r\"}"))
                .andExpect(status().isInternalServerError());
    }

    @Test
    @DisplayName("✓ GET /trainee/project/{id} returns the project entity")
    void get_project_happy_path() throws Exception {
        Project p = new Project();
        p.setProjectId(7);
        p.setProjectName("Card Renderer");
        when(projectService.getProject(7)).thenReturn(p);

        mvc.perform(get("/trainee/project/{id}", 7))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.projectName").value("Card Renderer"));
    }

    @Test
    @DisplayName("✗ GET /trainee/project/{id} returns 500 when not found")
    void get_project_error() throws Exception {
        when(projectService.getProject(anyInt())).thenThrow(new RuntimeException("not found"));

        mvc.perform(get("/trainee/project/{id}", 404))
                .andExpect(status().isInternalServerError());
    }

    @Test
    @DisplayName("✓ PUT /trainee/project/{id} returns 200 ApiResponse")
    void update_project_happy_path() throws Exception {
        Project p = new Project();
        p.setProjectId(8);
        when(projectService.updateProject(any(Project.class), eq(8))).thenReturn(p);

        mvc.perform(put("/trainee/project/{id}", 8)
                        .contentType(APPLICATION_JSON)
                        .content("{\"projectName\":\"New\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("Project is updated"));
    }

    @Test
    @DisplayName("✗ PUT /trainee/project/{id} returns 500 when service throws")
    void update_project_error() throws Exception {
        when(projectService.updateProject(any(), anyInt())).thenThrow(new RuntimeException("boom"));

        mvc.perform(put("/trainee/project/{id}", 404)
                        .contentType(APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isInternalServerError());
    }

    @Test
    @DisplayName("✓ DELETE /trainee/project/{id} returns 200")
    void delete_project_happy_path() throws Exception {
        mvc.perform(delete("/trainee/project/{id}", 11))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("deleted")));

        verify(projectService).deleteProject(11);
    }

    @Test
    @DisplayName("✗ DELETE /trainee/project/{id} returns 500 when service throws")
    void delete_project_error() throws Exception {
        doThrow(new RuntimeException("not found")).when(projectService).deleteProject(404);

        mvc.perform(delete("/trainee/project/{id}", 404))
                .andExpect(status().isInternalServerError());
    }

    // ── Skills ──────────────────────────────────────────────────────────

    @Test
    @DisplayName("✓ PUT /trainee/skill/{associateId} returns 200 ApiResponse")
    void update_skills_happy_path() throws Exception {
        Skills s = new Skills();
        s.setProgrammings("Java");
        when(skillsService.updateSkills(any(Skills.class), eq(2308322))).thenReturn(s);

        mvc.perform(put("/trainee/skill/{id}", 2308322)
                        .contentType(APPLICATION_JSON)
                        .content("{\"programmings\":\"Java\",\"tools\":\"Git\",\"frameworks\":\"Spring\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("Skills are updated"));
    }

    @Test
    @DisplayName("✗ PUT /trainee/skill/{associateId} returns 500 when service throws")
    void update_skills_error() throws Exception {
        when(skillsService.updateSkills(any(), anyInt())).thenThrow(new RuntimeException("boom"));

        mvc.perform(put("/trainee/skill/{id}", 404)
                        .contentType(APPLICATION_JSON)
                        .content("{\"programmings\":\"X\"}"))
                .andExpect(status().isInternalServerError());
    }

    // ── Achievement CRUD ────────────────────────────────────────────────

    @Test
    @DisplayName("✓ POST /trainee/achievement/{associateId} returns 201 ApiResponse")
    void add_achievement_happy_path() throws Exception {
        Achievement a = new Achievement();
        a.setAId(7);
        when(achievementService.addAchievement(any(Achievement.class), eq(2308322))).thenReturn(a);

        mvc.perform(post("/trainee/achievement/{id}", 2308322)
                        .contentType(APPLICATION_JSON)
                        .content("{\"type\":\"ACHIEVEMENT\",\"title\":\"Hackathon Win\",\"description\":\"1st\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.code").value(201))
                .andExpect(jsonPath("$.message").value("Achievement is added"));
    }

    @Test
    @DisplayName("✗ POST /trainee/achievement/{associateId} returns 500 when service throws")
    void add_achievement_error() throws Exception {
        when(achievementService.addAchievement(any(), anyInt())).thenThrow(new RuntimeException("boom"));

        mvc.perform(post("/trainee/achievement/{id}", 404)
                        .contentType(APPLICATION_JSON)
                        .content("{\"type\":\"X\",\"title\":\"Y\",\"description\":\"Z\"}"))
                .andExpect(status().isInternalServerError());
    }

    @Test
    @DisplayName("✓ GET /trainee/achievement/{id} returns the achievement")
    void get_achievement_happy_path() throws Exception {
        Achievement a = new Achievement();
        a.setAId(5);
        a.setTitle("Tech Talk");
        when(achievementService.getAchievement(5)).thenReturn(a);

        mvc.perform(get("/trainee/achievement/{id}", 5))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Tech Talk"));
    }

    @Test
    @DisplayName("✗ GET /trainee/achievement/{id} returns 500 when not found")
    void get_achievement_error() throws Exception {
        when(achievementService.getAchievement(anyInt())).thenThrow(new RuntimeException("not found"));

        mvc.perform(get("/trainee/achievement/{id}", 404))
                .andExpect(status().isInternalServerError());
    }

    @Test
    @DisplayName("✓ PUT /trainee/achievement/{id} returns 200 ApiResponse")
    void update_achievement_happy_path() throws Exception {
        Achievement a = new Achievement();
        a.setAId(8);
        when(achievementService.updateAchievement(any(Achievement.class), eq(8))).thenReturn(a);

        mvc.perform(put("/trainee/achievement/{id}", 8)
                        .contentType(APPLICATION_JSON)
                        .content("{\"title\":\"New Title\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("Achievement is updated"));
    }

    @Test
    @DisplayName("✗ PUT /trainee/achievement/{id} returns 500 when service throws")
    void update_achievement_error() throws Exception {
        when(achievementService.updateAchievement(any(), anyInt())).thenThrow(new RuntimeException("boom"));

        mvc.perform(put("/trainee/achievement/{id}", 404)
                        .contentType(APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isInternalServerError());
    }

    @Test
    @DisplayName("✓ DELETE /trainee/achievement/{id} returns 200")
    void delete_achievement_happy_path() throws Exception {
        mvc.perform(delete("/trainee/achievement/{id}", 11))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("deleted")));

        verify(achievementService).deleteAchievement(11);
    }

    @Test
    @DisplayName("✗ DELETE /trainee/achievement/{id} returns 500 when service throws")
    void delete_achievement_error() throws Exception {
        doThrow(new RuntimeException("not found"))
                .when(achievementService).deleteAchievement(404);

        mvc.perform(delete("/trainee/achievement/{id}", 404))
                .andExpect(status().isInternalServerError());
    }

    // ── Profile photo upload / fetch ────────────────────────────────────

    @Test
    @DisplayName("✓ POST /trainee/profile-photo/{id} stores the JPG and returns 200")
    void upload_profile_photo_happy_path() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file", "me.jpg", "image/jpeg", new byte[]{(byte) 0xFF, (byte) 0xD8});

        mvc.perform(multipart("/trainee/profile-photo/{id}", 2308322L).file(file))
                .andExpect(status().isOk())
                .andExpect(content().string("Uploaded successfully"));

        assert Files.exists(tempDir.resolve("2308322.jpg"));
    }

    @Test
    @DisplayName("✗ POST /trainee/profile-photo/{id} returns 400 when file is empty")
    void upload_profile_photo_empty_file() throws Exception {
        MockMultipartFile file = new MockMultipartFile("file", "me.jpg", "image/jpeg", new byte[0]);

        mvc.perform(multipart("/trainee/profile-photo/{id}", 2308322L).file(file))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Empty file"));
    }

    @Test
    @DisplayName("✓ GET /trainee/profile-photo/{id} serves the JPG when present")
    void get_profile_photo_existing() throws Exception {
        Files.write(tempDir.resolve("123.jpg"), new byte[]{(byte) 0xFF, (byte) 0xD8});

        mvc.perform(get("/trainee/profile-photo/{id}", 123L))
                .andExpect(status().isOk())
                .andExpect(content().contentType("image/jpeg"));
    }

    @Test
    @DisplayName("✓ GET /trainee/profile-photo/{id} falls back to default jpg when missing")
    void get_profile_photo_default() throws Exception {
        Files.write(tempDir.resolve("000000.jpg"), new byte[]{1});

        mvc.perform(get("/trainee/profile-photo/{id}", 999L))
                .andExpect(status().isOk())
                .andExpect(content().contentType("image/jpeg"));
    }
}
