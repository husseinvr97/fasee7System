// ========== MonthlyReportDeletedEvent.java ==========
package com.studenttracker.service.event;

import java.time.LocalDateTime;

public class MonthlyReportDeletedEvent implements Event {
    private final Integer reportId;
    private final String monthGroup;
    private final Integer deletedBy;
    private final LocalDateTime timestamp;

    public MonthlyReportDeletedEvent(Integer reportId, String monthGroup, Integer deletedBy) {
        this.reportId = reportId;
        this.monthGroup = monthGroup;
        this.deletedBy = deletedBy;
        this.timestamp = LocalDateTime.now();
    }

    public Integer getReportId() { return reportId; }
    public String getMonthGroup() { return monthGroup; }
    public Integer getDeletedBy() { return deletedBy; }
    public LocalDateTime getTimestamp() { return timestamp; }

    @Override
    public String toString() {
        return "MonthlyReportDeletedEvent{" +
                "reportId=" + reportId +
                ", monthGroup='" + monthGroup + '\'' +
                ", deletedBy=" + deletedBy +
                ", timestamp=" + timestamp +
                '}';
    }
}