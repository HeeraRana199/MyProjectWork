package com.cts.service;

import com.cts.entity.Candidate;
import com.cts.entity.Certification;
import com.cts.exceptions.CandidateNotFoundException;
import com.cts.repository.CandidateRepository;
import com.cts.repository.CertificationRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

@Service
@AllArgsConstructor
public class CertificationService {

    private static final Logger logger = LoggerFactory.getLogger(CertificationService.class);
    private CertificationRepository certificationRepository;
    private CandidateRepository candidateRepository;

    //Create certification logic
    public Certification registerCertification(Certification certification, Integer associateId) {
        logger.info("Registering certification for associateId: {}, Certification: {}", associateId, certification.getCertificationName());
        try {
            Candidate candidate = candidateRepository.findById(associateId)
                    .orElseThrow(() -> new CandidateNotFoundException("Candidate not found - to register Certification"));
            certification.setCandidate(candidate);//setting the value of Candidate in the Certification entity
            certification.setStatus(false);//setting the value of Status in the Certificate entity
            List<Certification> certificates = candidate.getCertificates();
            certificates.add(certification);
            candidate.setCertificates(certificates);
            candidate = candidateRepository.save(candidate);
            logger.info("Certification registered successfully for associateId: {}", associateId);
            return certification;//It will return the entire JSON body with the all fields of certification entity
        } catch (Exception e) {
            logger.error("Error while registering certification for associateId: {}", associateId, e);
            throw e;
        }
    }

    //Get certification logic
    public Certification getCertification(String certificationId){
        logger.debug("Fetching certification with ID: {}", certificationId);
        try {
            Certification certificate = certificationRepository.findById(certificationId)
                    .orElseThrow(()-> new RuntimeException("Certificate not found with this candidate ID"));

            logger.debug("Successfully retrieved certification with ID: {}", certificationId);
            return certificate;
        } catch (Exception e) {
            logger.error("Error while fetching certification with ID: {}", certificationId, e);
            throw e;
        }
    }

    //Update certification logic
    public Certification updateCertification(Certification certification, String certificationId){
        logger.info("Updating certification with ID: {}", certificationId);
        try {
            //first check inside the DB whether the candidate exit or not inside the candidate repository for which we are adding the certification
            Certification certificate = certificationRepository.findById(certificationId)
                    .orElseThrow(() -> new RuntimeException("Certificate not found - to update Certification"));

            // Merge only the fields present in the incoming payload
            if(certification.getCertificationName() != null){
                logger.debug("Updating certification name for ID: {} to: {}", certificationId, certification.getCertificationName());
                certificate.setCertificationName(certification.getCertificationName());
            }

            if(certification.getCertificationProvider() != null){
                logger.debug("Updating certification provider for ID: {} to: {}", certificationId, certification.getCertificationProvider());
                certificate.setCertificationProvider(certification.getCertificationProvider());
            }

            if(certification.getStatus() != null){
                logger.debug("Updating certification status for ID: {} to: {}", certificationId, certification.getStatus());
                certificate.setStatus(certification.getStatus());
            }

            //now save and return the updated certificate
            Certification updatedCertification = certificationRepository.save(certificate);
            logger.info("Certification updated successfully with ID: {}", certificationId);
            return updatedCertification;
        } catch (Exception e) {
            logger.error("Error while updating certification with ID: {}", certificationId, e);
            throw e;
        }
    }

    //Delete certification logic
    public void deleteCertification(String certificationId){
        logger.info("Deleting certification with ID: {}", certificationId);
        try {
            Certification certificate = certificationRepository.findById(certificationId)
                    .orElseThrow(()-> new RuntimeException("Certificate not found!"));

            certificationRepository.delete(certificate);//it will return nothing
            logger.info("Certification deleted successfully with ID: {}", certificationId);
        } catch (Exception e) {
            logger.error("Error while deleting certification with ID: {}", certificationId, e);
            throw e;
        }
    }
}
