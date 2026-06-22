package org.informatics.bank_system.data.repository;

import org.informatics.bank_system.data.entity.LegalClient;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface LegalClientRepository extends JpaRepository<LegalClient, Long> {

    boolean existsByEik(String eik);

    Optional<LegalClient> findByEik(String eik);
}
