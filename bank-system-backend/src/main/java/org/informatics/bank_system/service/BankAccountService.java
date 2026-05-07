package org.informatics.bank_system.service;

import org.informatics.bank_system.dto.BankAccountDto;
import org.informatics.bank_system.dto.CreateBankAccountDto;

import java.util.List;

public interface BankAccountService {

    List<BankAccountDto> getAccounts();

    BankAccountDto createAccount(CreateBankAccountDto dto);

    BankAccountDto closeAccount(Long id);
}
