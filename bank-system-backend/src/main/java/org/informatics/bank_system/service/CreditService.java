package org.informatics.bank_system.service;

import org.informatics.bank_system.dto.CreateCreditDto;
import org.informatics.bank_system.dto.CreditDto;
import org.informatics.bank_system.dto.CreditProductDto;
import org.informatics.bank_system.dto.RepaymentInstallmentDto;

import java.util.List;

public interface CreditService {

    List<CreditProductDto> getCreditProducts();

    List<CreditDto> getCredits();

    CreditDto createCredit(CreateCreditDto dto);

    List<RepaymentInstallmentDto> getInstallments(Long creditId);

    RepaymentInstallmentDto payInstallment(Long installmentId);

    CreditDto getCreditStatus(Long creditId);
}
