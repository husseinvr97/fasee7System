// ========== MonthlyReportGeneratedEvent.java ==========
package com.studenttracker.service.event;

import java.time.LocalDateTime;

public class MonthlyReportGeneratedEvent implements Event {
    private final Integer reportId;
    private final String monthGroup;
    private final Integer generatedBy;
    private final LocalDateTime timestamp;

    public MonthlyReportGeneratedEvent(Integer reportId, String monthGroup, Integer generatedBy) {
        this.reportId = reportId;
        this.monthGroup = monthGroup;
        this.generatedBy = generatedBy;
        this.timestamp = LocalDateTime.now();
    }

    public Integer getReportId() { return reportId; }
    public String getMonthGroup() { return monthGroup; }
    public Integer getGeneratedBy() { return generatedBy; }
    public LocalDateTime getTimestamp() { return timestamp; }

    @Override
    public String toString() {
        return "MonthlyReportGeneratedEvent{" +
                "reportId=" + reportId +
                ", monthGroup='" + monthGroup + '\'' +
                ", generatedBy=" + generatedBy +
                ", timestamp=" + timestamp +
                '}';
    }
}