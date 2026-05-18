package org.informatics.bank_system.config;

import lombok.RequiredArgsConstructor;
import org.informatics.bank_system.data.entity.CreditProduct;
import org.informatics.bank_system.data.entity.CreditProductCode;
import org.informatics.bank_system.data.entity.Role;
import org.informatics.bank_system.data.entity.User;
import org.informatics.bank_system.data.repository.CreditProductRepository;
import org.informatics.bank_system.data.repository.RoleRepository;
import org.informatics.bank_system.data.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final CreditProductRepository creditProductRepository;
    private final RoleRepository roleRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public void run(String... args) {
        Role adminRole = createRoleIfMissing("ADMIN");
        createRoleIfMissing("CLIENT");
        createAdminUserIfMissing("admin", "admin", adminRole);

        createCreditProductIfMissing(
                CreditProductCode.CONSUMER,
                "Consumer credit",
                new BigDecimal("7.50"),
                new BigDecimal("50000.00"),
                120
        );

        createCreditProductIfMissing(
                CreditProductCode.MORTGAGE,
                "Mortgage credit",
                new BigDecimal("4.20"),
                new BigDecimal("500000.00"),
                360
        );
    }

    private Role createRoleIfMissing(String authority) {
        return roleRepository.findByAuthority(authority)
                .orElseGet(() -> {
                    Role role = new Role();
                    role.setAuthority(authority);
                    return roleRepository.save(role);
                });
    }

    private void createAdminUserIfMissing(String username, String rawPassword, Role role) {
        if (userRepository.existsByUsername(username)) {
            return;
        }

        User user = new User();
        user.setUsername(username);
        user.setPassword(passwordEncoder.encode(rawPassword));
        user.setAccountNonExpired(true);
        user.setAccountNonLocked(true);
        user.setCredentialsNonExpired(true);
        user.setEnabled(true);
        user = userRepository.save(user);

        // Role owns the many-to-many relation, so the link is saved from the role side.
        role.getUsers().add(user);
        roleRepository.save(role);
    }

    private void createCreditProductIfMissing(
            CreditProductCode code,
            String name,
            BigDecimal annualInterestRate,
            BigDecimal maxAmount,
            Integer maxTermMonths
    ) {
        if (creditProductRepository.findByCode(code).isPresent()) {
            return;
        }

        CreditProduct product = new CreditProduct();
        product.setCode(code);
        product.setName(name);
        product.setAnnualInterestRate(annualInterestRate);
        product.setMaxAmount(maxAmount);
        product.setMaxTermMonths(maxTermMonths);
        creditProductRepository.save(product);
    }
}
