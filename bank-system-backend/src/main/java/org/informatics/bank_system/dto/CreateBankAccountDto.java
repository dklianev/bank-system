package org.informatics.bank_system.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class CreateBankAccountDto {

    @NotNull
    private Long clientId;

    @NotBlank
    @Pattern(regexp = "BG\\d{2}[A-Z]{4}\\d{6}[A-Z0-9]{8}", message = "IBAN must be a valid Bulgarian IBAN")
    private String iban;

    @NotNull
    @DecimalMin(value = "0.00")
    private BigDecimal initialBalance;
}
