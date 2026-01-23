package com.studenttracker.service.event;

public class AttendanceBatchCompletedEvent implements Event {
    private final Integer lessonId;
    private final Integer totalStudents;
    private final Integer presentCount;
    private final Integer absentCount;
    private final Integer completedBy;
    
    public AttendanceBatchCompletedEvent(Integer lessonId, Integer totalStudents,
                                        Integer presentCount, Integer absentCount,
                                        Integer completedBy) {
        this.lessonId = lessonId;
        this.totalStudents = totalStudents;
        this.presentCount = presentCount;
        this.absentCount = absentCount;
        this.completedBy = completedBy;
    }
    
    public Integer getLessonId() { return lessonId; }
    public Integer getTotalStudents() { return totalStudents; }
    public Integer getPresentCount() { return presentCount; }
    public Integer getAbsentCount() { return absentCount; }
    public Integer getCompletedBy() { return completedBy; }
    
    @Override
    public String toString() {
        return "AttendanceBatchCompletedEvent{lesson=" + lessonId + 
               ", total=" + totalStudents + ", present=" + presentCount + 
               ", absent=" + absentCount + "}";
    }
}