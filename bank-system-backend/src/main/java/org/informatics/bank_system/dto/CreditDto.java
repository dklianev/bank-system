package org.informatics.bank_system.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreditDto {

    private Long id;
    private Long clientId;
    private String clientDisplayName;
    private String creditProductCode;
    private String creditProductName;
    private BigDecimal principalAmount;
    private Integer termMonths;
    private BigDecimal annualInterestRate;
    private BigDecimal monthlyPayment;
    private LocalDate startDate;
    private String status;
    private int paidInstallments;
    private int totalInstallments;
}
