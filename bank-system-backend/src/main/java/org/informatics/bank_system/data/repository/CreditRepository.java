package org.informatics.bank_system.data.repository;

import org.informatics.bank_system.data.entity.Credit;
import org.informatics.bank_system.data.entity.CreditStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.math.BigDecimal;

import java.util.List;

public interface CreditRepository extends JpaRepository<Credit, Long> {

    List<Credit> findByClientId(Long clientId);

    long countByStatus(CreditStatus status);

    @Query("SELECT COALESCE(SUM(c.principalAmount), 0) FROM Credit c")
    BigDecimal sumPrincipalAmount();
}
