package com.example.QualityManagementSystem.service;

import com.example.QualityManagementSystem.dto.DashboardMetrics;
import com.example.QualityManagementSystem.model.Audit;
import com.example.QualityManagementSystem.model.Checklist;
import com.example.QualityManagementSystem.model.Checklist_item;
import com.example.QualityManagementSystem.model.ConformityStatus;
import com.example.QualityManagementSystem.model.Status;
import com.example.QualityManagementSystem.repository.AuditRepository;
import com.example.QualityManagementSystem.repository.ChecklistItemRepository;
import com.example.QualityManagementSystem.repository.ChecklistRepository;
import com.example.QualityManagementSystem.repository.NonConformityRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class DashboardService {

	private final AuditRepository auditRepository;
	private final NonConformityRepository ncRepository;
	private final ChecklistRepository checklistRepository;
	private final ChecklistItemRepository checklistItemRepository;

	public DashboardService(
			AuditRepository auditRepository,
			NonConformityRepository ncRepository,
			ChecklistRepository checklistRepository,
			ChecklistItemRepository checklistItemRepository
	) {
		this.auditRepository = auditRepository;
		this.ncRepository = ncRepository;
		this.checklistRepository = checklistRepository;
		this.checklistItemRepository = checklistItemRepository;
	}

	public DashboardMetrics getAdminMetrics(int months) {
		DashboardMetrics metrics = new DashboardMetrics();

		// Audit counts
		metrics.totalAudits = auditRepository.count();
		Map<Status, Long> auditsByStatus = toEnumCountMapStatus(auditRepository.countGroupByStatus());
		metrics.auditsPlanned = auditsByStatus.getOrDefault(Status.PLANNED, 0L);
		metrics.auditsInProgress = auditsByStatus.getOrDefault(Status.IN_PROGRESS, 0L);
		metrics.auditsCompleted = auditsByStatus.getOrDefault(Status.COMPLETED, 0L);
		metrics.auditsClosed = auditsByStatus.getOrDefault(Status.CLOSED, 0L);

		// NC counts
		metrics.totalNCs = ncRepository.count();
		Map<Status, Long> ncsByStatus = toEnumCountMapStatus(ncRepository.countGroupByStatus());
		metrics.ncsPending = ncsByStatus.getOrDefault(Status.PENDING, 0L);
		metrics.ncsInProgress = ncsByStatus.getOrDefault(Status.IN_PROGRESS, 0L);
		metrics.ncsCompleted = ncsByStatus.getOrDefault(Status.COMPLETED, 0L);
		metrics.ncsClosed = ncsByStatus.getOrDefault(Status.CLOSED, 0L);

		// By department and severity
		metrics.auditsByDepartment = toStringCountMap(auditRepository.countGroupByDepartment());
		metrics.ncsByDepartment = toStringCountMap(ncRepository.countGroupByDepartment());
		metrics.ncsBySeverity = toStringCountMap(ncRepository.countGroupBySeverity());

		// Compliance overall and per department
		computeCompliance(metrics);

		// Monthly trend for NCs
		metrics.ncMonthlyTrend = buildNcTrend(months);

		return metrics;
	}

	public Map<String, Long> getNcBySeverity() {
		return toStringCountMap(ncRepository.countGroupBySeverity());
	}

	public Map<String, Long> getAuditByStatus() {
		return toStringCountMap(auditRepository.countGroupByStatus());
	}

	public List<DashboardMetrics.TrendPoint> getNcMonthlyTrend(int months) {
		return buildNcTrend(months);
	}

	private List<DashboardMetrics.TrendPoint> buildNcTrend(int months) {
		int normalizedMonths = Math.max(1, Math.min(months, 36));
		LocalDate fromDate = LocalDate.now().withDayOfMonth(1).minusMonths(normalizedMonths - 1L);
		List<Object[]> rows = ncRepository.countByMonthSince(fromDate.atStartOfDay());

		DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM");
		Map<String, Long> monthToCount = new LinkedHashMap<>();
		// Initialize all months to 0 to ensure continuity
		LocalDate cursor = fromDate;
		for (int i = 0; i < normalizedMonths; i++) {
			monthToCount.put(cursor.format(fmt), 0L);
			cursor = cursor.plusMonths(1);
		}

		for (Object[] r : rows) {
			Object monthObj = r[0];
			Long count = ((Number) r[1]).longValue();
			String key;
			if (monthObj instanceof java.time.LocalDateTime) {
				key = ((java.time.LocalDateTime) monthObj).format(fmt);
			} else if (monthObj instanceof java.sql.Timestamp) {
				key = ((java.sql.Timestamp) monthObj).toLocalDateTime().format(fmt);
			} else {
				key = monthObj.toString().substring(0, 7); // fallback "YYYY-MM"
			}
			monthToCount.put(key, count);
		}

		return monthToCount.entrySet().stream()
				.map(e -> new DashboardMetrics.TrendPoint(e.getKey(), e.getValue()))
				.collect(Collectors.toList());
	}

	private void computeCompliance(DashboardMetrics metrics) {
		List<Audit> audits = auditRepository.findAll();
		Map<String, long[]> deptToCounts = new HashMap<>(); // [conform, total]
		long globalConform = 0;
		long globalTotal = 0;

		for (Audit audit : audits) {
			String dept = audit.getDepartment() != null ? audit.getDepartment() : "UNKNOWN";
			List<Checklist> checklists = checklistRepository.findByAudit_AuditId(audit.getAuditId());
			for (Checklist cl : checklists) {
				List<Checklist_item> items = checklistItemRepository.findByChecklist(cl);
				long total = items.size();
				long conform = items.stream().filter(i -> i.getConformityStatus() == ConformityStatus.COMPLIANT).count();
				globalConform += conform;
				globalTotal += total;
				long[] arr = deptToCounts.computeIfAbsent(dept, k -> new long[]{0, 0});
				arr[0] += conform;
				arr[1] += total;
			}
		}

		metrics.complianceScoreOverall = globalTotal == 0 ? 0.0 : (globalConform * 100.0) / globalTotal;
		Map<String, Double> deptCompliance = new HashMap<>();
		for (Map.Entry<String, long[]> e : deptToCounts.entrySet()) {
			long conform = e.getValue()[0];
			long total = e.getValue()[1];
			deptCompliance.put(e.getKey(), total == 0 ? 0.0 : (conform * 100.0) / total);
		}
		metrics.complianceByDepartment = deptCompliance;
	}

	private Map<Status, Long> toEnumCountMapStatus(List<Object[]> rows) {
		Map<Status, Long> map = new EnumMap<>(Status.class);
		for (Object[] row : rows) {
			Status key = (Status) row[0];
			Long count = ((Number) row[1]).longValue();
			map.put(key, count);
		}
		return map;
	}

	private Map<String, Long> toStringCountMap(List<Object[]> rows) {
		Map<String, Long> map = new LinkedHashMap<>();
		for (Object[] row : rows) {
			String key = String.valueOf(row[0]);
			Long count = ((Number) row[1]).longValue();
			map.put(key, count);
		}
		return map;
	}
}



