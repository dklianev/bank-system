package org.informatics.bank_system.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class CreateCreditDto {

    @NotNull
    private Long clientId;

    @NotNull
    private Long creditProductId;

    @NotNull
    @DecimalMin(value = "0.01")
    private BigDecimal principalAmount;

    @NotNull
    @Min(1)
    private Integer termMonths;

    private LocalDate startDate;
}
