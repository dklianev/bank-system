package org.informatics.bank_system.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class CreateIndividualClientDto {

    @NotBlank
    @Size(min = 2, max = 60)
    private String firstName;

    @NotBlank
    @Size(min = 2, max = 60)
    private String lastName;

    @NotBlank
    @Pattern(regexp = "\\d{10}", message = "EGN must contain exactly 10 digits")
    private String egn;
}
