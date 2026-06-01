package com.cts.repository;

import com.cts.entity.Candidate;
import com.cts.entity.Certification;
import com.cts.entity.Skills;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.List;

public final class CandidateSpecifications {

    private CandidateSpecifications() {}

    private static boolean isBlank(String s) {
        return s == null || s.trim().isEmpty();
    }

    private static String pattern(String s) {
        return "%" + s.toLowerCase().trim() + "%";
    }

    /** AND-combined LIKE on Skills programmings — each chip must match. */
    public static Specification<Candidate> hasProgrammingSkills(List<String> skills) {
        return skillsLikeAll(skills, "programmings");
    }

    public static Specification<Candidate> hasToolSkills(List<String> skills) {
        return skillsLikeAll(skills, "tools");
    }

    public static Specification<Candidate> hasFrameworkSkills(List<String> skills) {
        return skillsLikeAll(skills, "frameworks");
    }

    private static Specification<Candidate> skillsLikeAll(List<String> skills, String field) {
        return (root, query, cb) -> {
            if (skills == null || skills.isEmpty()) return cb.conjunction();
            Join<Candidate, Skills> sj = root.join("skills", JoinType.LEFT);
            Predicate[] preds = skills.stream()
                    .filter(s -> !isBlank(s))
                    .map(s -> cb.like(cb.lower(sj.get(field)), pattern(s)))
                    .toArray(Predicate[]::new);
            return preds.length == 0 ? cb.conjunction() : cb.and(preds);
        };
    }

    /** Matches when ANY associated certification's name OR provider contains the text. */
    public static Specification<Candidate> hasCertificate(String certificate) {
        return (root, query, cb) -> {
            if (isBlank(certificate)) return cb.conjunction();
            if (query != null) query.distinct(true);
            Join<Candidate, Certification> cj = root.join("certificates", JoinType.LEFT);
            String p = pattern(certificate);
            return cb.or(
                    cb.like(cb.lower(cj.get("certificationName")), p),
                    cb.like(cb.lower(cj.get("certificationProvider")), p)
            );
        };
    }

    public static Specification<Candidate> hasCohortCode(String cohortCode) {
        return (root, query, cb) -> {
            if (isBlank(cohortCode)) return cb.conjunction();
            return cb.like(cb.lower(root.get("cohortCode")), pattern(cohortCode));
        };
    }

    /**
     * Multi-value SL filter — OR-combined within the list (case-insensitive equality).
     * A blank / null / all-blank list reduces to cb.conjunction() so it composes
     * cleanly with the other AND-chained specs.
     */
    public static Specification<Candidate> hasSls(List<String> sls) {
        return (root, query, cb) -> {
            if (sls == null || sls.isEmpty()) return cb.conjunction();
            List<String> cleaned = sls.stream()
                    .filter(s -> !isBlank(s))
                    .map(s -> s.toLowerCase().trim())
                    .distinct()
                    .toList();
            if (cleaned.isEmpty()) return cb.conjunction();
            Predicate[] preds = cleaned.stream()
                    .map(v -> cb.equal(cb.lower(root.get("sl")), v))
                    .toArray(Predicate[]::new);
            return preds.length == 1 ? preds[0] : cb.or(preds);
        };
    }

    public static Specification<Candidate> hasDeploymentLocation(String location) {
        return (root, query, cb) -> {
            if (isBlank(location)) return cb.conjunction();
            return cb.like(cb.lower(root.get("deploymentLocation")), pattern(location));
        };
    }

    /**
     * Match against one or more Associate IDs (SQL IN clause). Null / empty list
     * / all-blank list reduces to a no-op (conjunction) so this composes cleanly
     * with the other AND-chained specs.
     */
    public static Specification<Candidate> hasAssociateId(List<Integer> associateIds) {
        return (root, query, cb) -> {
            if (associateIds == null || associateIds.isEmpty()) return cb.conjunction();
            List<Integer> cleaned = associateIds.stream()
                    .filter(java.util.Objects::nonNull)
                    .filter(i -> i > 0)
                    .distinct()
                    .toList();
            if (cleaned.isEmpty()) return cb.conjunction();
            if (cleaned.size() == 1) return cb.equal(root.get("associateId"), cleaned.get(0));
            return root.get("associateId").in(cleaned);
        };
    }

    
}
