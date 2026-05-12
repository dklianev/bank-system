package org.informatics.bank_system.service.impl;

import lombok.RequiredArgsConstructor;
import org.informatics.bank_system.data.entity.Client;
import org.informatics.bank_system.data.entity.Credit;
import org.informatics.bank_system.data.entity.CreditProduct;
import org.informatics.bank_system.data.entity.CreditStatus;
import org.informatics.bank_system.data.entity.InstallmentStatus;
import org.informatics.bank_system.data.entity.RepaymentInstallment;
import org.informatics.bank_system.data.entity.User;
import org.informatics.bank_system.data.repository.CreditProductRepository;
import org.informatics.bank_system.data.repository.CreditRepository;
import org.informatics.bank_system.data.repository.RepaymentInstallmentRepository;
import org.informatics.bank_system.dto.CreateCreditDto;
import org.informatics.bank_system.dto.CreditDto;
import org.informatics.bank_system.dto.CreditProductDto;
import org.informatics.bank_system.dto.RepaymentInstallmentDto;
import org.informatics.bank_system.exception.BusinessRuleException;
import org.informatics.bank_system.exception.ObjectNotFoundException;
import org.informatics.bank_system.service.ClientService;
import org.informatics.bank_system.service.CreditService;
import org.informatics.bank_system.service.UserService;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CreditServiceImpl implements CreditService {

    private final CreditRepository creditRepository;
    private final CreditProductRepository creditProductRepository;
    private final RepaymentInstallmentRepository installmentRepository;
    private final ClientService clientService;
    private final UserService userService;
    private final RepaymentPlanCalculator repaymentPlanCalculator;

    @Override
    @Transactional(readOnly = true)
    public List<CreditProductDto> getCreditProducts() {
        return creditProductRepository.findAll()
                .stream()
                .map(this::mapCreditProduct)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<CreditDto> getCredits() {
        User currentUser = userService.getCurrentUser();
        List<Credit> credits = currentUser.hasAuthority(UserServiceImpl.ROLE_ADMIN)
                ? creditRepository.findAll()
                : creditRepository.findByClientId(currentUser.getClient().getId());

        return credits.stream()
                .map(this::mapCredit)
                .toList();
    }

    @Override
    @Transactional
    public CreditDto createCredit(CreateCreditDto dto) {
        Client client = clientService.getClientEntity(dto.getClientId());
        CreditProduct product = creditProductRepository.findById(dto.getCreditProductId())
                .orElseThrow(() -> new ObjectNotFoundException("Credit product with id " + dto.getCreditProductId() + " was not found."));

        if (dto.getPrincipalAmount().compareTo(product.getMaxAmount()) > 0) {
            throw new BusinessRuleException("Requested amount is above the maximum for this credit type.");
        }

        if (dto.getTermMonths() > product.getMaxTermMonths()) {
            throw new BusinessRuleException("Requested term is above the maximum for this credit type.");
        }

        LocalDate startDate = dto.getStartDate() == null ? LocalDate.now() : dto.getStartDate();

        Credit credit = new Credit();
        credit.setClient(client);
        credit.setCreditProduct(product);
        credit.setPrincipalAmount(dto.getPrincipalAmount());
        credit.setTermMonths(dto.getTermMonths());
        credit.setAnnualInterestRate(product.getAnnualInterestRate());
        credit.setStartDate(startDate);
        credit.setStatus(CreditStatus.ACTIVE);

        List<RepaymentInstallment> installments = repaymentPlanCalculator.generate(
                dto.getPrincipalAmount(),
                product.getAnnualInterestRate(),
                dto.getTermMonths(),
                startDate
        );

        credit.setMonthlyPayment(installments.getFirst().getPaymentAmount());
        installments.forEach(credit::addInstallment);

        return mapCredit(creditRepository.save(credit));
    }

    @Override
    @Transactional(readOnly = true)
    public List<RepaymentInstallmentDto> getInstallments(Long creditId) {
        Credit credit = creditRepository.findById(creditId)
                .orElseThrow(() -> new ObjectNotFoundException("Credit with id " + creditId + " was not found."));
        assertCanAccessCredit(credit);

        return installmentRepository.findByCreditIdOrderByInstallmentNumber(creditId)
                .stream()
                .map(this::mapInstallment)
                .toList();
    }

    @Override
    @Transactional
    public RepaymentInstallmentDto payInstallment(Long installmentId) {
        RepaymentInstallment installment = installmentRepository.findById(installmentId)
                .orElseThrow(() -> new ObjectNotFoundException("Installment with id " + installmentId + " was not found."));

        assertCanAccessCredit(installment.getCredit());

        if (installment.getStatus() == InstallmentStatus.PAID) {
            throw new BusinessRuleException("Installment is already paid.");
        }

        boolean hasUnpaidPreviousInstallments = installmentRepository
                .findByCreditIdOrderByInstallmentNumber(installment.getCredit().getId())
                .stream()
                .anyMatch(current -> current.getInstallmentNumber() < installment.getInstallmentNumber()
                        && current.getStatus() != InstallmentStatus.PAID);

        if (hasUnpaidPreviousInstallments) {
            throw new BusinessRuleException("Previous installments must be paid first.");
        }

        installment.setStatus(InstallmentStatus.PAID);
        installment.setPaidAt(LocalDateTime.now());
        RepaymentInstallment saved = installmentRepository.save(installment);
        updateCreditStatus(saved.getCredit());

        return mapInstallment(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public CreditDto getCreditStatus(Long creditId) {
        Credit credit = creditRepository.findById(creditId)
                .orElseThrow(() -> new ObjectNotFoundException("Credit with id " + creditId + " was not found."));
        assertCanAccessCredit(credit);

        return mapCredit(credit);
    }

    // ADMIN works with every credit; a CLIENT user may only touch credits of the linked bank client.
    private void assertCanAccessCredit(Credit credit) {
        User currentUser = userService.getCurrentUser();
        if (currentUser.hasAuthority(UserServiceImpl.ROLE_ADMIN)) {
            return;
        }

        if (!credit.getClient().getId().equals(currentUser.getClient().getId())) {
            throw new AccessDeniedException("You can only access your own credits.");
        }
    }

    private void updateCreditStatus(Credit credit) {
        boolean allPaid = credit.getInstallments()
                .stream()
                .allMatch(installment -> installment.getStatus() == InstallmentStatus.PAID);

        if (allPaid) {
            credit.setStatus(CreditStatus.PAID_OFF);
            creditRepository.save(credit);
        }
    }

    private CreditProductDto mapCreditProduct(CreditProduct product) {
        return CreditProductDto.builder()
                .id(product.getId())
                .code(product.getCode().name())
                .name(product.getName())
                .annualInterestRate(product.getAnnualInterestRate())
                .maxAmount(product.getMaxAmount())
                .maxTermMonths(product.getMaxTermMonths())
                .build();
    }

    private CreditDto mapCredit(Credit credit) {
        int paidInstallments = (int) credit.getInstallments()
                .stream()
                .filter(installment -> installment.getStatus() == InstallmentStatus.PAID)
                .count();

        return CreditDto.builder()
                .id(credit.getId())
                .clientId(credit.getClient().getId())
                .clientDisplayName(credit.getClient().getDisplayName())
                .creditProductCode(credit.getCreditProduct().getCode().name())
                .creditProductName(credit.getCreditProduct().getName())
                .principalAmount(credit.getPrincipalAmount())
                .termMonths(credit.getTermMonths())
                .annualInterestRate(credit.getAnnualInterestRate())
                .monthlyPayment(credit.getMonthlyPayment())
                .startDate(credit.getStartDate())
                .status(credit.getStatus().name())
                .paidInstallments(paidInstallments)
                .totalInstallments(credit.getInstallments().size())
                .build();
    }

    private RepaymentInstallmentDto mapInstallment(RepaymentInstallment installment) {
        return RepaymentInstallmentDto.builder()
                .id(installment.getId())
                .creditId(installment.getCredit().getId())
                .installmentNumber(installment.getInstallmentNumber())
                .dueDate(installment.getDueDate())
                .paymentAmount(installment.getPaymentAmount())
                .principalPart(installment.getPrincipalPart())
                .interestPart(installment.getInterestPart())
                .remainingPrincipal(installment.getRemainingPrincipal())
                .status(installment.getStatus().name())
                .paidAt(installment.getPaidAt())
                .build();
    }
}
