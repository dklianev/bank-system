package org.informatics.bank_system.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.informatics.bank_system.dto.CreateUserDto;
import org.informatics.bank_system.dto.UserDto;
import org.informatics.bank_system.service.UserService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    @PostMapping
    public UserDto createClientUser(@Valid @RequestBody CreateUserDto dto) {
        return userService.createClientUser(dto);
    }
}
