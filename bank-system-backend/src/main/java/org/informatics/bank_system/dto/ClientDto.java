package org.informatics.bank_system.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ClientDto {

    private Long id;
    private String clientType;
    private String displayName;
    private String identifier;
    private String representativeName;
    private int accountsCount;
    private int creditsCount;
}
