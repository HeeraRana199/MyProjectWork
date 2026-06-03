package com.cts.controller;


import com.cts.dto.AchievementDto;
import com.cts.dto.CandidateDto;
import com.cts.dto.CandidateScoreDto;
import com.cts.dto.CertificationDto;
import com.cts.dto.ProjectDto;
import com.cts.dto.SkillsDto;
import com.cts.service.CandidateService;
import com.cts.service.LeaderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AllArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.io.PrintWriter;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

@RestController
@AllArgsConstructor
@RequestMapping("/leader")
@Tag(name = "Leader", description = "Read-only candidate search + CSV export for talent pipeline reviewers. Requires ROLE_LEADER (or ROLE_ADMIN).")
public class LeaderController {
    private static final Logger logger = LoggerFactory.getLogger(LeaderController.class);
    private final LeaderService leaderService;
    private final CandidateService candidateService;

    @GetMapping("/candidate")
    @Operation(summary = "Get a candidate by Associate Id (leader view)",
            description = "Returns the full CandidateDto. Same payload as the admin endpoint but accessible to leaders.")
    public ResponseEntity<?> getAssociateById(@RequestParam int id) {
        logger.info("Leader request to fetch candidate with ID: {}", id);
        try {
            var candidateDto = candidateService.getAssociateById(id);
            return new ResponseEntity<>(candidateDto, HttpStatus.OK);
        } catch (Exception e) {
            logger.error("Error fetching candidate {} for leader", id, e);
            return ResponseEntity.internalServerError().body("Error fetching candidate: " + e.getMessage());
        }
    }


    /**
     * Multi-criteria filter. All non-empty filters are combined with AND.
     * Skill lists (programmingSkills, toolSkills, frameworkSkills) ANDed within type — every chip must match.
     * Pass repeated query params for list values, e.g. ?programmingSkills=Java&programmingSkills=Python
     */
    @GetMapping("/candidates/filter")
    @Operation(summary = "Multi-criteria candidate search",
            description = "All non-empty filters are ANDed. Skill lists (programmingSkills, toolSkills, frameworkSkills) require EVERY chip to match. associateId / sls accept repeated query params, e.g. ?associateId=1&associateId=2. Returns a paginated CandidateDto page.")
    public ResponseEntity<?> filterCandidates(
            @RequestParam(required = false) List<String> programmingSkills,
            @RequestParam(required = false) List<String> toolSkills,
            @RequestParam(required = false) List<String> frameworkSkills,
            @RequestParam(required = false) String certificate,
            @RequestParam(required = false) String cohortCode,
            @RequestParam(required = false) String deploymentLocation,
            @RequestParam(required = false) List<Integer> associateId,
            @RequestParam(required = false) List<String> sls,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(required = false) Integer pageSize) {

        logger.info("Filter candidates - prog: {}, tools: {}, fw: {}, cert: {}, cohort: {}, loc: {}, associateId: {}, sls: {}, page: {}",
                programmingSkills, toolSkills, frameworkSkills, certificate, cohortCode, deploymentLocation, associateId, sls, page);
        try {
            var result = leaderService.getFilteredCandidates(
                    programmingSkills == null ? Collections.emptyList() : programmingSkills,
                    toolSkills == null ? Collections.emptyList() : toolSkills,
                    frameworkSkills == null ? Collections.emptyList() : frameworkSkills,
                    certificate,
                    cohortCode,
                    deploymentLocation,
                    associateId,
                    sls,
                    page,
                    pageSize
            );
            return new ResponseEntity<>(result, HttpStatus.OK);
        } catch (Exception e) {
            logger.error("Error filtering candidates", e);
            return ResponseEntity.internalServerError().body("Error filtering candidates: " + e.getMessage());
        }
    }

    /**
     * Streams all matching candidates (no pagination) as CSV for download.
     * Accepts the same filter params as /candidates/filter.
     */
    @GetMapping("/candidates/export")
    @Operation(summary = "Export matching candidates as CSV",
            description = "Accepts the same filter params as /candidates/filter. Streams a UTF-8 CSV (with BOM for Excel) with all matching rows — no pagination. Filename: candidates-{timestamp}.csv.")
    public void exportCandidates(
            @RequestParam(required = false) List<String> programmingSkills,
            @RequestParam(required = false) List<String> toolSkills,
            @RequestParam(required = false) List<String> frameworkSkills,
            @RequestParam(required = false) String certificate,
            @RequestParam(required = false) String cohortCode,
            @RequestParam(required = false) String deploymentLocation,
            @RequestParam(required = false) List<Integer> associateId,
            @RequestParam(required = false) List<String> sls,
            HttpServletResponse response) throws IOException {

        logger.info("Export candidates - prog: {}, tools: {}, fw: {}, cert: {}, cohort: {}, loc: {}, associateId: {}, sls: {}",
                programmingSkills, toolSkills, frameworkSkills, certificate, cohortCode, deploymentLocation, associateId, sls);

        String timestamp = java.time.LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss"));
        String filename = "candidates-" + timestamp + ".csv";

        response.setContentType("text/csv; charset=UTF-8");
        response.setHeader("Content-Disposition", "attachment; filename=\"" + filename + "\"");
        response.setCharacterEncoding("UTF-8");

        List<CandidateDto> rows = leaderService.getAllFilteredCandidates(
                programmingSkills, toolSkills, frameworkSkills,
                certificate, cohortCode, deploymentLocation, associateId, sls);

        try (PrintWriter writer = response.getWriter()) {
            // BOM so Excel detects UTF-8 correctly
            writer.write('﻿');

            writer.println(String.join(",",
                    "Associate ID", "Name", "Email", "Gender",
                    "Track", "Cohort Code", "SL", "Deployment Location", "Date of Joining",
                    "Programming Skills", "Tool Skills", "Framework Skills",
                    "Attendance Score", "Language Score",
                    "Interim RAG", "Final RAG",
                    "Certifications", "Projects", "Achievements"
            ));

            for (CandidateDto c : rows) {
                SkillsDto s = c.getSkills();
                CandidateScoreDto sc = c.getCandidateScore();

                writer.println(String.join(",",
                        csv(c.getAssociateId()),
                        csv(c.getCandidateName()),
                        csv(c.getCognizantEmailID()),
                        csv(c.getGender()),
                        csv(c.getTrackName()),
                        csv(c.getCohortCode()),
                        csv(c.getSl()),
                        csv(c.getDeploymentLocation()),
                        csv(c.getDoj()),
                        csv(s == null ? "" : s.getProgrammings()),
                        csv(s == null ? "" : s.getTools()),
                        csv(s == null ? "" : s.getFrameworks()),
                        csv(sc == null ? "" : sc.getAttendanceScore()),
                        csv(sc == null ? "" : sc.getLanguageScore()),
                        csv(sc == null ? "" : sc.getInterimScore()),
                        csv(sc == null ? "" : sc.getFinalScore()),
                        csv(joinCertificates(c.getCertificates())),
                        csv(joinProjects(c.getProjects())),
                        csv(joinAchievements(c.getAchievement()))
                ));
            }
            writer.flush();
        }
    }

    /**
     * Build comma-separated cell contents for the certificates column.
     * Format per entry: "Name (Provider)" — falls back to either field if one is blank.
     */
    private static String joinCertificates(List<CertificationDto> certs) {
        if (certs == null || certs.isEmpty()) return "";
        return certs.stream()
                .filter(Objects::nonNull)
                .map(c -> {
                    String name = nullToEmpty(c.getCertificationName()).trim();
                    String prov = nullToEmpty(c.getCertificationProvider()).trim();
                    if (!name.isEmpty() && !prov.isEmpty()) return name + " (" + prov + ")";
                    return name.isEmpty() ? prov : name;
                })
                .filter(s -> !s.isEmpty())
                .collect(java.util.stream.Collectors.joining(", "));
    }

    /** Format per entry: "Project Name (Role)" — name only if role blank. */
    private static String joinProjects(List<ProjectDto> projects) {
        if (projects == null || projects.isEmpty()) return "";
        return projects.stream()
                .filter(Objects::nonNull)
                .map(p -> {
                    String name = nullToEmpty(p.getProjectName()).trim();
                    String role = nullToEmpty(p.getRole()).trim();
                    if (!name.isEmpty() && !role.isEmpty()) return name + " (" + role + ")";
                    return name.isEmpty() ? role : name;
                })
                .filter(s -> !s.isEmpty())
                .collect(java.util.stream.Collectors.joining(", "));
    }

    /** Format per entry: "Title [TYPE]" — title only if type blank. */
    private static String joinAchievements(List<AchievementDto> achievements) {
        if (achievements == null || achievements.isEmpty()) return "";
        return achievements.stream()
                .filter(Objects::nonNull)
                .map(a -> {
                    String title = nullToEmpty(a.getTitle()).trim();
                    String type = nullToEmpty(a.getType()).trim();
                    if (!title.isEmpty() && !type.isEmpty()) return title + " [" + type + "]";
                    return title.isEmpty() ? type : title;
                })
                .filter(s -> !s.isEmpty())
                .collect(java.util.stream.Collectors.joining(", "));
    }

    private static String nullToEmpty(String s) {
        return s == null ? "" : s;
    }

    /** RFC 4180 CSV-safe quoting: wrap in quotes if value contains comma, quote, or newline. */
    private static String csv(Object value) {
        String s = Objects.toString(value, "");
        if (s.isEmpty()) return "";
        if (s.contains(",") || s.contains("\"") || s.contains("\n") || s.contains("\r")) {
            return "\"" + s.replace("\"", "\"\"") + "\"";
        }
        return s;
    }
}
