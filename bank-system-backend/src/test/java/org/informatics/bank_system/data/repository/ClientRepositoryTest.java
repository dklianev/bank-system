package org.informatics.bank_system.data.repository;

import org.informatics.bank_system.data.entity.AccountStatus;
import org.informatics.bank_system.data.entity.BankAccount;
import org.informatics.bank_system.data.entity.Client;
import org.informatics.bank_system.data.entity.IndividualClient;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DataJpaTest
class ClientRepositoryTest {

    @Autowired
    private ClientRepository clientRepository;

    @Autowired
    private IndividualClientRepository individualClientRepository;

    @Autowired
    private BankAccountRepository bankAccountRepository;

    @Test
    void joinedClientModelStoresIndividualClientAndAccountRelationship() {
        IndividualClient client = new IndividualClient();
        client.setFirstName("Dimitar");
        client.setLastName("Klianev");
        client.setEgn("9901011234");
        IndividualClient savedClient = individualClientRepository.save(client);

        BankAccount account = new BankAccount();
        account.setClient(savedClient);
        account.setIban("BG12TEST123456ABCDEFGH");
        account.setBalance(new BigDecimal("250.00"));
        account.setStatus(AccountStatus.ACTIVE);
        bankAccountRepository.save(account);

        List<Client> clients = clientRepository.findAll();

        assertEquals(1, clients.size());
        assertInstanceOf(IndividualClient.class, clients.getFirst());
        assertEquals(1, bankAccountRepository.findByClientId(savedClient.getId()).size());
        assertTrue(individualClientRepository.existsByEgn("9901011234"));
    }
}
