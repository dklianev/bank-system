package org.informatics.bank_system.data.entity;

import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(
        name = "legal_clients",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_legal_clients_eik", columnNames = "eik")
        }
)
@DiscriminatorValue("LEGAL")
@Getter
@Setter
@NoArgsConstructor
public class LegalClient extends Client {

    @NotBlank
    @Size(min = 2, max = 120)
    @Column(nullable = false, length = 120)
    private String companyName;

    @NotBlank
    @Pattern(regexp = "\\d{9}|\\d{13}", message = "EIK must contain 9 or 13 digits")
    @Column(nullable = false, length = 13, unique = true)
    private String eik;

    @NotBlank
    @Size(min = 2, max = 60)
    @Column(nullable = false, length = 60)
    private String representativeFirstName;

    @NotBlank
    @Size(min = 2, max = 60)
    @Column(nullable = false, length = 60)
    private String representativeLastName;

    @Override
    public String getDisplayName() {
        return companyName;
    }

    @Override
    public String getIdentifier() {
        return eik;
    }
}
