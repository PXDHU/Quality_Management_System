package com.example.QualityManagementSystem.service;

import com.example.QualityManagementSystem.dto.ReportFilterRequest;
import com.example.QualityManagementSystem.model.Audit;
import com.example.QualityManagementSystem.model.CorrectiveAction;
import com.example.QualityManagementSystem.model.NonConformity;
import com.example.QualityManagementSystem.repository.AuditRepository;
import com.example.QualityManagementSystem.repository.NonConformityRepository;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Objects;
import java.util.Set;

/**
 * Service for generating PDF reports for audits and non-conformities
 * Provides functionality to generate audit reports and filtered reports
 */
@Service
@Transactional(readOnly = true)
public class ReportService {

	private final AuditRepository auditRepository;
	private final NonConformityRepository ncRepository;

	public ReportService(AuditRepository auditRepository, NonConformityRepository ncRepository) {
		this.auditRepository = auditRepository;
		this.ncRepository = ncRepository;
	}

	/**
	 * Generates a PDF report for a specific audit
	 * @param auditId the ID of the audit to generate report for
	 * @return byte array containing the PDF data
	 * @throws RuntimeException if audit not found or PDF generation fails
	 */
	public byte[] generateAuditReportPdf(Long auditId) {
		if (auditId == null) {
			throw new IllegalArgumentException("Audit ID cannot be null");
		}
		
		Audit audit = auditRepository.findById(auditId)
			.orElseThrow(() -> new RuntimeException("Audit not found with ID: " + auditId));
		List<NonConformity> ncs = ncRepository.findByAudit_AuditId(auditId);

		try (PDDocument doc = new PDDocument(); ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
			PDPage page = new PDPage(PDRectangle.A4);
			doc.addPage(page);
			PDPageContentStream cs = new PDPageContentStream(doc, page);

			float margin = 50;
			float y = page.getMediaBox().getHeight() - margin;
			float leading = 14f;

			cs.setFont(PDType1Font.HELVETICA_BOLD, 16);
			y = writeLine(cs, "Audit Report", margin, y, leading);
			cs.setFont(PDType1Font.HELVETICA, 12);

			DateTimeFormatter df = DateTimeFormatter.ofPattern("yyyy-MM-dd");
			y = writeLine(cs, "Title: " + audit.getTitle(), margin, y, leading);
			y = writeMultiLine(cs, "Scope: " + safe(audit.getScope()), margin, y, leading, page);
			y = writeMultiLine(cs, "Objectives: " + safe(audit.getObjectives()), margin, y, leading, page);
			y = writeLine(cs, "Department: " + safe(audit.getDepartment()) + " | Location: " + safe(audit.getLocation()), margin, y, leading);
			y = writeLine(cs, "Start: " + (audit.getStartDate() != null ? audit.getStartDate().format(df) : "-") + " | End: " + (audit.getEndDate() != null ? audit.getEndDate().format(df) : "-"), margin, y, leading);
			y = writeLine(cs, "Status: " + (audit.getStatus() != null ? audit.getStatus().name() : "-"), margin, y, leading);
			y = writeMultiLine(cs, "Reviewer Comments: " + safe(audit.getNotes()), margin, y, leading, page);

			y = writeLine(cs, "", margin, y, leading);
			cs.setFont(PDType1Font.HELVETICA_BOLD, 14);
			y = writeLine(cs, "Findings (NCs)", margin, y, leading);
			cs.setFont(PDType1Font.HELVETICA, 12);

			if (ncs.isEmpty()) {
				y = writeLine(cs, "No non-conformities recorded.", margin, y, leading);
			} else {
				for (NonConformity nc : ncs) {
					y = writeLine(cs, "- NC #" + nc.getNonConformityId() + " | Severity: " + (nc.getSeverity() != null ? nc.getSeverity().name() : "-") + " | Status: " + (nc.getStatus() != null ? nc.getStatus().name() : "-"), margin, y, leading);
					y = writeMultiLine(cs, "  Title: " + safe(nc.getTitle()), margin, y, leading, page);
					y = writeMultiLine(cs, "  Description: " + safe(nc.getDescription()), margin, y, leading, page);
					Set<CorrectiveAction> actions = nc.getActions();
					if (actions != null && !actions.isEmpty()) {
						y = writeLine(cs, "  Actions:", margin, y, leading);
						for (CorrectiveAction a : actions) {
							String line = String.format("    - [%s] %s (Due: %s)", a.getStatus() != null ? a.getStatus().name() : "-", safe(a.getDescription()), a.getDueDate() != null ? a.getDueDate().toString() : "-");
							y = writeMultiLine(cs, line, margin, y, leading, page);
						}
					}
				}
			}

			cs.close();
			doc.save(baos);
			return baos.toByteArray();
		} catch (IOException e) {
			throw new RuntimeException("Failed to generate PDF for audit ID " + auditId + ": " + e.getMessage(), e);
		}
	}

	/**
	 * Generates a filtered PDF report based on the provided criteria
	 * @param filter the filter criteria for the report
	 * @return byte array containing the PDF data
	 * @throws RuntimeException if PDF generation fails
	 */
	public byte[] generateFilteredReportPdf(ReportFilterRequest filter) {
		if (filter == null) {
			throw new IllegalArgumentException("Filter cannot be null");
		}
		
		java.util.List<Audit> audits;
		if (filter.getAuditorId() != null) {
			audits = auditRepository.findByAuditor(filter.getAuditorId());
		} else {
			audits = auditRepository.searchByDepartmentAndStartDate(
					filter.getDepartment(), filter.getStartDateFrom(), filter.getStartDateTo());
		}

		if (filter.getClauseId() != null) {
			java.util.List<NonConformity> byClause = ncRepository.searchForReports(
					filter.getClauseId(), filter.getDepartment(), filter.getStartDateFrom(), filter.getStartDateTo());
			java.util.Set<Long> auditIds = byClause.stream().map(n -> n.getAudit().getAuditId()).collect(java.util.stream.Collectors.toSet());
			audits = audits.stream().filter(a -> auditIds.contains(a.getAuditId())).collect(java.util.stream.Collectors.toList());
		}

		try (PDDocument doc = new PDDocument(); ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
			DateTimeFormatter df = DateTimeFormatter.ofPattern("yyyy-MM-dd");
			for (Audit audit : audits) {
				PDPage page = new PDPage(PDRectangle.A4);
				doc.addPage(page);
				PDPageContentStream cs = new PDPageContentStream(doc, page);
				float margin = 50;
				float y = page.getMediaBox().getHeight() - margin;
				float leading = 14f;

				cs.setFont(PDType1Font.HELVETICA_BOLD, 15);
				y = writeLine(cs, "Audit: " + audit.getTitle() + " (" + audit.getAuditId() + ")", margin, y, leading);
				cs.setFont(PDType1Font.HELVETICA, 12);
				y = writeLine(cs, "Department: " + safe(audit.getDepartment()) + " | Location: " + safe(audit.getLocation()), margin, y, leading);
				y = writeLine(cs, "Start: " + (audit.getStartDate() != null ? audit.getStartDate().format(df) : "-") + " | End: " + (audit.getEndDate() != null ? audit.getEndDate().format(df) : "-"), margin, y, leading);
				y = writeLine(cs, "Status: " + (audit.getStatus() != null ? audit.getStatus().name() : "-"), margin, y, leading);


				java.util.List<NonConformity> ncs = ncRepository.findByAudit_AuditId(audit.getAuditId());
				if (filter.getClauseId() != null) {
					ncs = ncs.stream()
							.filter(n -> n.getClause() != null && Objects.equals(n.getClause().getClauseId(), filter.getClauseId()))
							.collect(java.util.stream.Collectors.toList());
				}

				y = writeLine(cs, "Findings:", margin, y, leading);
				if (ncs.isEmpty()) {
					y = writeLine(cs, "  - None", margin, y, leading);
				} else {
					for (NonConformity nc : ncs) {
						if (nc.getAudit().getAuditId() != audit.getAuditId()) continue;
						y = writeLine(cs, "  - NC #" + nc.getNonConformityId() + " | Clause: " + (nc.getClause() != null ? nc.getClause().getClauseNumber() : "-") + " | Severity: " + (nc.getSeverity() != null ? nc.getSeverity().name() : "-"), margin, y, leading);
						y = writeMultiLine(cs, "    Title: " + safe(nc.getTitle()), margin, y, leading, page);
						java.util.Set<CorrectiveAction> actions = nc.getActions();
						if (actions != null && !actions.isEmpty()) {
							for (CorrectiveAction a : actions) {
								String line = String.format("      - [%s] %s (Due: %s)", a.getStatus() != null ? a.getStatus().name() : "-", safe(a.getDescription()), a.getDueDate() != null ? a.getDueDate().toString() : "-");
								y = writeMultiLine(cs, line, margin, y, leading, page);
							}
						}
					}
				}

				cs.close();
			}

			doc.save(baos);
			return baos.toByteArray();
		} catch (IOException e) {
			throw new RuntimeException("Failed to generate filtered PDF: " + e.getMessage(), e);
		}
	}

	/**
	 * Safely converts a string to a display value, handling null cases
	 * @param s the string to convert
	 * @return the string or "-" if null
	 */
	private static String safe(String s) {
		return s == null ? "-" : s;
	}

	/**
	 * Writes a single line of text to the PDF
	 * @param cs the content stream
	 * @param text the text to write
	 * @param x the x coordinate
	 * @param y the y coordinate
	 * @param leading the line spacing
	 * @return the new y coordinate after writing the line
	 * @throws IOException if writing fails
	 */
	private float writeLine(PDPageContentStream cs, String text, float x, float y, float leading) throws IOException {
		if (y < 60) {
			// simplistic page overflow handling: new page not implemented fully to keep example compact
			return y; 
		}
		cs.beginText();
		cs.newLineAtOffset(x, y);
		cs.showText(text != null ? text : "");
		cs.endText();
		return y - leading;
	}

	/**
	 * Writes multiple lines of text to the PDF, wrapping as needed
	 * @param cs the content stream
	 * @param text the text to write
	 * @param x the x coordinate
	 * @param y the y coordinate
	 * @param leading the line spacing
	 * @param page the page for width calculations
	 * @return the new y coordinate after writing the text
	 * @throws IOException if writing fails
	 */
	private float writeMultiLine(PDPageContentStream cs, String text, float x, float y, float leading, PDPage page) throws IOException {
		if (text == null || text.trim().isEmpty()) {
			return y;
		}
		
		float width = page.getMediaBox().getWidth() - 2 * x;
		int maxChars = (int) (width / 6.5); // rough estimate for Helvetica 12
		String remaining = text;
		while (remaining.length() > 0 && y > 60) {
			int split = Math.min(maxChars, remaining.length());
			String line = remaining.substring(0, split);
			remaining = remaining.substring(split);
			y = writeLine(cs, line, x, y, leading);
		}
		return y;
	}
}


