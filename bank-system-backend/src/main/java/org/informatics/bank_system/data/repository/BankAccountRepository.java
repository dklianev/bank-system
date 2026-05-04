package org.informatics.bank_system.data.repository;

import org.informatics.bank_system.data.entity.BankAccount;
import org.informatics.bank_system.data.entity.AccountStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BankAccountRepository extends JpaRepository<BankAccount, Long> {

    boolean existsByIban(String iban);

    List<BankAccount> findByClientId(Long clientId);

    long countByStatus(AccountStatus status);
}
