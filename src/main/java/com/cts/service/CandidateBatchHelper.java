package com.cts.service;

import com.cts.entity.Candidate;
import com.cts.repository.CandidateRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * Tiny helper for committing one Candidate at a time inside its own transaction.
 *
 * Why a separate bean? When the outer Excel-upload method is @Transactional and
 * a single saveAll(...) hits a MySQL unique constraint, Hibernate marks the
 * outer transaction as rollback-only — which makes every subsequent row in the
 * loop fail with "Transaction silently rolled back, marked rollback-only".
 *
 * Routing each row through this helper means its save runs in a
 * Propagation.REQUIRES_NEW sub-transaction:
 *  - Success → committed independently of the outer transaction.
 *  - Failure → rolled back independently; the outer transaction stays clean
 *    and the loop continues processing the remaining rows.
 */
@Service
@RequiredArgsConstructor
public class CandidateBatchHelper {

    private final CandidateRepository candidateRepository;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public Candidate saveOne(Candidate candidate) {
        return candidateRepository.save(candidate);
    }
}
