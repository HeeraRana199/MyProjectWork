package com.cts.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ProjectDto {
    private Integer projectId;

    private String name;
    private String description;
    private String techStack;
    private String outcome;
    private String role;

}
