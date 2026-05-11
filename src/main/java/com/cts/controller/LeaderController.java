package com.cts.controller;


import com.cts.service.CandidateService;
import com.cts.service.LeaderService;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@AllArgsConstructor
//@RequiredArgsConstructor
@RequestMapping("/leader")
public class LeaderController {
    private static final Logger logger = LoggerFactory.getLogger(LeaderController.class);
    private final LeaderService leaderService;


    @GetMapping("/allcandidates")
    public ResponseEntity<?> getAllCandidates(
            @RequestParam String searchType,
            @RequestParam String searchText,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(required = false) Integer pageSize) {
        logger.info("Received request to fetch candidates - page: {}, pageSize: {}", page, pageSize);
        try {
            var paginatedCandidates = leaderService.getAllCandidatesPaginated(searchType, searchText, page, pageSize);
            logger.debug("Successfully fetched page {} with candidates", page);
            return new ResponseEntity<>(paginatedCandidates, HttpStatus.OK);
        } catch (Exception e) {
            logger.error("Error occurred while fetching paginated candidates", e);
            return ResponseEntity.internalServerError().body("Error fetching candidates: " + e.getMessage());
        }
    }

}
