package com.redhun.lendflow_api.controller;
import com.redhun.lendflow_api.dto.dashboard.DashboardResponse;
import com.redhun.lendflow_api.service.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final DashboardService dashboardService;

    @GetMapping("/admin")
    public DashboardResponse getAdminDashboard() {

        return dashboardService.getAdminDashboard();
    }
}
