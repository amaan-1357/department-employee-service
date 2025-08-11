package com.codegic.departmentManagement.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SalaryAdjustmentRequest {
    @NotNull(message = "Department ID is required")
    private Long departmentId;

    @Min(value = 0, message = "Performance score must be between 0 and 100")
    @Max(value = 100, message = "Performance score must be between 0 and 100")
    private int performanceScore;
}
