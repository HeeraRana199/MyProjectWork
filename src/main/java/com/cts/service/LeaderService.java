package com.cts.service;

import com.cts.dto.CandidateDto;
import com.cts.entity.Candidate;
import com.cts.mapper.CandidateRowMapper;
import com.cts.repository.CandidateRepository;
import com.cts.repository.CandidateSpecifications;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
public class LeaderService {

    private final CandidateRepository candidateRepository;
    private final CandidateRowMapper candidateRowMapper;

    @Value("${app.pagination.page-size:10}")
    private int defaultPageSize;

    @Transactional
    public CandidateService.PaginatedCandidatesResponse getFilteredCandidates(
            List<String> programmingSkills,
            List<String> toolSkills,
            List<String> frameworkSkills,
            String certificate,
            String cohortCode,
            String deploymentLocation,
            List<Integer> associateIds,
            List<String> sls,
            int page,
            Integer pageSize) {

        int size = (pageSize != null && pageSize > 0) ? pageSize : defaultPageSize;
        Pageable pageable = PageRequest.of(page, size);

        Specification<Candidate> spec = buildSpec(
                programmingSkills, toolSkills, frameworkSkills,
                certificate, cohortCode, deploymentLocation, associateIds, sls);

        Page<Candidate> candidatePage = candidateRepository.findAll(spec, pageable);

        List<CandidateDto> content = candidatePage.getContent().stream()
                .map(candidateRowMapper::convertToCandidateDto)
                .collect(Collectors.toList());

        return new CandidateService.PaginatedCandidatesResponse(
                content,
                page,
                size,
                candidatePage.getTotalElements(),
                candidatePage.getTotalPages(),
                candidatePage.isLast()
        );
    }

    /** Returns every candidate that matches the filters — used for CSV export. */
    @Transactional
    public List<CandidateDto> getAllFilteredCandidates(
            List<String> programmingSkills,
            List<String> toolSkills,
            List<String> frameworkSkills,
            String certificate,
            String cohortCode,
            String deploymentLocation,
            List<Integer> associateIds,
            List<String> sls) {

        Specification<Candidate> spec = buildSpec(
                programmingSkills, toolSkills, frameworkSkills,
                certificate, cohortCode, deploymentLocation, associateIds, sls);

        return candidateRepository.findAll(spec).stream()
                .map(candidateRowMapper::convertToCandidateDto)
                .collect(Collectors.toList());
    }

    private Specification<Candidate> buildSpec(
            List<String> programmingSkills,
            List<String> toolSkills,
            List<String> frameworkSkills,
            String certificate,
            String cohortCode,
            String deploymentLocation,
            List<Integer> associateIds,
            List<String> sls) {

        List<String> prog = programmingSkills == null ? Collections.emptyList() : programmingSkills;
        List<String> tools = toolSkills == null ? Collections.emptyList() : toolSkills;
        List<String> fw = frameworkSkills == null ? Collections.emptyList() : frameworkSkills;

        return Specification
                .where(CandidateSpecifications.hasProgrammingSkills(prog))
                .and(CandidateSpecifications.hasToolSkills(tools))
                .and(CandidateSpecifications.hasFrameworkSkills(fw))
                .and(CandidateSpecifications.hasCertificate(certificate))
                .and(CandidateSpecifications.hasCohortCode(cohortCode))
                .and(CandidateSpecifications.hasDeploymentLocation(deploymentLocation))
                .and(CandidateSpecifications.hasAssociateId(associateIds))
                .and(CandidateSpecifications.hasSls(sls));
    }
}
