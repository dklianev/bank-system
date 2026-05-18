package org.informatics.bank_system.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class CreateUserDto {

    @NotNull
    private Long clientId;

    @NotBlank
    @Size(min = 3, max = 40)
    private String username;

    @NotBlank
    @Size(min = 6, max = 72)
    private String password;
}
