package org.informatics.bank_system.service;

import org.informatics.bank_system.data.entity.User;
import org.informatics.bank_system.dto.CreateUserDto;
import org.informatics.bank_system.dto.SessionDto;
import org.informatics.bank_system.dto.UserDto;
import org.springframework.security.core.userdetails.UserDetailsService;

public interface UserService extends UserDetailsService {

    UserDto createClientUser(CreateUserDto dto);

    SessionDto getCurrentSession();

    User getCurrentUser();
}
