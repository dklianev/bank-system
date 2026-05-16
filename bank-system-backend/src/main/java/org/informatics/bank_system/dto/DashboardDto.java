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
public class DashboardDto {

    private long clientsCount;
    private long activeAccountsCount;
    private long activeCreditsCount;
    private BigDecimal totalCreditPrincipal;
}
