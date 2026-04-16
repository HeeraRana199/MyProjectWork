package com.cts.service;

import java.io.InputStream;
import java.util.List;

import com.cts.dto.CandidateDto;
import com.cts.exceptions.CandidateNotFoundException;
import com.cts.mapper.CandidateRowMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.cts.entity.Candidate;
import com.cts.repository.CandidateRepository;
import com.cts.util.CandidateExcelHelper;

import lombok.AllArgsConstructor;

@Service
@AllArgsConstructor
public class CandidateService {

    private CandidateRepository candidateRepository;
    private CandidateRowMapper candidateRowMapper;

    //Create candidate logic
    public Candidate addCandidate(Candidate candidate) {
        return candidateRepository.save(candidate);
    }

    //Get candidate info logic
    @Transactional
    public CandidateDto getCandidateById(int candidateId) {
        Candidate candidate = candidateRepository.findById(candidateId)
                .orElseThrow(() -> new CandidateNotFoundException("Candidate not found"));

        return candidateRowMapper.convertToCandidateDto(candidate);
    }

    //Excel Upload Logic
    public List<Candidate> saveCandidatesFromExcel(MultipartFile file) {
        try {
            InputStream is = file.getInputStream();
            List<Candidate> candidates = CandidateExcelHelper.excelToCandidates(is);

            // Bulk insert (efficient)
            return candidateRepository.saveAll(candidates);

        } catch (Exception e) {
            throw new RuntimeException("Failed to store Excel data", e);
        }
    }
}