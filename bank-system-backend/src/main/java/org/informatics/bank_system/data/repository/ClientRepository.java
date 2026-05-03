package org.informatics.bank_system.data.repository;

import org.informatics.bank_system.data.entity.Client;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ClientRepository extends JpaRepository<Client, Long> {
}
