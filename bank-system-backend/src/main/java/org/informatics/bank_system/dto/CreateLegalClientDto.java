package org.informatics.bank_system.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class CreateLegalClientDto {

    @NotBlank
    @Size(min = 2, max = 120)
    private String companyName;

    @NotBlank
    @Pattern(regexp = "\\d{9}|\\d{13}", message = "EIK must contain 9 or 13 digits")
    private String eik;

    @NotBlank
    @Size(min = 2, max = 60)
    private String representativeFirstName;

    @NotBlank
    @Size(min = 2, max = 60)
    private String representativeLastName;
}
