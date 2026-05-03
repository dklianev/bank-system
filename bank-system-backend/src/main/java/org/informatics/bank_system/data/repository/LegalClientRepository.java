package org.informatics.bank_system.data.repository;

import org.informatics.bank_system.data.entity.LegalClient;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LegalClientRepository extends JpaRepository<LegalClient, Long> {

    boolean existsByEik(String eik);
}
