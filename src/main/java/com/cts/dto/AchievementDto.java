package com.cts.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AchievementDto {
    private Integer aId;

    private String type; // "ACHIEVEMENT" or "ACTIVITY"

    private String title;

    private String description;
}
