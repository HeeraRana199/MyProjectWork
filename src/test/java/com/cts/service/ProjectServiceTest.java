package com.cts.service;

import com.cts.entity.Candidate;
import com.cts.entity.Project;
import com.cts.repository.CandidateRepository;
import com.cts.repository.ProjectRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProjectServiceTest {

    @Mock ProjectRepository projectRepository;
    @Mock CandidateRepository candidateRepository;
    @InjectMocks ProjectService projectService;

    private Candidate candidate(Integer id) {
        Candidate c = new Candidate();
        c.setAssociateId(id);
        return c;
    }

    private Project project(Integer projectId, String name, String tech, String outcome, String role) {
        Project p = new Project();
        p.setProjectId(projectId);
        p.setProjectName(name);
        p.setTech(tech);
        p.setOutcome(outcome);
        p.setRole(role);
        return p;
    }

    // ── addProject ───────────────────────────────────────────────────────

    @Test
    @DisplayName("✓ addProject() links the candidate and saves")
    void add_project_happy_path() {
        Candidate cand = candidate(2308322);
        Project incoming = project(null, "Talent UI", "React", "Built UI", "Frontend Dev");

        when(candidateRepository.findById(2308322)).thenReturn(Optional.of(cand));
        when(projectRepository.save(any(Project.class))).thenAnswer(inv -> {
            Project p = inv.getArgument(0);
            p.setProjectId(101);
            return p;
        });

        Project saved = projectService.addProject(incoming, 2308322);

        assertThat(saved.getProjectId()).isEqualTo(101);
        assertThat(saved.getCandidate()).isSameAs(cand);
        verify(projectRepository).save(incoming);
    }

    @Test
    @DisplayName("✗ addProject() throws when candidate doesn't exist")
    void add_project_no_candidate() {
        when(candidateRepository.findById(999)).thenReturn(Optional.empty());

        assertThatThrownBy(() ->
                projectService.addProject(project(null, "X", "T", "O", "R"), 999))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("candidate not found");

        verify(projectRepository, never()).save(any());
    }

    // ── getProject ───────────────────────────────────────────────────────

    @Test
    @DisplayName("✓ getProject() returns the persisted entity")
    void get_project_found() {
        Project existing = project(7, "Card Renderer", "Java", "Done", "Backend Dev");
        when(projectRepository.findById(7)).thenReturn(Optional.of(existing));

        Project p = projectService.getProject(7);
        assertThat(p.getProjectId()).isEqualTo(7);
        assertThat(p.getProjectName()).isEqualTo("Card Renderer");
    }

    @Test
    @DisplayName("✗ getProject() throws when not found")
    void get_project_not_found() {
        when(projectRepository.findById(404)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> projectService.getProject(404))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("not found");
    }

    // ── updateProject ────────────────────────────────────────────────────

    @Test
    @DisplayName("✓ updateProject() patches only the provided fields")
    void update_project_patches_only_provided_fields() {
        Project existing = project(8, "Old Name", "Old Tech", "Old Outcome", "Old Role");
        when(projectRepository.findById(8)).thenReturn(Optional.of(existing));
        when(projectRepository.save(any(Project.class))).thenAnswer(inv -> inv.getArgument(0));

        Project patch = new Project();
        patch.setProjectName("New Name");
        patch.setOutcome("New Outcome");
        // tech & role intentionally null — must be left alone

        Project result = projectService.updateProject(patch, 8);

        assertThat(result.getProjectName()).isEqualTo("New Name");
        assertThat(result.getOutcome()).isEqualTo("New Outcome");
        assertThat(result.getTech()).isEqualTo("Old Tech");
        assertThat(result.getRole()).isEqualTo("Old Role");
    }

    @Test
    @DisplayName("✗ updateProject() throws when project doesn't exist")
    void update_project_not_found() {
        when(projectRepository.findById(404)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> projectService.updateProject(new Project(), 404))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("not found");
    }

    // ── deleteProject ────────────────────────────────────────────────────

    @Test
    @DisplayName("✓ deleteProject() removes the entity")
    void delete_project_happy_path() {
        Project existing = project(11, "Doomed Project", "Tech", "Out", "Role");
        when(projectRepository.findById(11)).thenReturn(Optional.of(existing));

        projectService.deleteProject(11);

        verify(projectRepository).delete(existing);
    }

    @Test
    @DisplayName("✗ deleteProject() throws when project doesn't exist")
    void delete_project_not_found() {
        when(projectRepository.findById(404)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> projectService.deleteProject(404))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("not found");

        verify(projectRepository, never()).delete(any());
    }
}
