package com.studenttracker.service;

/**
 * Service interface for tracking consecutive student absences.
 * 
 * TODO: Implement when consecutivity module is designed.
 * For now, this is a placeholder interface.
 */
public interface ConsecutivityTrackingService {
    
    /**
     * Resets consecutivity tracking for a restored student.
     * When a student is restored from archived status, their consecutive absence
     * count should be reset to zero.
     * 
     * @param studentId The ID of the student whose consecutivity should be reset
     */
    void resetConsecutivity(Integer studentId);
    
    // TODO: Add more methods when consecutivity tracking is fully designed
    // Examples:
    // - void recordAbsence(Integer studentId, LocalDate date)
    // - int getConsecutiveAbsenceCount(Integer studentId)
    // - boolean shouldArchiveStudent(Integer studentId)
}