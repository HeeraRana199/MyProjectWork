package com.cts.service;

import com.cts.entity.Candidate;
import com.cts.entity.Skills;
import com.cts.exceptions.CandidateNotFoundException;
import com.cts.repository.CandidateRepository;
import com.cts.repository.SkillsRepository;
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
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SkillsServiceTest {

    @Mock SkillsRepository skillsRepository;
    @Mock CandidateRepository candidateRepository;
    @InjectMocks SkillsService skillsService;

    @Test
    @DisplayName("✓ updateSkills() creates a new Skills row when candidate has none")
    void creates_skills_when_missing() {
        Candidate cand = new Candidate();
        cand.setAssociateId(1);
        when(candidateRepository.findById(1)).thenReturn(Optional.of(cand));
        when(candidateRepository.save(any(Candidate.class))).thenAnswer(inv -> inv.getArgument(0));

        Skills incoming = new Skills();
        incoming.setProgrammings("Java");
        incoming.setTools("Git");
        incoming.setFrameworks("Spring");

        Skills result = skillsService.updateSkills(incoming, 1);

        assertThat(result.getProgrammings()).isEqualTo("Java");
        assertThat(result.getTools()).isEqualTo("Git");
        assertThat(result.getFrameworks()).isEqualTo("Spring");
        assertThat(cand.getSkills()).isSameAs(result);
    }

    @Test
    @DisplayName("✓ updateSkills() replaces non-null fields on existing Skills")
    void patches_existing_skills() {
        Skills existing = new Skills();
        existing.setProgrammings("OldLang");
        existing.setTools("OldTool");
        existing.setFrameworks("OldFramework");

        Candidate cand = new Candidate();
        cand.setAssociateId(2);
        cand.setSkills(existing);
        when(candidateRepository.findById(2)).thenReturn(Optional.of(cand));
        when(candidateRepository.save(any(Candidate.class))).thenAnswer(inv -> inv.getArgument(0));

        Skills patch = new Skills();
        patch.setProgrammings("NewLang");
        // tools / frameworks null on incoming — must NOT clear existing values

        Skills result = skillsService.updateSkills(patch, 2);

        assertThat(result.getProgrammings()).isEqualTo("NewLang");
        assertThat(result.getTools()).isEqualTo("OldTool");
        assertThat(result.getFrameworks()).isEqualTo("OldFramework");
    }

    @Test
    @DisplayName("✗ updateSkills() throws CandidateNotFoundException for missing candidate")
    void throws_when_candidate_missing() {
        when(candidateRepository.findById(404)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> skillsService.updateSkills(new Skills(), 404))
                .isInstanceOf(CandidateNotFoundException.class);
    }
}
