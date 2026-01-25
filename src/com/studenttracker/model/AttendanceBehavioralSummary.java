// ========== AttendanceBehavioralSummary.java ==========
package com.studenttracker.model;

import java.util.Map;

public class AttendanceBehavioralSummary {
    private int perfectAttendanceCount;
    private int oneToTwoAbsencesCount;
    private int threePlusAbsencesCount;
    private int archivedCount;
    private Map<String, Integer> behavioralIncidentsByType;

    public AttendanceBehavioralSummary() {}

    public AttendanceBehavioralSummary(int perfectAttendanceCount, int oneToTwoAbsencesCount,
                                      int threePlusAbsencesCount, int archivedCount,
                                      Map<String, Integer> behavioralIncidentsByType) {
        this.perfectAttendanceCount = perfectAttendanceCount;
        this.oneToTwoAbsencesCount = oneToTwoAbsencesCount;
        this.threePlusAbsencesCount = threePlusAbsencesCount;
        this.archivedCount = archivedCount;
        this.behavioralIncidentsByType = behavioralIncidentsByType;
    }

    public int getPerfectAttendanceCount() { return perfectAttendanceCount; }
    public void setPerfectAttendanceCount(int perfectAttendanceCount) { this.perfectAttendanceCount = perfectAttendanceCount; }

    public int getOneToTwoAbsencesCount() { return oneToTwoAbsencesCount; }
    public void setOneToTwoAbsencesCount(int oneToTwoAbsencesCount) { this.oneToTwoAbsencesCount = oneToTwoAbsencesCount; }

    public int getThreePlusAbsencesCount() { return threePlusAbsencesCount; }
    public void setThreePlusAbsencesCount(int threePlusAbsencesCount) { this.threePlusAbsencesCount = threePlusAbsencesCount; }

    public int getArchivedCount() { return archivedCount; }
    public void setArchivedCount(int archivedCount) { this.archivedCount = archivedCount; }

    public Map<String, Integer> getBehavioralIncidentsByType() { return behavioralIncidentsByType; }
    public void setBehavioralIncidentsByType(Map<String, Integer> behavioralIncidentsByType) { this.behavioralIncidentsByType = behavioralIncidentsByType; }
}