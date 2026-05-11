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

    public static Specification<Candidate> hasDeploymentLocation(String location) {
        return (root, query, cb) -> {
            if (isBlank(location)) return cb.conjunction();
            return cb.like(cb.lower(root.get("deploymentLocation")), pattern(location));
        };
    }
}
