package org.informatics.bank_system.data.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.HashSet;
import java.util.Set;

/*
 * The entity implements UserDetails directly; Lombok generates the getters for
 * "authorities" and the four boolean flags that satisfy the interface. A CLIENT
 * user is linked to exactly one bank client; ADMIN users have no client.
 */
@Entity
@Table(
        name = "users",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_users_username", columnNames = "username"),
                @UniqueConstraint(name = "uk_users_client_id", columnNames = "client_id")
        }
)
@Getter
@Setter
@NoArgsConstructor
public class User extends BaseEntity implements UserDetails {

    @NotBlank
    @Size(min = 3, max = 40)
    @Column(nullable = false, length = 40, unique = true)
    private String username;

    @NotBlank
    @Column(nullable = false)
    private String password;

    @ManyToMany(mappedBy = "users", fetch = FetchType.EAGER)
    private Set<Role> authorities = new HashSet<>();

    @Column(nullable = false)
    private boolean accountNonExpired;

    @Column(nullable = false)
    private boolean accountNonLocked;

    @Column(nullable = false)
    private boolean credentialsNonExpired;

    @Column(nullable = false)
    private boolean enabled;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "client_id")
    private Client client;

    public boolean hasAuthority(String authority) {
        return authorities != null && authorities.stream()
                .anyMatch(role -> authority.equals(role.getAuthority()));
    }
}
