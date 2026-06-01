package com.cts.service;

import com.cts.entity.Achievement;
import com.cts.entity.Candidate;
import com.cts.repository.AchievementRepository;
import com.cts.repository.CandidateRepository;
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
class AchievementServiceTest {

    @Mock AchievementRepository achievementRepository;
    @Mock CandidateRepository candidateRepository;
    @InjectMocks AchievementService achievementService;

    private Achievement achievement(Integer id, String type, String title, String desc) {
        Achievement a = new Achievement();
        a.setAId(id);
        a.setType(type);
        a.setTitle(title);
        a.setDescription(desc);
        return a;
    }

    // ── add ──────────────────────────────────────────────────────────────

    @Test
    @DisplayName("✓ addAchievement() links the candidate and saves")
    void add_happy_path() {
        Candidate cand = new Candidate();
        cand.setAssociateId(2308322);
        when(candidateRepository.findById(2308322)).thenReturn(Optional.of(cand));
        when(achievementRepository.save(any(Achievement.class))).thenAnswer(inv -> {
            Achievement a = inv.getArgument(0);
            a.setAId(7);
            return a;
        });

        Achievement saved = achievementService.addAchievement(
                achievement(null, "ACHIEVEMENT", "Hackathon Win", "1st"), 2308322);

        assertThat(saved.getAId()).isEqualTo(7);
        assertThat(saved.getCandidate()).isSameAs(cand);
    }

    @Test
    @DisplayName("✗ addAchievement() throws when candidate missing")
    void add_no_candidate() {
        when(candidateRepository.findById(404)).thenReturn(Optional.empty());

        assertThatThrownBy(() ->
                achievementService.addAchievement(achievement(null, "X", "Y", "Z"), 404))
                .isInstanceOf(RuntimeException.class);

        verify(achievementRepository, never()).save(any());
    }

    // ── get ──────────────────────────────────────────────────────────────

    @Test
    @DisplayName("✓ getAchievement() returns persisted entity")
    void get_found() {
        when(achievementRepository.findById(5)).thenReturn(Optional.of(
                achievement(5, "ACTIVITY", "Tech Talk", "Gave a talk")));

        Achievement a = achievementService.getAchievement(5);
        assertThat(a.getTitle()).isEqualTo("Tech Talk");
    }

    @Test
    @DisplayName("✗ getAchievement() throws when not found")
    void get_not_found() {
        when(achievementRepository.findById(404)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> achievementService.getAchievement(404))
                .isInstanceOf(RuntimeException.class);
    }

    // ── update ───────────────────────────────────────────────────────────

    @Test
    @DisplayName("✓ updateAchievement() patches only the provided non-null fields")
    void update_patches_provided_fields() {
        Achievement existing = achievement(8, "ACHIEVEMENT", "Old Title", "Old Desc");
        when(achievementRepository.findById(8)).thenReturn(Optional.of(existing));
        when(achievementRepository.save(any(Achievement.class))).thenAnswer(inv -> inv.getArgument(0));

        Achievement patch = new Achievement();
        patch.setTitle("New Title");
        // description null on patch — must keep old

        Achievement result = achievementService.updateAchievement(patch, 8);

        assertThat(result.getTitle()).isEqualTo("New Title");
        assertThat(result.getDescription()).isEqualTo("Old Desc");
    }

    @Test
    @DisplayName("✗ updateAchievement() throws when not found")
    void update_not_found() {
        when(achievementRepository.findById(404)).thenReturn(Optional.empty());
        assertThatThrownBy(() ->
                achievementService.updateAchievement(new Achievement(), 404))
                .isInstanceOf(RuntimeException.class);
    }

    // ── delete ───────────────────────────────────────────────────────────

    @Test
    @DisplayName("✓ deleteAchievement() removes the entity")
    void delete_happy_path() {
        Achievement existing = achievement(11, "X", "Y", "Z");
        when(achievementRepository.findById(11)).thenReturn(Optional.of(existing));

        achievementService.deleteAchievement(11);

        verify(achievementRepository).delete(existing);
    }

    @Test
    @DisplayName("✗ deleteAchievement() throws when not found")
    void delete_not_found() {
        when(achievementRepository.findById(404)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> achievementService.deleteAchievement(404))
                .isInstanceOf(RuntimeException.class);
        verify(achievementRepository, never()).delete(any());
    }
}
