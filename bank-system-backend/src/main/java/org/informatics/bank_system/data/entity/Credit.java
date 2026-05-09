package org.informatics.bank_system.data.entity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.Check;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(
        name = "credits",
        indexes = {
                @Index(name = "idx_credits_client_id", columnList = "client_id"),
                @Index(name = "idx_credits_product_id", columnList = "credit_product_id")
        }
)
@Getter
@Setter
@NoArgsConstructor
@Check(constraints = "principal_amount > 0 and term_months >= 1 and annual_interest_rate > 0 and monthly_payment > 0")
public class Credit extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "client_id", nullable = false)
    private Client client;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "credit_product_id", nullable = false)
    private CreditProduct creditProduct;

    @DecimalMin(value = "0.01")
    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal principalAmount;

    @Min(1)
    @Column(nullable = false)
    private Integer termMonths;

    @Column(nullable = false, precision = 5, scale = 2)
    private BigDecimal annualInterestRate;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal monthlyPayment;

    @Column(nullable = false)
    private LocalDate startDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private CreditStatus status = CreditStatus.ACTIVE;

    @OneToMany(mappedBy = "credit", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<RepaymentInstallment> installments = new ArrayList<>();

    public void addInstallment(RepaymentInstallment installment) {
        installments.add(installment);
        installment.setCredit(this);
    }
}
