package org.informatics.bank_system.service;

import org.informatics.bank_system.data.entity.RepaymentInstallment;
import org.informatics.bank_system.service.impl.RepaymentPlanCalculator;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RepaymentPlanCalculatorTest {

    private final RepaymentPlanCalculator calculator = new RepaymentPlanCalculator();

    @Test
    void generateCreatesAnnuityPlanWithDecreasingInterestAndZeroFinalPrincipal() {
        List<RepaymentInstallment> installments = calculator.generate(
                new BigDecimal("10000.00"),
                new BigDecimal("7.50"),
                12,
                LocalDate.of(2026, 6, 1)
        );

        assertEquals(12, installments.size());
        assertEquals(new BigDecimal("0.00"), installments.getLast().getRemainingPrincipal());
        assertTrue(installments.getFirst().getInterestPart().compareTo(installments.getLast().getInterestPart()) > 0);
        assertTrue(installments.getFirst().getPrincipalPart().compareTo(installments.getLast().getPrincipalPart()) < 0);
    }

    @Test
    void calculateMonthlyPaymentHandlesZeroInterest() {
        BigDecimal payment = calculator.calculateMonthlyPayment(
                new BigDecimal("1200.00"),
                BigDecimal.ZERO,
                12
        );

        assertEquals(new BigDecimal("100.00"), payment);
    }

    @Test
    void calculateMonthlyPaymentMatchesAnnuityFormulaReferenceValue() {
        BigDecimal payment = calculator.calculateMonthlyPayment(
                new BigDecimal("10000.00"),
                new BigDecimal("0.00625"),
                12
        );

        assertEquals(new BigDecimal("867.57"), payment);
    }

    @Test
    void principalPartsSumExactlyToGrantedAmount() {
        List<RepaymentInstallment> installments = calculator.generate(
                new BigDecimal("10000.00"),
                new BigDecimal("7.50"),
                12,
                LocalDate.of(2026, 6, 1)
        );

        BigDecimal principalSum = installments.stream()
                .map(RepaymentInstallment::getPrincipalPart)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        assertEquals(new BigDecimal("10000.00"), principalSum);
    }

    @Test
    void longTermMortgagePlanLosesNoMoneyToRounding() {
        List<RepaymentInstallment> installments = calculator.generate(
                new BigDecimal("200000.00"),
                new BigDecimal("4.20"),
                360,
                LocalDate.of(2026, 6, 1)
        );

        BigDecimal principalSum = installments.stream()
                .map(RepaymentInstallment::getPrincipalPart)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        assertEquals(360, installments.size());
        assertEquals(new BigDecimal("200000.00"), principalSum);
        assertEquals(new BigDecimal("0.00"), installments.getLast().getRemainingPrincipal());
    }

    @Test
    void paymentsAreEqualExceptLastInstallmentWhichAbsorbsRounding() {
        List<RepaymentInstallment> installments = calculator.generate(
                new BigDecimal("10000.00"),
                new BigDecimal("7.50"),
                12,
                LocalDate.of(2026, 6, 1)
        );

        BigDecimal regularPayment = installments.getFirst().getPaymentAmount();
        for (int i = 0; i < installments.size() - 1; i++) {
            assertEquals(regularPayment, installments.get(i).getPaymentAmount());
        }

        RepaymentInstallment last = installments.getLast();
        assertEquals(last.getPrincipalPart().add(last.getInterestPart()), last.getPaymentAmount());
    }
}
