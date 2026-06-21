package org.informatics.bank_system.data.repository;

import org.informatics.bank_system.data.entity.InstallmentStatus;
import org.informatics.bank_system.data.entity.RepaymentInstallment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RepaymentInstallmentRepository extends JpaRepository<RepaymentInstallment, Long> {

    List<RepaymentInstallment> findByCreditIdOrderByInstallmentNumber(Long creditId);

    boolean existsByCreditIdAndInstallmentNumberLessThanAndStatusNot(
            Long creditId,
            int installmentNumber,
            InstallmentStatus status
    );
}
