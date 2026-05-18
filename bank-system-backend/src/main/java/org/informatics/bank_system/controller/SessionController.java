package org.informatics.bank_system.controller;

import lombok.RequiredArgsConstructor;
import org.informatics.bank_system.dto.SessionDto;
import org.informatics.bank_system.service.UserService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/session")
public class SessionController {

    private final UserService userService;

    @GetMapping
    public SessionDto getSession() {
        return userService.getCurrentSession();
    }
}
