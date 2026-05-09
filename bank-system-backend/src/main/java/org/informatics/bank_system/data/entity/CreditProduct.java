package org.informatics.bank_system.data.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.Check;

import java.math.BigDecimal;

@Entity
@Table(
        name = "credit_products",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_credit_products_code", columnNames = "code")
        }
)
@Getter
@Setter
@NoArgsConstructor
@Check(constraints = "annual_interest_rate > 0 and max_amount > 0 and max_term_months >= 1")
public class CreditProduct extends BaseEntity {

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30, unique = true)
    private CreditProductCode code;

    @NotBlank
    @Column(nullable = false, length = 80)
    private String name;

    @DecimalMin(value = "0.01")
    @Column(nullable = false, precision = 5, scale = 2)
    private BigDecimal annualInterestRate;

    @DecimalMin(value = "0.01")
    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal maxAmount;

    @Min(1)
    @Column(nullable = false)
    private Integer maxTermMonths;
}
