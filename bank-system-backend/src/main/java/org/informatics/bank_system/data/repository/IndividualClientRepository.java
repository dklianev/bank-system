package org.informatics.bank_system.data.repository;

import org.informatics.bank_system.data.entity.IndividualClient;
import org.springframework.data.jpa.repository.JpaRepository;

public interface IndividualClientRepository extends JpaRepository<IndividualClient, Long> {

    boolean existsByEgn(String egn);
}
