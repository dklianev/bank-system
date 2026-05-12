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
public class CreditProductDto {

    private Long id;
    private String code;
    private String name;
    private BigDecimal annualInterestRate;
    private BigDecimal maxAmount;
    private Integer maxTermMonths;
}
