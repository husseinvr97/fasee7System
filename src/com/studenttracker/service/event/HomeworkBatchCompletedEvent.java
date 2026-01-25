package com.studenttracker.service.event;

/**
 * Event published when a batch of homework records is completed.
 */
public class HomeworkBatchCompletedEvent implements Event {
    private final Integer lessonId;
    private final Integer totalStudents;
    private final Integer doneCount;
    private final Integer partialCount;
    private final Integer notDoneCount;
    private final Integer completedBy;

    public HomeworkBatchCompletedEvent(Integer lessonId, Integer totalStudents, Integer doneCount, Integer partialCount, Integer notDoneCount, Integer completedBy) {
        this.lessonId = lessonId;
        this.totalStudents = totalStudents;
        this.doneCount = doneCount;
        this.partialCount = partialCount;
        this.notDoneCount = notDoneCount;
        this.completedBy = completedBy;
    }
    
    // Getters
    public Integer getLessonId() { return lessonId; }
    public Integer getTotalStudents() { return totalStudents; }
    public Integer getDoneCount() { return doneCount; }
    public Integer getPartialCount() { return partialCount; }
    public Integer getNotDoneCount() { return notDoneCount; }
    public Integer getCompletedBy() { return completedBy; }
    
    @Override
    public String toString() {
        return "HomeworkBatchCompletedEvent{" +
                "lessonId=" + lessonId +
                ", totalStudents=" + totalStudents +
                ", doneCount=" + doneCount +
                ", partialCount=" + partialCount +
                ", notDoneCount=" + notDoneCount +
                ", completedBy=" + completedBy +
                '}';
    }
}