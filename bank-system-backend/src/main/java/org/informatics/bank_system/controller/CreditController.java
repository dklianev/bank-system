package org.informatics.bank_system.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.informatics.bank_system.dto.CreateCreditDto;
import org.informatics.bank_system.dto.CreditDto;
import org.informatics.bank_system.dto.CreditProductDto;
import org.informatics.bank_system.dto.RepaymentInstallmentDto;
import org.informatics.bank_system.service.CreditService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/credits")
public class CreditController {

    private final CreditService creditService;

    @GetMapping("/products")
    public List<CreditProductDto> getCreditProducts() {
        return creditService.getCreditProducts();
    }

    @GetMapping
    public List<CreditDto> getCredits() {
        return creditService.getCredits();
    }

    @PostMapping
    public CreditDto createCredit(@Valid @RequestBody CreateCreditDto dto) {
        return creditService.createCredit(dto);
    }

    @GetMapping("/{id}/installments")
    public List<RepaymentInstallmentDto> getInstallments(@PathVariable Long id) {
        return creditService.getInstallments(id);
    }

    @GetMapping("/{id}/status")
    public CreditDto getCreditStatus(@PathVariable Long id) {
        return creditService.getCreditStatus(id);
    }

    @PatchMapping("/installments/{installmentId}/pay")
    public RepaymentInstallmentDto payInstallment(@PathVariable Long installmentId) {
        return creditService.payInstallment(installmentId);
    }
}
