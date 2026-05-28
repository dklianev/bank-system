package org.informatics.bank_system.integration;

import org.informatics.bank_system.dto.BankAccountDto;
import org.informatics.bank_system.dto.ClientDto;
import org.informatics.bank_system.dto.CreditDto;
import org.informatics.bank_system.dto.CreditProductDto;
import org.informatics.bank_system.dto.RepaymentInstallmentDto;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class BankWorkflowIntegrationTest {

    @Autowired
    private TestRestTemplate restTemplate;

    // The DataInitializer seeds an admin/admin account; every operator call runs as that user.
    private TestRestTemplate admin() {
        return restTemplate.withBasicAuth("admin", "admin");
    }

    private TestRestTemplate asClient(String username, String password) {
        return restTemplate.withBasicAuth(username, password);
    }

    @Test
    void completeBankWorkflowWorksThroughRestApi() {
        ClientDto client = admin().postForObject(
                "/api/clients/individual",
                Map.of(
                        "firstName", "Dimitar",
                        "lastName", "Klianev",
                        "egn", "9901011234"
                ),
                ClientDto.class
        );

        assertNotNull(client.getId());
        assertEquals("INDIVIDUAL", client.getClientType());

        BankAccountDto account = admin().postForObject(
                "/api/accounts",
                Map.of(
                        "clientId", client.getId(),
                        "iban", "BG12BANK123456ABCDEFGH",
                        "initialBalance", "2500.00"
                ),
                BankAccountDto.class
        );

        assertNotNull(account.getId());
        assertEquals("ACTIVE", account.getStatus());

        ResponseEntity<CreditProductDto[]> productsResponse = admin().getForEntity(
                "/api/credits/products",
                CreditProductDto[].class
        );
        CreditProductDto[] products = productsResponse.getBody();
        assertNotNull(products);
        assertTrue(products.length >= 2);

        CreditDto credit = admin().postForObject(
                "/api/credits",
                Map.of(
                        "clientId", client.getId(),
                        "creditProductId", products[0].getId(),
                        "principalAmount", "10000.00",
                        "termMonths", 12,
                        "startDate", "2026-06-01"
                ),
                CreditDto.class
        );

        assertNotNull(credit.getId());
        assertEquals(12, credit.getTotalInstallments());
        assertEquals("ACTIVE", credit.getStatus());

        ResponseEntity<RepaymentInstallmentDto[]> installmentsResponse = admin().getForEntity(
                "/api/credits/" + credit.getId() + "/installments",
                RepaymentInstallmentDto[].class
        );
        RepaymentInstallmentDto[] installments = installmentsResponse.getBody();
        assertNotNull(installments);
        assertEquals(12, installments.length);
        assertTrue(installments[0].getInterestPart().compareTo(installments[11].getInterestPart()) > 0);

        ResponseEntity<String> skippedPaymentResponse = admin().exchange(
                "/api/credits/installments/" + installments[1].getId() + "/pay",
                org.springframework.http.HttpMethod.PATCH,
                null,
                String.class
        );
        assertEquals(HttpStatus.BAD_REQUEST, skippedPaymentResponse.getStatusCode());

        admin().patchForObject(
                "/api/credits/installments/" + installments[0].getId() + "/pay",
                null,
                RepaymentInstallmentDto.class
        );

        CreditDto status = admin().getForObject(
                "/api/credits/" + credit.getId() + "/status",
                CreditDto.class
        );

        assertEquals(1, status.getPaidInstallments());
        assertEquals("ACTIVE", status.getStatus());
    }

    @Test
    void creditStatusBecomesPaidOffAfterAllInstallmentsArePaid() {
        ClientDto client = admin().postForObject(
                "/api/clients/legal",
                Map.of(
                        "companyName", "Adrian Finance",
                        "eik", "123456789",
                        "representativeFirstName", "Adrian",
                        "representativeLastName", "Vitig"
                ),
                ClientDto.class
        );
        assertNotNull(client);

        ResponseEntity<CreditProductDto[]> productsResponse = admin().getForEntity(
                "/api/credits/products",
                CreditProductDto[].class
        );
        CreditProductDto[] products = productsResponse.getBody();
        assertNotNull(products);
        assertTrue(products.length >= 1);

        CreditDto credit = admin().postForObject(
                "/api/credits",
                Map.of(
                        "clientId", client.getId(),
                        "creditProductId", products[0].getId(),
                        "principalAmount", "1200.00",
                        "termMonths", 2,
                        "startDate", "2026-06-01"
                ),
                CreditDto.class
        );

        ResponseEntity<RepaymentInstallmentDto[]> installmentsResponse = admin().getForEntity(
                "/api/credits/" + credit.getId() + "/installments",
                RepaymentInstallmentDto[].class
        );
        RepaymentInstallmentDto[] installments = installmentsResponse.getBody();
        assertNotNull(installments);
        assertEquals(2, installments.length);

        admin().patchForObject(
                "/api/credits/installments/" + installments[0].getId() + "/pay",
                null,
                RepaymentInstallmentDto.class
        );
        admin().patchForObject(
                "/api/credits/installments/" + installments[1].getId() + "/pay",
                null,
                RepaymentInstallmentDto.class
        );

        CreditDto status = admin().getForObject(
                "/api/credits/" + credit.getId() + "/status",
                CreditDto.class
        );

        assertEquals(2, status.getPaidInstallments());
        assertEquals("PAID_OFF", status.getStatus());
    }

    @Test
    void closingAccountRequiresZeroBalance() {
        ClientDto client = admin().postForObject(
                "/api/clients/individual",
                Map.of(
                        "firstName", "Elena",
                        "lastName", "Petrova",
                        "egn", "8807071234"
                ),
                ClientDto.class
        );

        BankAccountDto fundedAccount = admin().postForObject(
                "/api/accounts",
                Map.of(
                        "clientId", client.getId(),
                        "iban", "BG55BANK111111CLOSE001",
                        "initialBalance", "100.00"
                ),
                BankAccountDto.class
        );

        ResponseEntity<String> blockedClose = admin().exchange(
                "/api/accounts/" + fundedAccount.getId() + "/close",
                org.springframework.http.HttpMethod.PATCH,
                null,
                String.class
        );
        assertEquals(HttpStatus.BAD_REQUEST, blockedClose.getStatusCode());

        BankAccountDto emptyAccount = admin().postForObject(
                "/api/accounts",
                Map.of(
                        "clientId", client.getId(),
                        "iban", "BG66BANK222222CLOSE002",
                        "initialBalance", "0.00"
                ),
                BankAccountDto.class
        );

        BankAccountDto closedAccount = admin().patchForObject(
                "/api/accounts/" + emptyAccount.getId() + "/close",
                null,
                BankAccountDto.class
        );
        assertEquals("CLOSED", closedAccount.getStatus());
    }

    @Test
    void unknownApiPathReturnsNotFound() {
        ResponseEntity<String> response = admin().getForEntity("/api/unknown", String.class);
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    void requestsWithoutAuthenticationAreRejected() {
        ResponseEntity<String> response = restTemplate.getForEntity("/api/accounts", String.class);
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
    }

    @Test
    void loginReturnsSessionForValidCredentialsAndRejectsBadOnes() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, String> goodForm = new LinkedMultiValueMap<>();
        goodForm.add("username", "admin");
        goodForm.add("password", "admin");
        ResponseEntity<String> ok = restTemplate.postForEntity(
                "/api/login", new HttpEntity<>(goodForm, headers), String.class);
        assertEquals(HttpStatus.OK, ok.getStatusCode());
        assertNotNull(ok.getBody());
        assertTrue(ok.getBody().contains("\"username\":\"admin\""));
        assertTrue(ok.getBody().contains("\"role\":\"ADMIN\""));

        MultiValueMap<String, String> badForm = new LinkedMultiValueMap<>();
        badForm.add("username", "admin");
        badForm.add("password", "wrong");
        ResponseEntity<String> unauthorized = restTemplate.postForEntity(
                "/api/login", new HttpEntity<>(badForm, headers), String.class);
        assertEquals(HttpStatus.UNAUTHORIZED, unauthorized.getStatusCode());
    }

    @Test
    void clientUserSeesOnlyOwnDataAndCannotReachOthersOrAdminEndpoints() {
        ClientDto clientA = admin().postForObject("/api/clients/individual",
                Map.of("firstName", "Anna", "lastName", "Asenova", "egn", "9001011234"), ClientDto.class);
        ClientDto clientB = admin().postForObject("/api/clients/individual",
                Map.of("firstName", "Boris", "lastName", "Borisov", "egn", "9002021234"), ClientDto.class);

        admin().postForObject("/api/accounts",
                Map.of("clientId", clientA.getId(), "iban", "BG18BANK555555OWNERA01", "initialBalance", "0.00"),
                BankAccountDto.class);
        admin().postForObject("/api/accounts",
                Map.of("clientId", clientB.getId(), "iban", "BG19BANK666666OWNERB01", "initialBalance", "0.00"),
                BankAccountDto.class);

        CreditProductDto[] products = admin().getForObject("/api/credits/products", CreditProductDto[].class);
        CreditDto creditA = admin().postForObject("/api/credits",
                Map.of("clientId", clientA.getId(), "creditProductId", products[0].getId(),
                        "principalAmount", "5000.00", "termMonths", 6, "startDate", "2026-06-01"),
                CreditDto.class);
        CreditDto creditB = admin().postForObject("/api/credits",
                Map.of("clientId", clientB.getId(), "creditProductId", products[0].getId(),
                        "principalAmount", "5000.00", "termMonths", 6, "startDate", "2026-06-01"),
                CreditDto.class);

        // Admin provisions a login for client A only.
        ResponseEntity<String> userCreated = admin().postForEntity("/api/users",
                Map.of("clientId", clientA.getId(), "username", "anna", "password", "anna123"), String.class);
        assertEquals(HttpStatus.OK, userCreated.getStatusCode());

        TestRestTemplate anna = asClient("anna", "anna123");

        BankAccountDto[] visibleAccounts = anna.getForObject("/api/accounts", BankAccountDto[].class);
        assertEquals(1, visibleAccounts.length);
        assertEquals(clientA.getId(), visibleAccounts[0].getClientId());

        CreditDto[] visibleCredits = anna.getForObject("/api/credits", CreditDto[].class);
        assertEquals(1, visibleCredits.length);
        assertEquals(creditA.getId(), visibleCredits[0].getId());

        // Reading another client's repayment plan is forbidden (IDOR guard).
        ResponseEntity<String> foreignPlan = anna.getForEntity(
                "/api/credits/" + creditB.getId() + "/installments", String.class);
        assertEquals(HttpStatus.FORBIDDEN, foreignPlan.getStatusCode());

        // The same guard protects the credit status endpoint.
        ResponseEntity<String> foreignStatus = anna.getForEntity(
                "/api/credits/" + creditB.getId() + "/status", String.class);
        assertEquals(HttpStatus.FORBIDDEN, foreignStatus.getStatusCode());

        // Own repayment plan is visible and the first installment can be paid.
        RepaymentInstallmentDto[] ownPlan = anna.getForObject(
                "/api/credits/" + creditA.getId() + "/installments", RepaymentInstallmentDto[].class);
        assertEquals(6, ownPlan.length);
        RepaymentInstallmentDto paid = anna.patchForObject(
                "/api/credits/installments/" + ownPlan[0].getId() + "/pay", null, RepaymentInstallmentDto.class);
        assertEquals("PAID", paid.getStatus());

        // Paying a foreign installment is forbidden.
        RepaymentInstallmentDto[] foreignOwnerPlan = admin().getForObject(
                "/api/credits/" + creditB.getId() + "/installments", RepaymentInstallmentDto[].class);
        ResponseEntity<String> foreignPay = anna.exchange(
                "/api/credits/installments/" + foreignOwnerPlan[0].getId() + "/pay",
                HttpMethod.PATCH, null, String.class);
        assertEquals(HttpStatus.FORBIDDEN, foreignPay.getStatusCode());

        // Operator-only endpoints are forbidden for a client user.
        ResponseEntity<String> createClient = anna.postForEntity("/api/clients/individual",
                Map.of("firstName", "X", "lastName", "Y", "egn", "9009091234"), String.class);
        assertEquals(HttpStatus.FORBIDDEN, createClient.getStatusCode());

        ResponseEntity<String> dashboard = anna.getForEntity("/api/dashboard", String.class);
        assertEquals(HttpStatus.FORBIDDEN, dashboard.getStatusCode());
    }

    @Test
    void userProvisioningRejectsDuplicates() {
        ClientDto client = admin().postForObject("/api/clients/individual",
                Map.of("firstName", "Galina", "lastName", "Geneva", "egn", "9003031234"), ClientDto.class);

        ResponseEntity<String> first = admin().postForEntity("/api/users",
                Map.of("clientId", client.getId(), "username", "galina", "password", "galina1"), String.class);
        assertEquals(HttpStatus.OK, first.getStatusCode());

        // Same client cannot get a second login.
        ResponseEntity<String> secondForClient = admin().postForEntity("/api/users",
                Map.of("clientId", client.getId(), "username", "galina2", "password", "galina1"), String.class);
        assertEquals(HttpStatus.BAD_REQUEST, secondForClient.getStatusCode());

        ClientDto other = admin().postForObject("/api/clients/individual",
                Map.of("firstName", "Hristo", "lastName", "Hristov", "egn", "9004041234"), ClientDto.class);

        // Username must be unique.
        ResponseEntity<String> duplicateUsername = admin().postForEntity("/api/users",
                Map.of("clientId", other.getId(), "username", "galina", "password", "galina1"), String.class);
        assertEquals(HttpStatus.BAD_REQUEST, duplicateUsername.getStatusCode());
    }

    @Test
    void duplicateIbanAndUnknownClientAreRejected() {
        ClientDto client = admin().postForObject(
                "/api/clients/individual",
                Map.of(
                        "firstName", "Ivan",
                        "lastName", "Georgiev",
                        "egn", "9202021234"
                ),
                ClientDto.class
        );

        BankAccountDto account = admin().postForObject(
                "/api/accounts",
                Map.of(
                        "clientId", client.getId(),
                        "iban", "BG70BANK333333DUPL0001",
                        "initialBalance", "0.00"
                ),
                BankAccountDto.class
        );
        assertNotNull(account.getId());

        ResponseEntity<String> duplicateIban = admin().postForEntity(
                "/api/accounts",
                Map.of(
                        "clientId", client.getId(),
                        "iban", "BG70BANK333333DUPL0001",
                        "initialBalance", "0.00"
                ),
                String.class
        );
        assertEquals(HttpStatus.BAD_REQUEST, duplicateIban.getStatusCode());
        assertNotNull(duplicateIban.getBody());
        assertTrue(duplicateIban.getBody().contains("Bank account with this IBAN already exists."));

        ResponseEntity<String> unknownClient = admin().postForEntity(
                "/api/accounts",
                Map.of(
                        "clientId", 999999,
                        "iban", "BG70BANK444444DUPL0002",
                        "initialBalance", "0.00"
                ),
                String.class
        );
        assertEquals(HttpStatus.NOT_FOUND, unknownClient.getStatusCode());
        assertNotNull(unknownClient.getBody());
        assertTrue(unknownClient.getBody().contains("Client with id 999999 was not found."));
    }
}
