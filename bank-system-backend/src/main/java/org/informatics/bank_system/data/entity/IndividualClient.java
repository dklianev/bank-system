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
        name = "individual_clients",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_individual_clients_egn", columnNames = "egn")
        }
)
@DiscriminatorValue("INDIVIDUAL")
@Getter
@Setter
@NoArgsConstructor
public class IndividualClient extends Client {

    @NotBlank
    @Size(min = 2, max = 60)
    @Column(nullable = false, length = 60)
    private String firstName;

    @NotBlank
    @Size(min = 2, max = 60)
    @Column(nullable = false, length = 60)
    private String lastName;

    @NotBlank
    @Pattern(regexp = "\\d{10}", message = "EGN must contain exactly 10 digits")
    @Column(nullable = false, length = 10, unique = true)
    private String egn;

    @Override
    public String getDisplayName() {
        return firstName + " " + lastName;
    }

    @Override
    public String getIdentifier() {
        return egn;
    }
}
