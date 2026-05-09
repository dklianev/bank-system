package org.informatics.bank_system.data.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.Check;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(
        name = "repayment_installments",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_installments_credit_number", columnNames = {"credit_id", "installment_number"})
        },
        indexes = {
                @Index(name = "idx_installments_credit_id", columnList = "credit_id")
        }
)
@Getter
@Setter
@NoArgsConstructor
@Check(constraints = "installment_number >= 1 and payment_amount >= 0 and principal_part >= 0 and interest_part >= 0 and remaining_principal >= 0")
public class RepaymentInstallment extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "credit_id", nullable = false)
    private Credit credit;

    @Min(1)
    @Column(name = "installment_number", nullable = false)
    private Integer installmentNumber;

    @Column(nullable = false)
    private LocalDate dueDate;

    @DecimalMin(value = "0.00")
    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal paymentAmount;

    @DecimalMin(value = "0.00")
    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal principalPart;

    @DecimalMin(value = "0.00")
    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal interestPart;

    @DecimalMin(value = "0.00")
    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal remainingPrincipal;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private InstallmentStatus status = InstallmentStatus.PENDING;

    private LocalDateTime paidAt;
}
