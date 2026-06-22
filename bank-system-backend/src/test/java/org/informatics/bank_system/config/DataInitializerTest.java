package org.informatics.bank_system.config;

import org.informatics.bank_system.data.entity.CreditProduct;
import org.informatics.bank_system.data.entity.CreditProductCode;
import org.informatics.bank_system.data.entity.Role;
import org.informatics.bank_system.data.repository.BankAccountRepository;
import org.informatics.bank_system.data.repository.CreditProductRepository;
import org.informatics.bank_system.data.repository.CreditRepository;
import org.informatics.bank_system.data.repository.IndividualClientRepository;
import org.informatics.bank_system.data.repository.LegalClientRepository;
import org.informatics.bank_system.data.repository.RoleRepository;
import org.informatics.bank_system.data.repository.UserRepository;
import org.informatics.bank_system.dto.ClientDto;
import org.informatics.bank_system.dto.CreateBankAccountDto;
import org.informatics.bank_system.dto.CreateCreditDto;
import org.informatics.bank_system.dto.CreateIndividualClientDto;
import org.informatics.bank_system.dto.CreateLegalClientDto;
import org.informatics.bank_system.dto.CreateUserDto;
import org.informatics.bank_system.service.BankAccountService;
import org.informatics.bank_system.service.ClientService;
import org.informatics.bank_system.service.CreditService;
import org.informatics.bank_system.service.UserService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DataInitializerTest {

    @Mock
    private CreditProductRepository creditProductRepository;

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private IndividualClientRepository individualClientRepository;

    @Mock
    private LegalClientRepository legalClientRepository;

    @Mock
    private BankAccountRepository bankAccountRepository;

    @Mock
    private CreditRepository creditRepository;

    @Mock
    private ClientService clientService;

    @Mock
    private BankAccountService bankAccountService;

    @Mock
    private UserService userService;

    @Mock
    private CreditService creditService;

    @Test
    void runSeedsDemoClientsAccountsUsersAndCreditsWhenTheyAreMissing() {
        when(roleRepository.findByAuthority("ADMIN")).thenReturn(Optional.of(role("ADMIN")));
        when(roleRepository.findByAuthority("CLIENT")).thenReturn(Optional.of(role("CLIENT")));
        when(userRepository.existsByUsername("admin")).thenReturn(true);

        when(creditProductRepository.findByCode(CreditProductCode.CONSUMER))
                .thenReturn(Optional.of(product(10L, CreditProductCode.CONSUMER)));
        when(creditProductRepository.findByCode(CreditProductCode.MORTGAGE))
                .thenReturn(Optional.of(product(11L, CreditProductCode.MORTGAGE)));

        when(individualClientRepository.findByEgn("8505054321")).thenReturn(Optional.empty());
        when(legalClientRepository.findByEik("204567890")).thenReturn(Optional.empty());
        when(clientService.createIndividualClient(any())).thenReturn(ClientDto.builder().id(1L).build());
        when(clientService.createLegalClient(any())).thenReturn(ClientDto.builder().id(2L).build());

        when(bankAccountRepository.existsByIban("BG10DEMO000001IVAN0001")).thenReturn(false);
        when(bankAccountRepository.existsByIban("BG11DEMO000002IVAN0002")).thenReturn(false);
        when(bankAccountRepository.existsByIban("BG12DEMO000003RILA0001")).thenReturn(false);

        when(userRepository.existsByUsername("ivan.client")).thenReturn(false);
        when(userRepository.existsByUsername("rila.client")).thenReturn(false);
        when(userRepository.existsByClientId(1L)).thenReturn(false);
        when(userRepository.existsByClientId(2L)).thenReturn(false);

        when(creditRepository.findByClientId(1L)).thenReturn(List.of());
        when(creditRepository.findByClientId(2L)).thenReturn(List.of());

        DataInitializer initializer = new DataInitializer(
                creditProductRepository,
                roleRepository,
                userRepository,
                passwordEncoder,
                individualClientRepository,
                legalClientRepository,
                bankAccountRepository,
                creditRepository,
                clientService,
                bankAccountService,
                userService,
                creditService
        );

        initializer.run();

        ArgumentCaptor<CreateIndividualClientDto> individualCaptor =
                ArgumentCaptor.forClass(CreateIndividualClientDto.class);
        verify(clientService).createIndividualClient(individualCaptor.capture());
        assertEquals("Ivan", individualCaptor.getValue().getFirstName());
        assertEquals("Petrov", individualCaptor.getValue().getLastName());
        assertEquals("8505054321", individualCaptor.getValue().getEgn());

        ArgumentCaptor<CreateLegalClientDto> legalCaptor =
                ArgumentCaptor.forClass(CreateLegalClientDto.class);
        verify(clientService).createLegalClient(legalCaptor.capture());
        assertEquals("Rila Soft OOD", legalCaptor.getValue().getCompanyName());
        assertEquals("204567890", legalCaptor.getValue().getEik());

        ArgumentCaptor<CreateBankAccountDto> accountCaptor =
                ArgumentCaptor.forClass(CreateBankAccountDto.class);
        verify(bankAccountService, times(3)).createAccount(accountCaptor.capture());
        List<CreateBankAccountDto> accounts = accountCaptor.getAllValues();
        assertEquals("BG10DEMO000001IVAN0001", accounts.get(0).getIban());
        assertEquals(new BigDecimal("2500.00"), accounts.get(0).getInitialBalance());
        assertEquals("BG11DEMO000002IVAN0002", accounts.get(1).getIban());
        assertEquals(new BigDecimal("0.00"), accounts.get(1).getInitialBalance());
        assertEquals("BG12DEMO000003RILA0001", accounts.get(2).getIban());
        assertEquals(new BigDecimal("125000.00"), accounts.get(2).getInitialBalance());

        ArgumentCaptor<CreateUserDto> userCaptor = ArgumentCaptor.forClass(CreateUserDto.class);
        verify(userService, times(2)).createClientUser(userCaptor.capture());
        assertEquals("ivan.client", userCaptor.getAllValues().get(0).getUsername());
        assertEquals("rila.client", userCaptor.getAllValues().get(1).getUsername());

        ArgumentCaptor<CreateCreditDto> creditCaptor = ArgumentCaptor.forClass(CreateCreditDto.class);
        verify(creditService, times(2)).createCredit(creditCaptor.capture());
        List<CreateCreditDto> credits = creditCaptor.getAllValues();
        assertEquals(1L, credits.get(0).getClientId());
        assertEquals(10L, credits.get(0).getCreditProductId());
        assertEquals(new BigDecimal("12000.00"), credits.get(0).getPrincipalAmount());
        assertEquals(24, credits.get(0).getTermMonths());
        assertNull(credits.get(0).getStartDate());
        assertEquals(2L, credits.get(1).getClientId());
        assertEquals(11L, credits.get(1).getCreditProductId());
        assertEquals(new BigDecimal("180000.00"), credits.get(1).getPrincipalAmount());
        assertEquals(180, credits.get(1).getTermMonths());
        assertNull(credits.get(1).getStartDate());
    }

    private Role role(String authority) {
        Role role = new Role();
        role.setAuthority(authority);
        return role;
    }

    private CreditProduct product(Long id, CreditProductCode code) {
        CreditProduct product = new CreditProduct();
        product.setId(id);
        product.setCode(code);
        product.setName(code.name());
        product.setAnnualInterestRate(new BigDecimal("5.00"));
        product.setMaxAmount(new BigDecimal("500000.00"));
        product.setMaxTermMonths(360);
        return product;
    }
}
