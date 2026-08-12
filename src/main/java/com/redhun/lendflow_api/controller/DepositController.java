package com.redhun.lendflow_api.controller;
import com.redhun.lendflow_api.dto.desposit.AddDepositMoneyRequest;
import com.redhun.lendflow_api.dto.desposit.CreateDepositRequest;
import com.redhun.lendflow_api.dto.desposit.DepositResponse;
import com.redhun.lendflow_api.service.DepositService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/deposits")
@RequiredArgsConstructor
public class DepositController {

    private final DepositService depositService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public DepositResponse createDeposit(
            @Valid @RequestBody CreateDepositRequest request
    ) {
        return depositService.createDeposit(request);
    }

    @GetMapping("/{id}")
    public DepositResponse getDeposit(
            @PathVariable Long id
    ) {
        return depositService.getDeposit(id);
    }

    @GetMapping("/user/{userId}")
    public List<DepositResponse> getUserDeposits(
            @PathVariable Long userId
    ) {
        return depositService.getUserDeposits(userId);
    }

    @PostMapping("/{id}/add-money")
    public DepositResponse addMoney(
            @PathVariable Long id,
            @Valid @RequestBody AddDepositMoneyRequest request
    ) {
        return depositService.addMoney(id, request);
    }

    @PostMapping("/{id}/close")
    public DepositResponse closeDeposit(
            @PathVariable Long id
    ) {
        return depositService.closeDeposit(id);
    }
}