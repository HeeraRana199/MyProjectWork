package com.cts.controller;

import com.cts.entity.Certification;
import com.cts.service.CertificationService;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RestController
@AllArgsConstructor
@RequestMapping("/certification")
public class CertificationController {

    private static final Logger logger = LoggerFactory.getLogger(CertificationController.class);
    private CertificationService certificationService;

    //Create Certification
    @PostMapping("/{candidateId}")
    public ResponseEntity<?> registerCertification(@RequestBody Certification certification, @PathVariable Integer candidateId){
        logger.info("Received request to register certification for candidateId: {}", candidateId);
        try {
            certification = certificationService.registerCertification(certification, candidateId);
            logger.info("Successfully registered certification for candidateId: {}", candidateId);
            return new ResponseEntity<>(certification, HttpStatus.CREATED);
        } catch (Exception e) {
            logger.error("Error occurred while registering certification for candidateId: {}", candidateId, e);
            return ResponseEntity.internalServerError().body("Error registering certification: " + e.getMessage());
        }
    }

    //Fetch Certification
    @GetMapping("/{certificationId}")
    public ResponseEntity<?> getCertification(@PathVariable String certificationId){
        logger.info("Received request to fetch certification with ID: {}", certificationId);
        try {
            Certification certification = certificationService.getCertification(certificationId);
            logger.debug("Successfully fetched certification with ID: {}", certificationId);
            return ResponseEntity.ok(certification);
        } catch (Exception e) {
            logger.error("Error occurred while fetching certification with ID: {}", certificationId, e);
            return ResponseEntity.internalServerError().body("Error fetching certification: " + e.getMessage());
        }
    }

    //Update Certification
    @PatchMapping("/{certificationId}")
    public ResponseEntity<?> updateCertification(@RequestBody Certification certification, @PathVariable String certificationId){
        logger.info("Received request to update certification with ID: {}", certificationId);
        try {
            Certification updatedCertification = certificationService.updateCertification(certification, certificationId);
            logger.info("Successfully updated certification with ID: {}", certificationId);
            return ResponseEntity.ok(updatedCertification);
        } catch (Exception e) {
            logger.error("Error occurred while updating certification with ID: {}", certificationId, e);
            return ResponseEntity.internalServerError().body("Error updating certification: " + e.getMessage());
        }
    }

    //Delete Certification
    @DeleteMapping("/{certificationId}")
    public ResponseEntity<?> deleteCertification(@PathVariable String certificationId){
        logger.info("Received request to delete certification with ID: {}", certificationId);
        try {
            certificationService.deleteCertification(certificationId);
            logger.info("Successfully deleted certification with ID: {}", certificationId);
            return ResponseEntity.ok("Certificate deleted successfully!!");
        } catch (Exception e) {
            logger.error("Error occurred while deleting certification with ID: {}", certificationId, e);
            return ResponseEntity.internalServerError().body("Error deleting certification: " + e.getMessage());
        }
    }
}
