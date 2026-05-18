package org.informatics.bank_system.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SessionDto {

    private String username;
    private String role;
    private Long clientId;
    private String displayName;
}
