package org.informatics.bank_system.service.impl;

import lombok.RequiredArgsConstructor;
import org.informatics.bank_system.data.entity.Client;
import org.informatics.bank_system.data.entity.Role;
import org.informatics.bank_system.data.entity.User;
import org.informatics.bank_system.data.repository.RoleRepository;
import org.informatics.bank_system.data.repository.UserRepository;
import org.informatics.bank_system.dto.CreateUserDto;
import org.informatics.bank_system.dto.SessionDto;
import org.informatics.bank_system.dto.UserDto;
import org.informatics.bank_system.exception.BusinessRuleException;
import org.informatics.bank_system.exception.ObjectNotFoundException;
import org.informatics.bank_system.service.ClientService;
import org.informatics.bank_system.service.UserService;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    public static final String ROLE_ADMIN = "ADMIN";
    public static final String ROLE_CLIENT = "CLIENT";

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final ClientService clientService;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return userRepository.findByUsernameWithClient(username)
                .orElseThrow(() -> new UsernameNotFoundException("User " + username + " was not found."));
    }

    @Override
    @Transactional
    public UserDto createClientUser(CreateUserDto dto) {
        String username = dto.getUsername().trim();
        if (userRepository.existsByUsername(username)) {
            throw new BusinessRuleException("User with this username already exists.");
        }

        if (userRepository.existsByClientId(dto.getClientId())) {
            throw new BusinessRuleException("Client already has a user account.");
        }

        Client client = clientService.getClientEntity(dto.getClientId());
        Role clientRole = roleRepository.findByAuthority(ROLE_CLIENT)
                .orElseThrow(() -> new ObjectNotFoundException("Role " + ROLE_CLIENT + " was not found."));

        User user = new User();
        user.setUsername(username);
        user.setPassword(passwordEncoder.encode(dto.getPassword()));
        user.setAccountNonExpired(true);
        user.setAccountNonLocked(true);
        user.setCredentialsNonExpired(true);
        user.setEnabled(true);
        user.setClient(client);
        user = userRepository.save(user);

        // Role owns the many-to-many relation, so the link is saved from the role side.
        clientRole.getUsers().add(user);
        roleRepository.save(clientRole);
        user.getAuthorities().add(clientRole);

        return mapUser(user);
    }

    @Override
    public SessionDto getCurrentSession() {
        User user = getCurrentUser();
        return SessionDto.builder()
                .username(user.getUsername())
                .role(user.getAuthorities().stream()
                        .map(GrantedAuthority::getAuthority)
                        .findFirst()
                        .orElse(ROLE_CLIENT))
                .clientId(user.getClient() == null ? null : user.getClient().getId())
                .displayName(user.getClient() == null ? "Administrator" : user.getClient().getDisplayName())
                .build();
    }

    @Override
    public User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof User user)) {
            throw new ObjectNotFoundException("No authenticated bank user in the current session.");
        }
        return user;
    }

    private UserDto mapUser(User user) {
        return UserDto.builder()
                .id(user.getId())
                .username(user.getUsername())
                .role(ROLE_CLIENT)
                .clientId(user.getClient().getId())
                .clientDisplayName(user.getClient().getDisplayName())
                .build();
    }
}
