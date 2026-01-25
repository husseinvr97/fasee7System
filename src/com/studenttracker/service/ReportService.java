package com.studenttracker.service;

import com.studenttracker.model.MonthlyReport;
import java.util.List;

/**
 * Service interface for generating and managing monthly reports.
 * Reports aggregate data from all key metrics and serialize to JSON.
 */
public interface ReportService {
    
    /**
     * Generate a monthly report for the specified month.
     * Validates admin permission and checks for duplicates.
     * Collects data from all sources and saves as JSON.
     * 
     * @param monthGroup Month identifier (e.g., "Month 2")
     * @param generatedBy User ID who is generating the report (must be admin)
     * @return Generated report ID
     * @throws com.studenttracker.exception.UnauthorizedException if user is not admin
     * @throws com.studenttracker.exception.ServiceException if report already exists or generation fails
     */
    Integer generateMonthlyReport(String monthGroup, Integer generatedBy);
    
    /**
     * Retrieve a report by its ID.
     * 
     * @param reportId Report ID
     * @return MonthlyReport object, or null if not found
     */
    MonthlyReport getReportById(Integer reportId);
    
    /**
     * Retrieve a report by month group.
     * 
     * @param monthGroup Month identifier
     * @return MonthlyReport object, or null if not found
     */
    MonthlyReport getReportByMonth(String monthGroup);
    
    /**
     * Retrieve all reports ordered by month.
     * 
     * @return List of all reports
     */
    List<MonthlyReport> getAllReports();
    
    /**
     * Retrieve the most recent report.
     * 
     * @return Latest MonthlyReport, or null if no reports exist
     */
    MonthlyReport getLatestReport();
    
    /**
     * Export a report as JSON string.
     * 
     * @param reportId Report ID
     * @return JSON string of report data
     * @throws com.studenttracker.exception.ServiceException if report not found
     */
    String exportReportAsJson(Integer reportId);
    
    /**
     * Check if a report exists for a specific month.
     * 
     * @param monthGroup Month identifier
     * @return true if report exists, false otherwise
     */
    boolean reportExists(String monthGroup);
    
    /**
     * Delete a report by ID.
     * Validates admin permission.
     * 
     * @param reportId Report ID
     * @param deletedBy User ID who is deleting the report (must be admin)
     * @return true if deletion successful, false otherwise
     * @throws com.studenttracker.exception.UnauthorizedException if user is not admin
     */
    boolean deleteReport(Integer reportId, Integer deletedBy);
}