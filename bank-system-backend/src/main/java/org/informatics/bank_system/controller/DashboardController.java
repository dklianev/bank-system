package org.informatics.bank_system.controller;

import lombok.RequiredArgsConstructor;
import org.informatics.bank_system.dto.DashboardDto;
import org.informatics.bank_system.service.DashboardService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/dashboard")
public class DashboardController {

    private final DashboardService dashboardService;

    @GetMapping
    public DashboardDto getDashboard() {
        return dashboardService.getDashboard();
    }
}
