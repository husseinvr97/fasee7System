// ========== ReportData.java ==========
package com.studenttracker.model;

public class ReportData {
    private String monthGroup;
    private OverviewData overview;
    private Fasee7SnapshotData fasee7Snapshot;
    private PerformanceSummary performanceSummary;
    private TargetSummary targetSummary;
    private AttendanceBehavioralSummary attendanceBehavioral;
    private HomeworkSummary homeworkSummary;

    public ReportData() {}

    public ReportData(String monthGroup, OverviewData overview, 
                     Fasee7SnapshotData fasee7Snapshot, PerformanceSummary performanceSummary,
                     TargetSummary targetSummary, AttendanceBehavioralSummary attendanceBehavioral,
                     HomeworkSummary homeworkSummary) {
        this.monthGroup = monthGroup;
        this.overview = overview;
        this.fasee7Snapshot = fasee7Snapshot;
        this.performanceSummary = performanceSummary;
        this.targetSummary = targetSummary;
        this.attendanceBehavioral = attendanceBehavioral;
        this.homeworkSummary = homeworkSummary;
    }

    public String getMonthGroup() { return monthGroup; }
    public void setMonthGroup(String monthGroup) { this.monthGroup = monthGroup; }

    public OverviewData getOverview() { return overview; }
    public void setOverview(OverviewData overview) { this.overview = overview; }

    public Fasee7SnapshotData getFasee7Snapshot() { return fasee7Snapshot; }
    public void setFasee7Snapshot(Fasee7SnapshotData fasee7Snapshot) { this.fasee7Snapshot = fasee7Snapshot; }

    public PerformanceSummary getPerformanceSummary() { return performanceSummary; }
    public void setPerformanceSummary(PerformanceSummary performanceSummary) { this.performanceSummary = performanceSummary; }

    public TargetSummary getTargetSummary() { return targetSummary; }
    public void setTargetSummary(TargetSummary targetSummary) { this.targetSummary = targetSummary; }

    public AttendanceBehavioralSummary getAttendanceBehavioral() { return attendanceBehavioral; }
    public void setAttendanceBehavioral(AttendanceBehavioralSummary attendanceBehavioral) { this.attendanceBehavioral = attendanceBehavioral; }

    public HomeworkSummary getHomeworkSummary() { return homeworkSummary; }
    public void setHomeworkSummary(HomeworkSummary homeworkSummary) { this.homeworkSummary = homeworkSummary; }
}
