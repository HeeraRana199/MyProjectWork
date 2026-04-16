package com.cts.controller;

import com.cts.entity.Certification;
import com.cts.service.CertificationService;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@AllArgsConstructor
@RequestMapping("/certification")
public class CertificationController {

    private CertificationService certificationService;

    //Create Certification
    @PostMapping("/{candidateId}")
    public ResponseEntity<?> registerCertification(@RequestBody Certification certification, @PathVariable Integer candidateId){
        certification = certificationService.registerCertification(certification,candidateId);
        return new ResponseEntity<>(certification, HttpStatus.CREATED);
    }

    //Fetch Certification
    @GetMapping("/{certificationId}")
    public ResponseEntity<?> getCertification(@PathVariable String certificationId){
        Certification certification = certificationService.getCertification(certificationId);
        return ResponseEntity.ok(certification);
    }

    //Update Certification
    @PatchMapping("/{certificationId}")
    public ResponseEntity<?> updateCertification(@RequestBody Certification certification, @PathVariable String certificationId){
        Certification updatedCertification = certificationService.updateCertification(certification,certificationId);
        //return new ResponseEntity<>(updatedCertification, HttpStatus.OK);
        return ResponseEntity.ok(updatedCertification);
    }

    //Delete Certification
    @DeleteMapping("/{certificationId}")
    public ResponseEntity<?> deleteCertification(@PathVariable String certificationId){
        //this method has void return type so no need to store its result
        certificationService.deleteCertification(certificationId);
        return ResponseEntity.ok("Certificate deleted successfully!!");
    }
}
