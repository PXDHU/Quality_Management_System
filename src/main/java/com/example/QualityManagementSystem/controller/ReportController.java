package com.example.QualityManagementSystem.controller;

import com.example.QualityManagementSystem.dto.ReportFilterRequest;
import com.example.QualityManagementSystem.service.ReportService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/reports")
@CrossOrigin(origins = "*")
public class ReportController {

	private final ReportService reportService;

	public ReportController(ReportService reportService) {
		this.reportService = reportService;
	}

	// Export PDF for a specific audit including scope, findings, NCs, actions, reviewer comments
	@PreAuthorize("hasAnyRole('ADMIN','AUDITOR','REVIEWER')")
	@GetMapping("/audits/{auditId}/pdf")
	public ResponseEntity<byte[]> exportAuditReport(@PathVariable Long auditId) {
		byte[] pdf = reportService.generateAuditReportPdf(auditId);
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_PDF);
		headers.set(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=Audit-" + auditId + "-Report.pdf");
		return ResponseEntity.ok().headers(headers).body(pdf);
	}

	// Export a filtered report PDF by auditor, department, period, or clause
	@PreAuthorize("hasAnyRole('ADMIN','AUDITOR','REVIEWER')")
	@PostMapping(value = "/filtered/pdf", consumes = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<byte[]> exportFilteredReport(@RequestBody ReportFilterRequest filter) {
		byte[] pdf = reportService.generateFilteredReportPdf(filter);
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_PDF);
		headers.set(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=Filtered-Report.pdf");
		return ResponseEntity.ok().headers(headers).body(pdf);
	}
}


