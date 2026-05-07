package org.informatics.bank_system.service;

import org.informatics.bank_system.data.entity.Client;
import org.informatics.bank_system.dto.ClientDto;
import org.informatics.bank_system.dto.CreateIndividualClientDto;
import org.informatics.bank_system.dto.CreateLegalClientDto;

import java.util.List;

public interface ClientService {

    List<ClientDto> getClients();

    ClientDto createIndividualClient(CreateIndividualClientDto dto);

    ClientDto createLegalClient(CreateLegalClientDto dto);

    Client getClientEntity(Long id);
}
