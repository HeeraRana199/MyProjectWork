package com.cts.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.cts.entity.Candidate;

import java.util.Optional;


@Repository
public interface CandidateRepository extends JpaRepository<Candidate, Integer>{

    Optional<Candidate> findByCognizantEmailID(String email);
}