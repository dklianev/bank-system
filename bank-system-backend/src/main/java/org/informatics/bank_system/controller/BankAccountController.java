package org.informatics.bank_system.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.informatics.bank_system.dto.BankAccountDto;
import org.informatics.bank_system.dto.CreateBankAccountDto;
import org.informatics.bank_system.service.BankAccountService;
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
@RequestMapping("/api/accounts")
public class BankAccountController {

    private final BankAccountService bankAccountService;

    @GetMapping
    public List<BankAccountDto> getAccounts() {
        return bankAccountService.getAccounts();
    }

    @PostMapping
    public BankAccountDto createAccount(@Valid @RequestBody CreateBankAccountDto dto) {
        return bankAccountService.createAccount(dto);
    }

    @PatchMapping("/{id}/close")
    public BankAccountDto closeAccount(@PathVariable Long id) {
        return bankAccountService.closeAccount(id);
    }
}
