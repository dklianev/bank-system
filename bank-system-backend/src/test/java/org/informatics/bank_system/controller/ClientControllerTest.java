package org.informatics.bank_system.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.informatics.bank_system.config.SecurityConfig;
import org.informatics.bank_system.dto.ClientDto;
import org.informatics.bank_system.dto.CreateIndividualClientDto;
import org.informatics.bank_system.dto.CreateLegalClientDto;
import org.informatics.bank_system.exception.BusinessRuleException;
import org.informatics.bank_system.service.ClientService;
import org.informatics.bank_system.service.UserService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ClientController.class)
@Import(SecurityConfig.class)
@WithMockUser(authorities = "ADMIN")
class ClientControllerTest {

    @MockitoBean
    private ClientService clientService;

    @MockitoBean
    private UserService userService;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void createIndividualClientReturnsCreatedClient() throws Exception {
        ClientDto created = ClientDto.builder()
                .id(1L)
                .clientType("INDIVIDUAL")
                .displayName("Dimitar Klianev")
                .identifier("9901011234")
                .build();

        Mockito.when(clientService.createIndividualClient(any())).thenReturn(created);

        mockMvc.perform(MockMvcRequestBuilders
                        .post("/api/clients/individual")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(individualRequest("9901011234"))))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.clientType", is("INDIVIDUAL")))
                .andExpect(jsonPath("$.identifier", is("9901011234")));
    }

    @Test
    void createIndividualClientWithInvalidEgnReturnsBadRequest() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders
                        .post("/api/clients/individual")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(individualRequest("123"))))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status", is(400)))
                .andExpect(jsonPath("$.errors[0]", is("egn: EGN must contain exactly 10 digits")));

        Mockito.verify(clientService, Mockito.never()).createIndividualClient(any());
    }

    @Test
    void createIndividualClientWithDuplicateEgnReturnsBadRequest() throws Exception {
        Mockito.when(clientService.createIndividualClient(any()))
                .thenThrow(new BusinessRuleException("Client with this EGN already exists."));

        mockMvc.perform(MockMvcRequestBuilders
                        .post("/api/clients/individual")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(individualRequest("9901011234"))))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors[0]", is("Client with this EGN already exists.")));
    }

    @Test
    void createLegalClientReturnsCreatedClient() throws Exception {
        ClientDto created = ClientDto.builder()
                .id(2L)
                .clientType("LEGAL")
                .displayName("Adrian Finance")
                .identifier("123456789")
                .representativeName("Adrian Vitig")
                .build();

        Mockito.when(clientService.createLegalClient(any())).thenReturn(created);

        mockMvc.perform(MockMvcRequestBuilders
                        .post("/api/clients/legal")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(legalRequest("123456789"))))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.clientType", is("LEGAL")))
                .andExpect(jsonPath("$.representativeName", is("Adrian Vitig")));
    }

    @Test
    void createLegalClientWithInvalidEikReturnsBadRequest() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders
                        .post("/api/clients/legal")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(legalRequest("12345"))))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors[0]", is("eik: EIK must contain 9 or 13 digits")));

        Mockito.verify(clientService, Mockito.never()).createLegalClient(any());
    }

    @Test
    void createLegalClientWithDuplicateEikReturnsBadRequest() throws Exception {
        Mockito.when(clientService.createLegalClient(any()))
                .thenThrow(new BusinessRuleException("Client with this EIK already exists."));

        mockMvc.perform(MockMvcRequestBuilders
                        .post("/api/clients/legal")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(legalRequest("123456789"))))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors[0]", is("Client with this EIK already exists.")));
    }

    @Test
    void getClientsReturnsAllClients() throws Exception {
        ClientDto individual = ClientDto.builder()
                .id(1L)
                .clientType("INDIVIDUAL")
                .displayName("Dimitar Klianev")
                .identifier("9901011234")
                .build();
        ClientDto legal = ClientDto.builder()
                .id(2L)
                .clientType("LEGAL")
                .displayName("Adrian Finance")
                .identifier("123456789")
                .representativeName("Adrian Vitig")
                .build();

        given(clientService.getClients()).willReturn(List.of(individual, legal));

        mockMvc.perform(MockMvcRequestBuilders.get("/api/clients"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id", is(individual.getId()), Long.class))
                .andExpect(jsonPath("$[0].clientType", is("INDIVIDUAL")))
                .andExpect(jsonPath("$[1].id", is(legal.getId()), Long.class))
                .andExpect(jsonPath("$[1].clientType", is("LEGAL")));
    }

    @Test
    @WithAnonymousUser
    void anonymousRequestIsRejectedWithUnauthorized() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/api/clients"))
                .andExpect(status().isUnauthorized());

        Mockito.verify(clientService, Mockito.never()).getClients();
    }

    @Test
    @WithMockUser(authorities = "CLIENT")
    void clientRoleCannotAccessClientCatalog() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/api/clients"))
                .andExpect(status().isForbidden());

        Mockito.verify(clientService, Mockito.never()).getClients();
    }

    private CreateIndividualClientDto individualRequest(String egn) {
        CreateIndividualClientDto request = new CreateIndividualClientDto();
        request.setFirstName("Dimitar");
        request.setLastName("Klianev");
        request.setEgn(egn);
        return request;
    }

    private CreateLegalClientDto legalRequest(String eik) {
        CreateLegalClientDto request = new CreateLegalClientDto();
        request.setCompanyName("Adrian Finance");
        request.setEik(eik);
        request.setRepresentativeFirstName("Adrian");
        request.setRepresentativeLastName("Vitig");
        return request;
    }
}
