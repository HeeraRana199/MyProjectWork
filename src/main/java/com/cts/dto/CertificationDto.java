package com.cts.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CertificationDto {
    private String certificationId;
    private String certificationName;
    private String certificationProvider;
    private Boolean status;
}
