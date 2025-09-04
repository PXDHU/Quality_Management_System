package com.example.QualityManagementSystem.controller;

import com.example.QualityManagementSystem.dto.DashboardMetrics;
import com.example.QualityManagementSystem.service.DashboardService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/dashboard")
@CrossOrigin(origins = "*")
public class DashboardController {

	private final DashboardService dashboardService;

	public DashboardController(DashboardService dashboardService) {
		this.dashboardService = dashboardService;
	}

	// Admin overview metrics
	@PreAuthorize("hasRole('ADMIN')")
	@GetMapping("/admin")
	public ResponseEntity<DashboardMetrics> getAdminMetrics(@RequestParam(name = "months", defaultValue = "12") int months) {
		return ResponseEntity.ok(dashboardService.getAdminMetrics(months));
	}

	// Charts: NC by severity
	@PreAuthorize("hasAnyRole('ADMIN','AUDITOR','REVIEWER')")
	@GetMapping("/charts/nc-severity")
	public ResponseEntity<Map<String, Long>> getNcBySeverity() {
		return ResponseEntity.ok(dashboardService.getNcBySeverity());
	}

	// Charts: Audits by status (as completion proxy)
	@PreAuthorize("hasAnyRole('ADMIN','AUDITOR','REVIEWER')")
	@GetMapping("/charts/audit-status")
	public ResponseEntity<Map<String, Long>> getAuditsByStatus() {
		return ResponseEntity.ok(dashboardService.getAuditByStatus());
	}

	// Trends: NC monthly trend
	@PreAuthorize("hasAnyRole('ADMIN','AUDITOR','REVIEWER')")
	@GetMapping("/trends/nc-monthly")
	public ResponseEntity<List<DashboardMetrics.TrendPoint>> getNcMonthlyTrend(@RequestParam(name = "months", defaultValue = "12") int months) {
		return ResponseEntity.ok(dashboardService.getNcMonthlyTrend(months));
	}
}



