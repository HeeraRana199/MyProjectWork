package com.cts.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ProjectDto {
    private Integer projectId;

    private String projectName;
    private String tech;
    private String outcome;
    private String role;
    private String description;

}
