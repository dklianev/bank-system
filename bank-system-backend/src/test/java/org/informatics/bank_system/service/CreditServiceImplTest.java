package org.informatics.bank_system.service;

import org.informatics.bank_system.data.entity.Credit;
import org.informatics.bank_system.data.entity.CreditProduct;
import org.informatics.bank_system.data.entity.CreditProductCode;
import org.informatics.bank_system.data.entity.CreditStatus;
import org.informatics.bank_system.data.entity.IndividualClient;
import org.informatics.bank_system.data.entity.InstallmentStatus;
import org.informatics.bank_system.data.entity.RepaymentInstallment;
import org.informatics.bank_system.data.entity.Role;
import org.informatics.bank_system.data.entity.User;
import org.informatics.bank_system.data.repository.CreditProductRepository;
import org.informatics.bank_system.data.repository.CreditRepository;
import org.informatics.bank_system.data.repository.RepaymentInstallmentRepository;
import org.informatics.bank_system.dto.CreateCreditDto;
import org.informatics.bank_system.dto.CreditDto;
import org.informatics.bank_system.dto.RepaymentInstallmentDto;
import org.informatics.bank_system.exception.BusinessRuleException;
import org.informatics.bank_system.exception.ObjectNotFoundException;
import org.informatics.bank_system.service.impl.CreditServiceImpl;
import org.informatics.bank_system.service.impl.RepaymentPlanCalculator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;

@ExtendWith(MockitoExtension.class)
class CreditServiceImplTest {

    @Mock
    private CreditRepository creditRepository;

    @Mock
    private CreditProductRepository creditProductRepository;

    @Mock
    private RepaymentInstallmentRepository installmentRepository;

    @Mock
    private ClientService clientService;

    @Mock
    private UserService userService;

    @Spy
    private RepaymentPlanCalculator repaymentPlanCalculator = new RepaymentPlanCalculator();

    @InjectMocks
    private CreditServiceImpl creditService;

    @Test
    void createCreditRejectsAmountAboveProductMaximum() {
        CreateCreditDto dto = createCreditDto(new BigDecimal("60000.00"), 12);

        Mockito.when(clientService.getClientEntity(1L)).thenReturn(individualClient());
        Mockito.when(creditProductRepository.findById(2L)).thenReturn(Optional.of(consumerProduct()));

        BusinessRuleException exception =
                assertThrows(BusinessRuleException.class, () -> creditService.createCredit(dto));

        assertEquals("Requested amount is above the maximum for this credit type.", exception.getMessage());
        Mockito.verify(creditRepository, Mockito.never()).save(any());
    }

    @Test
    void createCreditRejectsTermAboveProductMaximum() {
        CreateCreditDto dto = createCreditDto(new BigDecimal("10000.00"), 240);

        Mockito.when(clientService.getClientEntity(1L)).thenReturn(individualClient());
        Mockito.when(creditProductRepository.findById(2L)).thenReturn(Optional.of(consumerProduct()));

        BusinessRuleException exception =
                assertThrows(BusinessRuleException.class, () -> creditService.createCredit(dto));

        assertEquals("Requested term is above the maximum for this credit type.", exception.getMessage());
        Mockito.verify(creditRepository, Mockito.never()).save(any());
    }

    @Test
    void createCreditFailsForUnknownCreditProduct() {
        CreateCreditDto dto = createCreditDto(new BigDecimal("10000.00"), 12);
        dto.setCreditProductId(99L);

        Mockito.when(clientService.getClientEntity(1L)).thenReturn(individualClient());
        Mockito.when(creditProductRepository.findById(99L)).thenReturn(Optional.empty());

        ObjectNotFoundException exception =
                assertThrows(ObjectNotFoundException.class, () -> creditService.createCredit(dto));

        assertEquals("Credit product with id 99 was not found.", exception.getMessage());
        Mockito.verify(creditRepository, Mockito.never()).save(any());
    }

    @Test
    void createCreditSnapshotsProductRateAndGeneratesFullPlan() {
        CreateCreditDto dto = createCreditDto(new BigDecimal("10000.00"), 12);

        Mockito.when(clientService.getClientEntity(1L)).thenReturn(individualClient());
        Mockito.when(creditProductRepository.findById(2L)).thenReturn(Optional.of(consumerProduct()));
        Mockito.when(creditRepository.save(any(Credit.class))).thenAnswer(invocation -> invocation.getArgument(0));

        CreditDto created = creditService.createCredit(dto);

        assertEquals("ACTIVE", created.getStatus());
        assertEquals(12, created.getTotalInstallments());
        assertEquals(0, created.getPaidInstallments());
        assertEquals(new BigDecimal("7.50"), created.getAnnualInterestRate());
        assertEquals(new BigDecimal("867.57"), created.getMonthlyPayment());
    }

    @Test
    void payInstallmentRejectsAlreadyPaidInstallment() {
        RepaymentInstallment installment = new RepaymentInstallment();
        installment.setId(10L);
        installment.setInstallmentNumber(1);
        installment.setStatus(InstallmentStatus.PAID);

        Mockito.when(installmentRepository.findById(10L)).thenReturn(Optional.of(installment));
        Mockito.when(userService.getCurrentUser()).thenReturn(adminUser());

        BusinessRuleException exception =
                assertThrows(BusinessRuleException.class, () -> creditService.payInstallment(10L));

        assertEquals("Installment is already paid.", exception.getMessage());
        Mockito.verify(installmentRepository, Mockito.never()).save(any());
    }

    @Test
    void payInstallmentRejectsPaymentWhilePreviousInstallmentIsPending() {
        Credit credit = activeCredit();
        RepaymentInstallment first = installment(credit, 1, InstallmentStatus.PENDING);
        RepaymentInstallment second = installment(credit, 2, InstallmentStatus.PENDING);
        second.setId(11L);

        Mockito.when(installmentRepository.findById(11L)).thenReturn(Optional.of(second));
        Mockito.when(userService.getCurrentUser()).thenReturn(adminUser());
        Mockito.when(installmentRepository.findByCreditIdOrderByInstallmentNumber(5L)).thenReturn(List.of(first, second));

        BusinessRuleException exception =
                assertThrows(BusinessRuleException.class, () -> creditService.payInstallment(11L));

        assertEquals("Previous installments must be paid first.", exception.getMessage());
        Mockito.verify(installmentRepository, Mockito.never()).save(any());
    }

    @Test
    void payInstallmentMarksCreditPaidOffAfterFinalPayment() {
        Credit credit = activeCredit();
        RepaymentInstallment first = installment(credit, 1, InstallmentStatus.PAID);
        RepaymentInstallment second = installment(credit, 2, InstallmentStatus.PENDING);
        second.setId(12L);

        Mockito.when(installmentRepository.findById(12L)).thenReturn(Optional.of(second));
        Mockito.when(userService.getCurrentUser()).thenReturn(adminUser());
        Mockito.when(installmentRepository.findByCreditIdOrderByInstallmentNumber(5L)).thenReturn(List.of(first, second));
        Mockito.when(installmentRepository.save(second)).thenReturn(second);

        RepaymentInstallmentDto paid = creditService.payInstallment(12L);

        assertEquals("PAID", paid.getStatus());
        assertEquals(CreditStatus.PAID_OFF, credit.getStatus());
        Mockito.verify(creditRepository).save(credit);
    }

    @Test
    void getInstallmentsRejectsCreditOwnedByAnotherClient() {
        Credit credit = activeCredit();

        Mockito.when(creditRepository.findById(5L)).thenReturn(Optional.of(credit));
        Mockito.when(userService.getCurrentUser()).thenReturn(clientUser(2L));

        AccessDeniedException exception =
                assertThrows(AccessDeniedException.class, () -> creditService.getInstallments(5L));

        assertEquals("You can only access your own credits.", exception.getMessage());
    }

    @Test
    void getInstallmentsReturnsPlanForOwnCredit() {
        Credit credit = activeCredit();
        RepaymentInstallment first = installment(credit, 1, InstallmentStatus.PENDING);

        Mockito.when(creditRepository.findById(5L)).thenReturn(Optional.of(credit));
        Mockito.when(userService.getCurrentUser()).thenReturn(clientUser(1L));
        Mockito.when(installmentRepository.findByCreditIdOrderByInstallmentNumber(5L)).thenReturn(List.of(first));

        List<RepaymentInstallmentDto> installments = creditService.getInstallments(5L);

        assertEquals(1, installments.size());
    }

    @Test
    void getCreditStatusRejectsCreditOwnedByAnotherClient() {
        Credit credit = activeCredit();

        Mockito.when(creditRepository.findById(5L)).thenReturn(Optional.of(credit));
        Mockito.when(userService.getCurrentUser()).thenReturn(clientUser(2L));

        AccessDeniedException exception =
                assertThrows(AccessDeniedException.class, () -> creditService.getCreditStatus(5L));

        assertEquals("You can only access your own credits.", exception.getMessage());
    }

    @Test
    void getCreditStatusReturnsStatusForOwnCredit() {
        Credit credit = activeCredit();

        Mockito.when(creditRepository.findById(5L)).thenReturn(Optional.of(credit));
        Mockito.when(userService.getCurrentUser()).thenReturn(clientUser(1L));

        CreditDto status = creditService.getCreditStatus(5L);

        assertEquals(credit.getId(), status.getId());
    }

    private CreateCreditDto createCreditDto(BigDecimal principalAmount, int termMonths) {
        CreateCreditDto dto = new CreateCreditDto();
        dto.setClientId(1L);
        dto.setCreditProductId(2L);
        dto.setPrincipalAmount(principalAmount);
        dto.setTermMonths(termMonths);
        dto.setStartDate(LocalDate.of(2026, 6, 1));
        return dto;
    }

    private IndividualClient individualClient() {
        IndividualClient client = new IndividualClient();
        client.setId(1L);
        client.setFirstName("Dimitar");
        client.setLastName("Klianev");
        client.setEgn("9901011234");
        return client;
    }

    private CreditProduct consumerProduct() {
        CreditProduct product = new CreditProduct();
        product.setId(2L);
        product.setCode(CreditProductCode.CONSUMER);
        product.setName("Consumer credit");
        product.setAnnualInterestRate(new BigDecimal("7.50"));
        product.setMaxAmount(new BigDecimal("50000.00"));
        product.setMaxTermMonths(120);
        return product;
    }

    private Credit activeCredit() {
        Credit credit = new Credit();
        credit.setId(5L);
        credit.setClient(individualClient());
        credit.setCreditProduct(consumerProduct());
        credit.setPrincipalAmount(new BigDecimal("1200.00"));
        credit.setTermMonths(2);
        credit.setAnnualInterestRate(new BigDecimal("7.50"));
        credit.setMonthlyPayment(new BigDecimal("603.76"));
        credit.setStartDate(LocalDate.of(2026, 6, 1));
        return credit;
    }

    private RepaymentInstallment installment(Credit credit, int number, InstallmentStatus status) {
        RepaymentInstallment installment = new RepaymentInstallment();
        installment.setInstallmentNumber(number);
        installment.setDueDate(credit.getStartDate().plusMonths(number));
        installment.setPaymentAmount(new BigDecimal("603.76"));
        installment.setPrincipalPart(new BigDecimal("600.00"));
        installment.setInterestPart(new BigDecimal("3.76"));
        installment.setRemainingPrincipal(new BigDecimal("0.00"));
        installment.setStatus(status);
        credit.addInstallment(installment);
        return installment;
    }

    private User adminUser() {
        Role adminRole = new Role();
        adminRole.setAuthority("ADMIN");
        User user = new User();
        user.setUsername("admin");
        user.getAuthorities().add(adminRole);
        return user;
    }

    private User clientUser(Long clientId) {
        Role clientRole = new Role();
        clientRole.setAuthority("CLIENT");
        IndividualClient client = new IndividualClient();
        client.setId(clientId);
        client.setFirstName("Client");
        client.setLastName("Number" + clientId);
        client.setEgn("0000000000");
        User user = new User();
        user.setUsername("client" + clientId);
        user.setClient(client);
        user.getAuthorities().add(clientRole);
        return user;
    }
}
