package org.informatics.bank_system.service.impl;

import lombok.RequiredArgsConstructor;
import org.informatics.bank_system.data.entity.Client;
import org.informatics.bank_system.data.entity.IndividualClient;
import org.informatics.bank_system.data.entity.LegalClient;
import org.informatics.bank_system.data.repository.ClientRepository;
import org.informatics.bank_system.data.repository.IndividualClientRepository;
import org.informatics.bank_system.data.repository.LegalClientRepository;
import org.informatics.bank_system.dto.ClientDto;
import org.informatics.bank_system.dto.CreateIndividualClientDto;
import org.informatics.bank_system.dto.CreateLegalClientDto;
import org.informatics.bank_system.exception.BusinessRuleException;
import org.informatics.bank_system.exception.ObjectNotFoundException;
import org.informatics.bank_system.service.ClientService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ClientServiceImpl implements ClientService {

    private final ClientRepository clientRepository;
    private final IndividualClientRepository individualClientRepository;
    private final LegalClientRepository legalClientRepository;

    @Override
    @Transactional(readOnly = true)
    public List<ClientDto> getClients() {
        return clientRepository.findAll()
                .stream()
                .map(this::mapClient)
                .toList();
    }

    @Override
    @Transactional
    public ClientDto createIndividualClient(CreateIndividualClientDto dto) {
        if (individualClientRepository.existsByEgn(dto.getEgn())) {
            throw new BusinessRuleException("Client with this EGN already exists.");
        }

        IndividualClient client = new IndividualClient();
        client.setFirstName(dto.getFirstName().trim());
        client.setLastName(dto.getLastName().trim());
        client.setEgn(dto.getEgn().trim());

        return mapClient(individualClientRepository.save(client));
    }

    @Override
    @Transactional
    public ClientDto createLegalClient(CreateLegalClientDto dto) {
        if (legalClientRepository.existsByEik(dto.getEik())) {
            throw new BusinessRuleException("Client with this EIK already exists.");
        }

        LegalClient client = new LegalClient();
        client.setCompanyName(dto.getCompanyName().trim());
        client.setEik(dto.getEik().trim());
        client.setRepresentativeFirstName(dto.getRepresentativeFirstName().trim());
        client.setRepresentativeLastName(dto.getRepresentativeLastName().trim());

        return mapClient(legalClientRepository.save(client));
    }

    @Override
    @Transactional(readOnly = true)
    public Client getClientEntity(Long id) {
        return clientRepository.findById(id)
                .orElseThrow(() -> new ObjectNotFoundException("Client with id " + id + " was not found."));
    }

    private ClientDto mapClient(Client client) {
        String type = "UNKNOWN";
        String representativeName = null;

        if (client instanceof IndividualClient) {
            type = "INDIVIDUAL";
        }

        if (client instanceof LegalClient legalClient) {
            type = "LEGAL";
            representativeName = legalClient.getRepresentativeFirstName() + " " + legalClient.getRepresentativeLastName();
        }

        return ClientDto.builder()
                .id(client.getId())
                .clientType(type)
                .displayName(client.getDisplayName())
                .identifier(client.getIdentifier())
                .representativeName(representativeName)
                .accountsCount(client.getAccounts().size())
                .creditsCount(client.getCredits().size())
                .build();
    }
}
