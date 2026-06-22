package org.informatics.bank_system.config;

import lombok.RequiredArgsConstructor;
import org.informatics.bank_system.data.entity.CreditProduct;
import org.informatics.bank_system.data.entity.CreditProductCode;
import org.informatics.bank_system.data.entity.IndividualClient;
import org.informatics.bank_system.data.entity.LegalClient;
import org.informatics.bank_system.data.entity.Role;
import org.informatics.bank_system.data.entity.User;
import org.informatics.bank_system.data.repository.BankAccountRepository;
import org.informatics.bank_system.data.repository.CreditProductRepository;
import org.informatics.bank_system.data.repository.CreditRepository;
import org.informatics.bank_system.data.repository.IndividualClientRepository;
import org.informatics.bank_system.data.repository.LegalClientRepository;
import org.informatics.bank_system.data.repository.RoleRepository;
import org.informatics.bank_system.data.repository.UserRepository;
import org.informatics.bank_system.dto.CreateBankAccountDto;
import org.informatics.bank_system.dto.CreateCreditDto;
import org.informatics.bank_system.dto.CreateIndividualClientDto;
import org.informatics.bank_system.dto.CreateLegalClientDto;
import org.informatics.bank_system.dto.CreateUserDto;
import org.informatics.bank_system.service.BankAccountService;
import org.informatics.bank_system.service.ClientService;
import org.informatics.bank_system.service.CreditService;
import org.informatics.bank_system.service.UserService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final CreditProductRepository creditProductRepository;
    private final RoleRepository roleRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final IndividualClientRepository individualClientRepository;
    private final LegalClientRepository legalClientRepository;
    private final BankAccountRepository bankAccountRepository;
    private final CreditRepository creditRepository;
    private final ClientService clientService;
    private final BankAccountService bankAccountService;
    private final UserService userService;
    private final CreditService creditService;

    @Override
    @Transactional
    public void run(String... args) {
        Role adminRole = createRoleIfMissing("ADMIN");
        createRoleIfMissing("CLIENT");
        createAdminUserIfMissing("admin", "admin", adminRole);

        CreditProduct consumerProduct = createCreditProductIfMissing(
                CreditProductCode.CONSUMER,
                "Consumer credit",
                new BigDecimal("7.50"),
                new BigDecimal("50000.00"),
                120
        );

        CreditProduct mortgageProduct = createCreditProductIfMissing(
                CreditProductCode.MORTGAGE,
                "Mortgage credit",
                new BigDecimal("4.20"),
                new BigDecimal("500000.00"),
                360
        );

        seedDemoWorkflowData(consumerProduct, mortgageProduct);
    }

    private Role createRoleIfMissing(String authority) {
        return roleRepository.findByAuthority(authority)
                .orElseGet(() -> {
                    Role role = new Role();
                    role.setAuthority(authority);
                    return roleRepository.save(role);
                });
    }

    private void createAdminUserIfMissing(String username, String rawPassword, Role role) {
        if (userRepository.existsByUsername(username)) {
            return;
        }

        User user = new User();
        user.setUsername(username);
        user.setPassword(passwordEncoder.encode(rawPassword));
        user.setAccountNonExpired(true);
        user.setAccountNonLocked(true);
        user.setCredentialsNonExpired(true);
        user.setEnabled(true);
        user = userRepository.save(user);

        // Role owns the many-to-many relation, so the link is saved from the role side.
        role.getUsers().add(user);
        roleRepository.save(role);
    }

    private CreditProduct createCreditProductIfMissing(
            CreditProductCode code,
            String name,
            BigDecimal annualInterestRate,
            BigDecimal maxAmount,
            Integer maxTermMonths
    ) {
        return creditProductRepository.findByCode(code)
                .orElseGet(() -> {
                    CreditProduct product = new CreditProduct();
                    product.setCode(code);
                    product.setName(name);
                    product.setAnnualInterestRate(annualInterestRate);
                    product.setMaxAmount(maxAmount);
                    product.setMaxTermMonths(maxTermMonths);
                    return creditProductRepository.save(product);
                });
    }

    private void seedDemoWorkflowData(CreditProduct consumerProduct, CreditProduct mortgageProduct) {
        Long individualClientId = getOrCreateIndividualClient();
        Long legalClientId = getOrCreateLegalClient();

        createAccountIfMissing(individualClientId, "BG10DEMO000001IVAN0001", new BigDecimal("2500.00"));
        createAccountIfMissing(individualClientId, "BG11DEMO000002IVAN0002", new BigDecimal("0.00"));
        createAccountIfMissing(legalClientId, "BG12DEMO000003RILA0001", new BigDecimal("125000.00"));

        createClientUserIfMissing(individualClientId, "ivan.client", "client123");
        createClientUserIfMissing(legalClientId, "rila.client", "client123");

        createCreditIfMissing(individualClientId, consumerProduct, new BigDecimal("12000.00"), 24);
        createCreditIfMissing(legalClientId, mortgageProduct, new BigDecimal("180000.00"), 180);
    }

    private Long getOrCreateIndividualClient() {
        return individualClientRepository.findByEgn("8505054321")
                .map(IndividualClient::getId)
                .orElseGet(() -> {
                    CreateIndividualClientDto dto = new CreateIndividualClientDto();
                    dto.setFirstName("Ivan");
                    dto.setLastName("Petrov");
                    dto.setEgn("8505054321");
                    return clientService.createIndividualClient(dto).getId();
                });
    }

    private Long getOrCreateLegalClient() {
        return legalClientRepository.findByEik("204567890")
                .map(LegalClient::getId)
                .orElseGet(() -> {
                    CreateLegalClientDto dto = new CreateLegalClientDto();
                    dto.setCompanyName("Rila Soft OOD");
                    dto.setEik("204567890");
                    dto.setRepresentativeFirstName("Maria");
                    dto.setRepresentativeLastName("Georgieva");
                    return clientService.createLegalClient(dto).getId();
                });
    }

    private void createAccountIfMissing(Long clientId, String iban, BigDecimal initialBalance) {
        if (bankAccountRepository.existsByIban(iban)) {
            return;
        }

        CreateBankAccountDto dto = new CreateBankAccountDto();
        dto.setClientId(clientId);
        dto.setIban(iban);
        dto.setInitialBalance(initialBalance);
        bankAccountService.createAccount(dto);
    }

    private void createClientUserIfMissing(Long clientId, String username, String rawPassword) {
        if (userRepository.existsByUsername(username) || userRepository.existsByClientId(clientId)) {
            return;
        }

        CreateUserDto dto = new CreateUserDto();
        dto.setClientId(clientId);
        dto.setUsername(username);
        dto.setPassword(rawPassword);
        userService.createClientUser(dto);
    }

    private void createCreditIfMissing(
            Long clientId,
            CreditProduct product,
            BigDecimal principalAmount,
            Integer termMonths
    ) {
        boolean alreadySeeded = creditRepository.findByClientId(clientId)
                .stream()
                .anyMatch(credit ->
                        credit.getCreditProduct().getCode() == product.getCode()
                                && credit.getPrincipalAmount().compareTo(principalAmount) == 0
                                && credit.getTermMonths().equals(termMonths)
                );

        if (alreadySeeded) {
            return;
        }

        CreateCreditDto dto = new CreateCreditDto();
        dto.setClientId(clientId);
        dto.setCreditProductId(product.getId());
        dto.setPrincipalAmount(principalAmount);
        dto.setTermMonths(termMonths);
        creditService.createCredit(dto);
    }
}
