package com.cts.service;

import com.cts.dto.CandidateDto;
import com.cts.entity.Candidate;
import com.cts.mapper.CandidateRowMapper;
import com.cts.repository.CandidateRepository;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

//@AllArgsConstructor
@RequiredArgsConstructor
@Service
public class LeaderService {

    private final CandidateRepository candidateRepository;
    private final CandidateRowMapper candidateRowMapper;

    @Value("${app.pagination.page-size:10}")
    private int defaultPageSize;


    //Get paginated candidates logic
    @Transactional
    public CandidateService.PaginatedCandidatesResponse getAllCandidatesPaginated(String searchType, String searchText, int page, Integer pageSize) {
        int size = (pageSize != null && pageSize > 0) ? pageSize : defaultPageSize;
        Pageable pageable = PageRequest.of(page, size);
        Page<Candidate> candidatePage = null;
        searchType=searchType.toLowerCase();
        switch(searchType){
            case "cohortcode":
                candidatePage= candidateRepository.findByCohortCode(searchText,pageable);
                break;

            case "skills":
                candidatePage= candidateRepository.findBySkillsToolsContainingIgnoreCaseOrSkillsProgrammingsContainingIgnoreCaseOrSkillsFrameworksContainingIgnoreCase(searchText,pageable);
                break;

            case "location":
                candidatePage= candidateRepository.findByDeploymentLocation(searchText,pageable);
                break;

            case "certificates":
                candidatePage= candidateRepository.findByCertificates(searchText,pageable);
                break;

        }
        candidateRepository.findAll(pageable);

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
}
