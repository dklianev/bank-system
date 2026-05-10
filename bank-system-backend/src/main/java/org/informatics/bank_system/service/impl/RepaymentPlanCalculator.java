package org.informatics.bank_system.service.impl;

import org.informatics.bank_system.data.entity.InstallmentStatus;
import org.informatics.bank_system.data.entity.RepaymentInstallment;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Component
public class RepaymentPlanCalculator {

    private static final MathContext MATH_CONTEXT = MathContext.DECIMAL128;
    private static final BigDecimal MONTHS_IN_YEAR_PERCENT = BigDecimal.valueOf(1200);

    public List<RepaymentInstallment> generate(
            BigDecimal principal,
            BigDecimal annualInterestRate,
            int termMonths,
            LocalDate startDate
    ) {
        BigDecimal monthlyRate = annualInterestRate.divide(MONTHS_IN_YEAR_PERCENT, MATH_CONTEXT);
        BigDecimal monthlyPayment = calculateMonthlyPayment(principal, monthlyRate, termMonths);
        BigDecimal remaining = principal.setScale(2, RoundingMode.HALF_UP);
        List<RepaymentInstallment> installments = new ArrayList<>();

        for (int month = 1; month <= termMonths; month++) {
            BigDecimal interestPart = remaining.multiply(monthlyRate).setScale(2, RoundingMode.HALF_UP);
            BigDecimal principalPart = monthlyPayment.subtract(interestPart).setScale(2, RoundingMode.HALF_UP);
            BigDecimal paymentAmount = monthlyPayment;

            if (month == termMonths || principalPart.compareTo(remaining) > 0) {
                principalPart = remaining;
                paymentAmount = principalPart.add(interestPart).setScale(2, RoundingMode.HALF_UP);
            }

            remaining = remaining.subtract(principalPart).setScale(2, RoundingMode.HALF_UP);
            if (remaining.compareTo(BigDecimal.ZERO) < 0) {
                remaining = BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
            }

            RepaymentInstallment installment = new RepaymentInstallment();
            installment.setInstallmentNumber(month);
            installment.setDueDate(startDate.plusMonths(month));
            installment.setPaymentAmount(paymentAmount);
            installment.setPrincipalPart(principalPart);
            installment.setInterestPart(interestPart);
            installment.setRemainingPrincipal(remaining);
            installment.setStatus(InstallmentStatus.PENDING);
            installments.add(installment);
        }

        return installments;
    }

    public BigDecimal calculateMonthlyPayment(BigDecimal principal, BigDecimal monthlyRate, int termMonths) {
        if (monthlyRate.compareTo(BigDecimal.ZERO) == 0) {
            return principal.divide(BigDecimal.valueOf(termMonths), 2, RoundingMode.HALF_UP);
        }

        BigDecimal factor = BigDecimal.ONE.add(monthlyRate).pow(termMonths, MATH_CONTEXT);
        BigDecimal numerator = principal.multiply(monthlyRate, MATH_CONTEXT).multiply(factor, MATH_CONTEXT);
        BigDecimal denominator = factor.subtract(BigDecimal.ONE, MATH_CONTEXT);

        return numerator.divide(denominator, 2, RoundingMode.HALF_UP);
    }
}
