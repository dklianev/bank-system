package org.informatics.bank_system.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BankAccountDto {

    private Long id;
    private String iban;
    private BigDecimal balance;
    private String status;
    private Long clientId;
    private String clientDisplayName;
}
