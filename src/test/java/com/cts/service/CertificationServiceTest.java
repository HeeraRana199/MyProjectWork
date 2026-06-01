package com.cts.service;

import com.cts.entity.Candidate;
import com.cts.entity.Certification;
import com.cts.exceptions.CandidateNotFoundException;
import com.cts.repository.CandidateRepository;
import com.cts.repository.CertificationRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CertificationServiceTest {

    @Mock CertificationRepository certificationRepository;
    @Mock CandidateRepository candidateRepository;
    @InjectMocks CertificationService certificationService;

    private Candidate candidate() {
        Candidate c = new Candidate();
        c.setAssociateId(2308322);
        c.setCertificates(new ArrayList<>());
        return c;
    }

    private Certification cert(String id, String name, String provider, Boolean status) {
        Certification c = new Certification();
        c.setCertificationId(id);
        c.setCertificationName(name);
        c.setCertificationProvider(provider);
        c.setStatus(status);
        return c;
    }

    // ── registerCertification ────────────────────────────────────────────

    @Test
    @DisplayName("✓ registerCertification() links the candidate, forces status=false, persists")
    void register_happy_path() {
        Candidate cand = candidate();
        Certification incoming = cert("AWS-001", "Solutions Architect", "AWS", null);
        when(candidateRepository.findById(2308322)).thenReturn(Optional.of(cand));
        when(candidateRepository.save(any(Candidate.class))).thenAnswer(inv -> inv.getArgument(0));

        Certification result = certificationService.registerCertification(incoming, 2308322);

        assertThat(result.getStatus()).isFalse();
        assertThat(result.getCandidate()).isSameAs(cand);
        assertThat(cand.getCertificates()).hasSize(1);
    }

    @Test
    @DisplayName("✗ registerCertification() throws CandidateNotFoundException for missing candidate")
    void register_no_candidate() {
        when(candidateRepository.findById(404)).thenReturn(Optional.empty());

        assertThatThrownBy(() ->
                certificationService.registerCertification(cert("X", "Y", "Z", null), 404))
                .isInstanceOf(CandidateNotFoundException.class);
    }

    // ── getCertification ─────────────────────────────────────────────────

    @Test
    @DisplayName("✓ getCertification() returns persisted entity")
    void get_found() {
        Certification existing = cert("AZ-900", "Azure Fundamentals", "Microsoft", true);
        when(certificationRepository.findById("AZ-900")).thenReturn(Optional.of(existing));

        Certification c = certificationService.getCertification("AZ-900");
        assertThat(c.getCertificationName()).isEqualTo("Azure Fundamentals");
    }

    @Test
    @DisplayName("✗ getCertification() throws when not found")
    void get_not_found() {
        when(certificationRepository.findById("MISSING")).thenReturn(Optional.empty());
        assertThatThrownBy(() -> certificationService.getCertification("MISSING"))
                .isInstanceOf(RuntimeException.class);
    }

    // ── updateCertification ──────────────────────────────────────────────

    @Test
    @DisplayName("✓ updateCertification() patches only the provided non-null fields")
    void update_patches() {
        Certification existing = cert("AWS-001", "Old Name", "Old Provider", false);
        when(certificationRepository.findById("AWS-001")).thenReturn(Optional.of(existing));
        when(certificationRepository.save(any(Certification.class))).thenAnswer(inv -> inv.getArgument(0));

        Certification patch = new Certification();
        patch.setCertificationName("New Name");
        patch.setStatus(true);
        // provider intentionally null

        Certification result = certificationService.updateCertification(patch, "AWS-001");

        assertThat(result.getCertificationName()).isEqualTo("New Name");
        assertThat(result.getStatus()).isTrue();
        assertThat(result.getCertificationProvider()).isEqualTo("Old Provider");
    }

    @Test
    @DisplayName("✗ updateCertification() throws when not found")
    void update_not_found() {
        when(certificationRepository.findById("MISSING")).thenReturn(Optional.empty());
        assertThatThrownBy(() ->
                certificationService.updateCertification(new Certification(), "MISSING"))
                .isInstanceOf(RuntimeException.class);
    }

    // ── deleteCertification ──────────────────────────────────────────────

    @Test
    @DisplayName("✓ deleteCertification() removes the entity")
    void delete_happy_path() {
        Certification existing = cert("X", "Y", "Z", true);
        when(certificationRepository.findById("X")).thenReturn(Optional.of(existing));

        certificationService.deleteCertification("X");

        verify(certificationRepository).delete(existing);
    }

    @Test
    @DisplayName("✗ deleteCertification() throws when not found")
    void delete_not_found() {
        when(certificationRepository.findById("MISSING")).thenReturn(Optional.empty());
        assertThatThrownBy(() -> certificationService.deleteCertification("MISSING"))
                .isInstanceOf(RuntimeException.class);
        verify(certificationRepository, never()).delete(any());
    }
}
