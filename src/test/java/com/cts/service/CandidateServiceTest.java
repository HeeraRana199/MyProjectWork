package com.cts.service;

import com.cts.dto.CandidateDto;
import com.cts.entity.Candidate;
import com.cts.exceptions.CandidateNotFoundException;
import com.cts.mapper.CandidateRowMapper;
import com.cts.repository.CandidateRepository;
import com.cts.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CandidateServiceTest {

    @Mock CandidateRepository candidateRepository;
    @Mock UserRepository userRepository;
    @Mock CandidateRowMapper candidateRowMapper;
    @Mock PasswordEncoder passwordEncoder;
    @Mock IngestionLogService ingestionLogService;
    @Mock CandidateBatchHelper candidateBatchHelper;

    @InjectMocks CandidateService candidateService;

    @BeforeEach
    void setUp() {
        // Inject the @Value field manually since Mockito doesn't process @Value
        ReflectionTestUtils.setField(candidateService, "defaultPageSize", 10);
    }

    private Candidate candidate(int associateId, String name) {
        Candidate c = new Candidate();
        c.setAssociateId(associateId);
        c.setCandidateName(name);
        return c;
    }

    private CandidateDto dto(int associateId, String name) {
        CandidateDto d = new CandidateDto();
        d.setAssociateId(associateId);
        d.setCandidateName(name);
        return d;
    }

    // ── addCandidate ─────────────────────────────────────────────────────

    @Test
    @DisplayName("✓ addCandidate() delegates to repository.save and returns the entity")
    void add_candidate_happy_path() {
        Candidate input = candidate(2308322, "Heera");
        when(candidateRepository.save(input)).thenReturn(input);

        Candidate result = candidateService.addCandidate(input);

        assertThat(result).isSameAs(input);
        verify(candidateRepository, times(1)).save(input);
    }

    // ── getAssociateById ─────────────────────────────────────────────────

    @Test
    @DisplayName("✓ getAssociateById() returns the mapped DTO when candidate exists")
    void get_by_id_happy_path() {
        Candidate entity = candidate(2308322, "Heera");
        CandidateDto mapped = dto(2308322, "Heera");
        when(candidateRepository.findById(2308322)).thenReturn(Optional.of(entity));
        when(candidateRowMapper.convertToCandidateDto(entity)).thenReturn(mapped);

        CandidateDto result = candidateService.getAssociateById(2308322);

        assertThat(result.getAssociateId()).isEqualTo(2308322);
        assertThat(result.getCandidateName()).isEqualTo("Heera");
    }

    @Test
    @DisplayName("✗ getAssociateById() throws CandidateNotFoundException for missing id")
    void get_by_id_not_found() {
        when(candidateRepository.findById(404)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> candidateService.getAssociateById(404))
                .isInstanceOf(CandidateNotFoundException.class)
                .hasMessageContaining("not found");
    }

    // ── getAllCandidates ─────────────────────────────────────────────────

    @Test
    @DisplayName("✓ getAllCandidates() maps every persisted entity to a DTO")
    void get_all_candidates_maps_all() {
        Candidate c1 = candidate(1, "Alice");
        Candidate c2 = candidate(2, "Bob");
        when(candidateRepository.findAll()).thenReturn(List.of(c1, c2));
        when(candidateRowMapper.convertToCandidateDto(c1)).thenReturn(dto(1, "Alice"));
        when(candidateRowMapper.convertToCandidateDto(c2)).thenReturn(dto(2, "Bob"));

        List<CandidateDto> result = candidateService.getAllCandidates();

        assertThat(result).hasSize(2);
        assertThat(result.get(0).getCandidateName()).isEqualTo("Alice");
        assertThat(result.get(1).getCandidateName()).isEqualTo("Bob");
    }

    @Test
    @DisplayName("✓ getAllCandidates() returns empty list when repository is empty")
    void get_all_candidates_empty() {
        when(candidateRepository.findAll()).thenReturn(List.of());

        List<CandidateDto> result = candidateService.getAllCandidates();

        assertThat(result).isEmpty();
    }

    // ── getAllCandidatesPaginated ────────────────────────────────────────

    @Test
    @DisplayName("✓ getAllCandidatesPaginated() honors the explicit pageSize argument")
    void paginated_with_explicit_size() {
        Candidate c1 = candidate(1, "Alice");
        Pageable expected = PageRequest.of(0, 5);
        Page<Candidate> page = new PageImpl<>(List.of(c1), expected, 1);

        when(candidateRepository.findAll(expected)).thenReturn(page);
        when(candidateRowMapper.convertToCandidateDto(c1)).thenReturn(dto(1, "Alice"));

        CandidateService.PaginatedCandidatesResponse resp =
                candidateService.getAllCandidatesPaginated(0, 5);

        assertThat(resp.getContent()).hasSize(1);
        assertThat(resp.getCurrentPage()).isZero();
        assertThat(resp.getPageSize()).isEqualTo(5);
        assertThat(resp.getTotalElements()).isEqualTo(1);
        assertThat(resp.getTotalPages()).isEqualTo(1);
        assertThat(resp.isLast()).isTrue();
    }

    @Test
    @DisplayName("✓ getAllCandidatesPaginated() falls back to defaultPageSize when pageSize is null")
    void paginated_falls_back_to_default_size_when_null() {
        Pageable expected = PageRequest.of(0, 10); // defaultPageSize=10
        Page<Candidate> page = new PageImpl<>(List.of(), expected, 0);
        when(candidateRepository.findAll(expected)).thenReturn(page);

        CandidateService.PaginatedCandidatesResponse resp =
                candidateService.getAllCandidatesPaginated(0, null);

        assertThat(resp.getPageSize()).isEqualTo(10);
        assertThat(resp.getContent()).isEmpty();
    }

    @Test
    @DisplayName("✓ getAllCandidatesPaginated() falls back to defaultPageSize when pageSize is 0 or negative")
    void paginated_falls_back_to_default_size_when_non_positive() {
        Pageable expected = PageRequest.of(2, 10);
        Page<Candidate> page = new PageImpl<>(List.of(), expected, 0);
        when(candidateRepository.findAll(expected)).thenReturn(page);

        CandidateService.PaginatedCandidatesResponse resp =
                candidateService.getAllCandidatesPaginated(2, 0);

        assertThat(resp.getPageSize()).isEqualTo(10);
        assertThat(resp.getCurrentPage()).isEqualTo(2);
    }

    // ── deleteCandidateById ──────────────────────────────────────────────

    @Test
    @DisplayName("✓ deleteCandidateById() removes the candidate when it exists")
    void delete_candidate_happy_path() {
        Candidate existing = candidate(2308322, "Heera");
        when(candidateRepository.findById(2308322)).thenReturn(Optional.of(existing));

        candidateService.deleteCandidateById(2308322);

        verify(candidateRepository, times(1)).delete(existing);
    }

    @Test
    @DisplayName("✗ deleteCandidateById() throws CandidateNotFoundException for unknown id")
    void delete_candidate_not_found() {
        when(candidateRepository.findById(404)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> candidateService.deleteCandidateById(404))
                .isInstanceOf(CandidateNotFoundException.class)
                .hasMessageContaining("404");

        verify(candidateRepository, never()).delete(any(Candidate.class));
    }
}
