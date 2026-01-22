package com.studenttracker.model;

import java.time.LocalDateTime;

public class MonthlyReport {
    private Integer reportId;
    private String reportMonth;
    private String reportData;
    private LocalDateTime generatedAt;
    private Integer generatedBy;

    public MonthlyReport() {
    }

    public MonthlyReport(String reportMonth, String reportData, 
                         LocalDateTime generatedAt, Integer generatedBy) {
        this.reportMonth = reportMonth;
        this.reportData = reportData;
        this.generatedAt = generatedAt;
        this.generatedBy = generatedBy;
    }

    public Integer getReportId() {
        return reportId;
    }

    public void setReportId(Integer reportId) {
        this.reportId = reportId;
    }

    public String getReportMonth() {
        return reportMonth;
    }

    public void setReportMonth(String reportMonth) {
        this.reportMonth = reportMonth;
    }

    public String getReportData() {
        return reportData;
    }

    public void setReportData(String reportData) {
        this.reportData = reportData;
    }

    public LocalDateTime getGeneratedAt() {
        return generatedAt;
    }

    public void setGeneratedAt(LocalDateTime generatedAt) {
        this.generatedAt = generatedAt;
    }

    public Integer getGeneratedBy() {
        return generatedBy;
    }

    public void setGeneratedBy(Integer generatedBy) {
        this.generatedBy = generatedBy;
    }

    @Override
    public String toString() {
        return "MonthlyReport{" +
                "reportId=" + reportId +
                ", reportMonth='" + reportMonth + '\'' +
                ", reportData='" + reportData + '\'' +
                ", generatedAt=" + generatedAt +
                ", generatedBy=" + generatedBy +
                '}';
    }
}