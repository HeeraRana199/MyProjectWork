package com.cts.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.cts.entity.Candidate;

import java.util.List;
import java.util.Optional;


@Repository
public interface CandidateRepository extends JpaRepository<Candidate, Integer>, JpaSpecificationExecutor<Candidate> {

    Optional<Candidate> findByCognizantEmailID(String email);
    Page<Candidate> findByCohortCode(String cohortCode, Pageable pageable);

    Page<Candidate> findByDeploymentLocation(String deploymentLocation, Pageable pageable);


    @Query("""
        SELECT c
        FROM Candidate c
        JOIN c.skills s
        WHERE 
            LOWER(s.programmings) LIKE LOWER(CONCAT('%', :skill, '%'))
            OR LOWER(s.tools) LIKE LOWER(CONCAT('%', :skill, '%'))
            OR LOWER(s.frameworks) LIKE LOWER(CONCAT('%', :skill, '%'))
        """)
    Page<Candidate>  findBySkillsToolsContainingIgnoreCaseOrSkillsProgrammingsContainingIgnoreCaseOrSkillsFrameworksContainingIgnoreCase(String skill, Pageable pageable);

    @Query("""
        SELECT c
        FROM Candidate c
        JOIN c.certificates s
        WHERE 
            LOWER(s.certificationName) LIKE LOWER(CONCAT('%', :certificates, '%'))
            OR LOWER(s.certificationProvider) LIKE LOWER(CONCAT('%', :certificates, '%'))
        """)
   Page<Candidate> findByCertificates(String certificates, Pageable pageable);
//
//    List<Candidate> findByCohortCode(String cohortCode, Pageable pageable);

}