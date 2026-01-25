package com.studenttracker.service;

import com.studenttracker.model.Fasee7Points;
import com.studenttracker.model.Fasee7Snapshot;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * Service interface for managing Fasee7 points and rankings.
 * Handles point calculations, rankings, and historical snapshots.
 */
public interface Fasee7TableService {
    
    // ========== Points Calculation ==========
    
    /**
     * Recalculates all points for a student from scratch.
     * Queries all DAOs and sums up quiz, attendance, homework, and target points.
     * 
     * @param studentId The student ID
     */
    void recalculatePoints(Integer studentId);
    
    /**
     * Updates quiz points only for a student.
     * Called by event subscriber after quiz grading.
     * 
     * @param studentId The student ID
     */
    void updateQuizPoints(Integer studentId);
    
    /**
     * Updates attendance points only for a student.
     * Called by event subscriber after attendance update.
     * 
     * @param studentId The student ID
     */
    void updateAttendancePoints(Integer studentId);
    
    /**
     * Updates homework points only for a student.
     * Called by event subscriber after homework update.
     * 
     * @param studentId The student ID
     */
    void updateHomeworkPoints(Integer studentId);
    
    /**
     * Updates target points only for a student.
     * Called by event subscriber after target achievement.
     * 
     * @param studentId The student ID
     */
    void updateTargetPoints(Integer studentId);
    
    
    // ========== Rankings ==========
    
    /**
     * Gets all rankings with tie-breaking applied.
     * Only includes active students.
     * 
     * @return Ordered list of Fasee7Points with ranks
     */
    List<Fasee7Points> getRankings();
    
    /**
     * Gets the rank of a specific student.
     * 
     * @param studentId The student ID
     * @return Rank (1-based), or -1 if not found
     */
    int getStudentRank(Integer studentId);
    
    /**
     * Gets top N students by points.
     * 
     * @param limit Number of students to return
     * @return Top N students
     */
    List<Fasee7Points> getTopN(int limit);
    
    /**
     * Gets points record for a specific student.
     * 
     * @param studentId The student ID
     * @return Fasee7Points object, or null if not found
     */
    Fasee7Points getStudentPoints(Integer studentId);
    
    
    // ========== Snapshots ==========
    
    /**
     * Creates a snapshot of current rankings.
     * 
     * @param snapshotDate The date of the snapshot
     * @param createdBy User ID who created the snapshot
     * @return Snapshot ID
     */
    Integer createSnapshot(LocalDate snapshotDate, Integer createdBy);
    
    /**
     * Gets a snapshot by date.
     * 
     * @param date The snapshot date
     * @return Fasee7Snapshot, or null if not found
     */
    Fasee7Snapshot getSnapshot(LocalDate date);
    
    /**
     * Gets the most recent snapshot.
     * 
     * @return Latest Fasee7Snapshot, or null if none exist
     */
    Fasee7Snapshot getLatestSnapshot();
    
    /**
     * Gets all snapshots ordered by date.
     * 
     * @return List of all snapshots
     */
    List<Fasee7Snapshot> getAllSnapshots();
    
    
    // ========== Comparison ==========
    
    /**
     * Compares rankings between two dates.
     * 
     * @param date1 First date
     * @param date2 Second date
     * @return Map of studentId → rank change (positive = improved)
     */
    Map<Integer, Integer> compareRankings(LocalDate date1, LocalDate date2);
    
    
    // ========== Statistics ==========
    
    /**
     * Gets average points across all active students.
     * 
     * @return Average points
     */
    Double getAveragePoints();
    
    /**
     * Gets the highest total points.
     * 
     * @return Highest points
     */
    Double getHighestPoints();
    
    
    // ========== Initialization ==========
    
    /**
     * Initializes points for a new student.
     * Creates record with all zeros.
     * 
     * @param studentId The student ID
     */
    void initializePoints(Integer studentId);
}