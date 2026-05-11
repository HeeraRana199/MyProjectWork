package com.cts.controller;


import com.cts.service.LeaderService;
import lombok.AllArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collections;
import java.util.List;

@RestController
@AllArgsConstructor
@RequestMapping("/leader")
public class LeaderController {
    private static final Logger logger = LoggerFactory.getLogger(LeaderController.class);
    private final LeaderService leaderService;


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
}
