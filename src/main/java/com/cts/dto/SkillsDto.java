package com.cts.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SkillsDto {
    private Integer skillId;

    private String programmings;
    private String tools;
    private String frameworks;
}
