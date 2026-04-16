package com.cts.service;

import com.cts.entity.Candidate;
import com.cts.entity.Certification;
import com.cts.exceptions.CandidateNotFoundException;
import com.cts.repository.CandidateRepository;
import com.cts.repository.CertificationRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;

@Service
@AllArgsConstructor
public class CertificationService {

    private CertificationRepository certificationRepository;
    private CandidateRepository candidateRepository;

    //Create certification logic
    public Certification registerCertification(Certification certification, Integer candidateId) {
        Candidate candidate = candidateRepository.findById(candidateId)
                .orElseThrow(() -> new CandidateNotFoundException("Candidate not found - to register Certification"));
        certification.setCandidate(candidate);//setting the value of Candidate in the Certification entity
        certification.setStatus(false);//setting the value of Status in the Certificate entity
//        certification = certificationRepository.save(certification);// saving the certificate entity in the database
        List<Certification> certificates = candidate.getCertificates();
        certificates.add(certification);
        candidate.setCertificates(certificates);
        candidate = candidateRepository.save(candidate);
        return certification;//It will return the entire JSON body with the all fields of certification entity
    }

    //Get certification logic
    public Certification getCertification(String certificationId){
        Certification certificate = certificationRepository.findById(certificationId)
                .orElseThrow(()-> new RuntimeException("Certificate not found with this candidate ID"));

        return certificate;
    }

    //Update certification logic
    public Certification updateCertification(Certification certification, String certificationId){
        //first check inside the DB whether the candidate exit or not inside the candidate repository for which we are adding the certification
        Certification certificate = certificationRepository.findById(certificationId)
                .orElseThrow(() -> new RuntimeException("Certificate not found - to update Certification"));

        //update only the fields which already exists
        if(certificate.getCertificationName() != null){
            certificate.setCertificationName(certification.getCertificationName());
        }

        if(certificate.getCertificationProvider() != null){
            certificate.setCertificationProvider(certification.getCertificationProvider());
        }

        if(certificate.getStatus() != null){
            certificate.setStatus(certification.getStatus());
        }

        //now save and return the updated certificate
        return certificationRepository.save(certificate);
    }

    //Delete certification logic
    public void deleteCertification(String certificationId){
        Certification certificate = certificationRepository.findById(certificationId)
                .orElseThrow(()-> new RuntimeException("Certificate not found!"));

        certificationRepository.delete(certificate);//it will return nothing
    }
}
