package org.informatics.bank_system.service.impl;

import lombok.RequiredArgsConstructor;
import org.informatics.bank_system.data.entity.AccountStatus;
import org.informatics.bank_system.data.entity.BankAccount;
import org.informatics.bank_system.data.entity.Client;
import org.informatics.bank_system.data.entity.User;
import org.informatics.bank_system.data.repository.BankAccountRepository;
import org.informatics.bank_system.dto.BankAccountDto;
import org.informatics.bank_system.dto.CreateBankAccountDto;
import org.informatics.bank_system.exception.BusinessRuleException;
import org.informatics.bank_system.exception.ObjectNotFoundException;
import org.informatics.bank_system.service.BankAccountService;
import org.informatics.bank_system.service.ClientService;
import org.informatics.bank_system.service.UserService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class BankAccountServiceImpl implements BankAccountService {

    private final BankAccountRepository bankAccountRepository;
    private final ClientService clientService;
    private final UserService userService;

    @Override
    @Transactional(readOnly = true)
    public List<BankAccountDto> getAccounts() {
        User currentUser = userService.getCurrentUser();
        List<BankAccount> accounts = currentUser.hasAuthority(UserServiceImpl.ROLE_ADMIN)
                ? bankAccountRepository.findAll()
                : bankAccountRepository.findByClientId(currentUser.getClient().getId());

        return accounts.stream()
                .map(this::mapAccount)
                .toList();
    }

    @Override
    @Transactional
    public BankAccountDto createAccount(CreateBankAccountDto dto) {
        String iban = dto.getIban().trim().toUpperCase();
        if (bankAccountRepository.existsByIban(iban)) {
            throw new BusinessRuleException("Bank account with this IBAN already exists.");
        }

        Client client = clientService.getClientEntity(dto.getClientId());

        BankAccount account = new BankAccount();
        account.setClient(client);
        account.setIban(iban);
        account.setBalance(dto.getInitialBalance());
        account.setStatus(AccountStatus.ACTIVE);

        return mapAccount(bankAccountRepository.save(account));
    }

    @Override
    @Transactional
    public BankAccountDto closeAccount(Long id) {
        BankAccount account = bankAccountRepository.findById(id)
                .orElseThrow(() -> new ObjectNotFoundException("Bank account with id " + id + " was not found."));

        if (account.getStatus() == AccountStatus.CLOSED) {
            throw new BusinessRuleException("Bank account is already closed.");
        }

        if (account.getBalance().compareTo(BigDecimal.ZERO) != 0) {
            throw new BusinessRuleException("Bank account with a non-zero balance cannot be closed.");
        }

        account.setStatus(AccountStatus.CLOSED);
        account.setClosedAt(LocalDateTime.now());
        return mapAccount(bankAccountRepository.save(account));
    }

    private BankAccountDto mapAccount(BankAccount account) {
        return BankAccountDto.builder()
                .id(account.getId())
                .iban(account.getIban())
                .balance(account.getBalance())
                .status(account.getStatus().name())
                .clientId(account.getClient().getId())
                .clientDisplayName(account.getClient().getDisplayName())
                .build();
    }
}
