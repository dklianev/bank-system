package org.informatics.bank_system.data.repository;

import org.informatics.bank_system.data.entity.CreditProduct;
import org.informatics.bank_system.data.entity.CreditProductCode;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CreditProductRepository extends JpaRepository<CreditProduct, Long> {

    Optional<CreditProduct> findByCode(CreditProductCode code);
}
