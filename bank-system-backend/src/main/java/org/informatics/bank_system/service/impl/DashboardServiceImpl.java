package org.informatics.bank_system.service.impl;

import lombok.RequiredArgsConstructor;
import org.informatics.bank_system.data.entity.AccountStatus;
import org.informatics.bank_system.data.entity.CreditStatus;
import org.informatics.bank_system.data.repository.BankAccountRepository;
import org.informatics.bank_system.data.repository.ClientRepository;
import org.informatics.bank_system.data.repository.CreditRepository;
import org.informatics.bank_system.dto.DashboardDto;
import org.informatics.bank_system.service.DashboardService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class DashboardServiceImpl implements DashboardService {

    private final ClientRepository clientRepository;
    private final BankAccountRepository bankAccountRepository;
    private final CreditRepository creditRepository;

    @Override
    @Transactional(readOnly = true)
    public DashboardDto getDashboard() {
        return DashboardDto.builder()
                .clientsCount(clientRepository.count())
                .activeAccountsCount(bankAccountRepository.countByStatus(AccountStatus.ACTIVE))
                .activeCreditsCount(creditRepository.countByStatus(CreditStatus.ACTIVE))
                .totalCreditPrincipal(creditRepository.sumPrincipalAmount())
                .build();
    }
}
