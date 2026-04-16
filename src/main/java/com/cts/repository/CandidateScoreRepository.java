package com.cts.repository;

import com.cts.entity.CandidateScore;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CandidateScoreRepository extends JpaRepository<CandidateScore, Integer> {
}
