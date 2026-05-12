package com.cts.controller;


import com.cts.dto.CandidateDto;
import com.cts.dto.CandidateScoreDto;
import com.cts.dto.SkillsDto;
import com.cts.service.CandidateService;
import com.cts.service.LeaderService;
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
public class LeaderController {
    private static final Logger logger = LoggerFactory.getLogger(LeaderController.class);
    private final LeaderService leaderService;
    private final CandidateService candidateService;

    @GetMapping("/candidate")
    public ResponseEntity<?> getCandidateById(@RequestParam int id) {
        logger.info("Leader request to fetch candidate with ID: {}", id);
        try {
            var candidateDto = candidateService.getCandidateById(id);
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
    public ResponseEntity<?> filterCandidates(
            @RequestParam(required = false) List<String> programmingSkills,
            @RequestParam(required = false) List<String> toolSkills,
            @RequestParam(required = false) List<String> frameworkSkills,
            @RequestParam(required = false) String certificate,
            @RequestParam(required = false) String cohortCode,
            @RequestParam(required = false) String deploymentLocation,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(required = false) Integer pageSize) {

        logger.info("Filter candidates - prog: {}, tools: {}, fw: {}, cert: {}, cohort: {}, loc: {}, page: {}",
                programmingSkills, toolSkills, frameworkSkills, certificate, cohortCode, deploymentLocation, page);
        try {
            var result = leaderService.getFilteredCandidates(
                    programmingSkills == null ? Collections.emptyList() : programmingSkills,
                    toolSkills == null ? Collections.emptyList() : toolSkills,
                    frameworkSkills == null ? Collections.emptyList() : frameworkSkills,
                    certificate,
                    cohortCode,
                    deploymentLocation,
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
    public void exportCandidates(
            @RequestParam(required = false) List<String> programmingSkills,
            @RequestParam(required = false) List<String> toolSkills,
            @RequestParam(required = false) List<String> frameworkSkills,
            @RequestParam(required = false) String certificate,
            @RequestParam(required = false) String cohortCode,
            @RequestParam(required = false) String deploymentLocation,
            HttpServletResponse response) throws IOException {

        logger.info("Export candidates - prog: {}, tools: {}, fw: {}, cert: {}, cohort: {}, loc: {}",
                programmingSkills, toolSkills, frameworkSkills, certificate, cohortCode, deploymentLocation);

        String timestamp = java.time.LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss"));
        String filename = "candidates-" + timestamp + ".csv";

        response.setContentType("text/csv; charset=UTF-8");
        response.setHeader("Content-Disposition", "attachment; filename=\"" + filename + "\"");
        response.setCharacterEncoding("UTF-8");

        List<CandidateDto> rows = leaderService.getAllFilteredCandidates(
                programmingSkills, toolSkills, frameworkSkills,
                certificate, cohortCode, deploymentLocation);

        try (PrintWriter writer = response.getWriter()) {
            // BOM so Excel detects UTF-8 correctly
            writer.write('﻿');

            writer.println(String.join(",",
                    "Candidate ID", "Associate ID", "Name", "Email", "Gender",
                    "Track", "Cohort Code", "Deployment Location", "Date of Joining",
                    "Programming Skills", "Tool Skills", "Framework Skills",
                    "Overall Score", "Attendance Score", "Language Score",
                    "Interim RAG", "Final RAG",
                    "Certifications", "Projects", "Achievements"
            ));

            for (CandidateDto c : rows) {
                SkillsDto s = c.getSkills();
                CandidateScoreDto sc = c.getCandidateScore();

                writer.println(String.join(",",
                        csv(c.getCognizantCandidateId()),
                        csv(c.getAssociateId()),
                        csv(c.getCandidateName()),
                        csv(c.getCognizantEmailID()),
                        csv(c.getGender()),
                        csv(c.getTrackName()),
                        csv(c.getCohortCode()),
                        csv(c.getDeploymentLocation()),
                        csv(c.getDoj()),
                        csv(s == null ? "" : s.getProgrammings()),
                        csv(s == null ? "" : s.getTools()),
                        csv(s == null ? "" : s.getFrameworks()),
                        csv(sc == null ? "" : sc.getPerformanceScore()),
                        csv(sc == null ? "" : sc.getAttendanceScore()),
                        csv(sc == null ? "" : sc.getLanguageScore()),
                        csv(sc == null ? "" : sc.getInterimScore()),
                        csv(sc == null ? "" : sc.getFinalScore()),
                        csv(c.getCertificates() == null ? 0 : c.getCertificates().size()),
                        csv(c.getProjects() == null ? 0 : c.getProjects().size()),
                        csv(c.getAchievement() == null ? 0 : c.getAchievement().size())
                ));
            }
            writer.flush();
        }
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
