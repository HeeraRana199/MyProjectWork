package com.cts.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.cts.entity.Candidate;

@Repository
public interface CandidateRepository extends JpaRepository<Candidate, Integer>{

}