package org.informatics.bank_system.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.informatics.bank_system.dto.ClientDto;
import org.informatics.bank_system.dto.CreateIndividualClientDto;
import org.informatics.bank_system.dto.CreateLegalClientDto;
import org.informatics.bank_system.service.ClientService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/clients")
public class ClientController {

    private final ClientService clientService;

    @GetMapping
    public List<ClientDto> getClients() {
        return clientService.getClients();
    }

    @PostMapping("/individual")
    public ClientDto createIndividualClient(@Valid @RequestBody CreateIndividualClientDto dto) {
        return clientService.createIndividualClient(dto);
    }

    @PostMapping("/legal")
    public ClientDto createLegalClient(@Valid @RequestBody CreateLegalClientDto dto) {
        return clientService.createLegalClient(dto);
    }
}
