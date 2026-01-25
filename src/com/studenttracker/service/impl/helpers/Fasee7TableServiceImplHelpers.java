package com.studenttracker.service.impl.helpers;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.studenttracker.dao.*;
import com.studenttracker.model.*;

import java.lang.reflect.Type;
import java.util.*;

/**
 * Helper class for Fasee7TableServiceImpl.
 * Contains utility methods for point calculations and ranking logic.
 */
public class Fasee7TableServiceImplHelpers {
    
    private static final Gson gson = new Gson();
    
    private Fasee7TableServiceImplHelpers() {}
    
    
    // ========== Point Calculation Helpers ==========
    
    /**
     * Calculates total quiz points for a student.
     * 
     * @param studentId The student ID
     * @param quizScoreDAO The quiz score DAO
     * @return Sum of all quiz points earned
     */
    public static Double calculateQuizPoints(Integer studentId, QuizScoreDAO quizScoreDAO) {
        List<QuizScore> scores = quizScoreDAO.findByStudentId(studentId);
        
        Double total = 0.0;
        for (QuizScore score : scores) {
            if (score.getPointsEarned() != null) {
                total = total + score.getPointsEarned();
            }
        }
        
        return total;
    }
    
    /**
     * Calculates attendance points for a student.
     * Each PRESENT = 1 point.
     * 
     * @param studentId The student ID
     * @param attendanceDAO The attendance DAO
     * @return Total attendance points
     */
    public static int calculateAttendancePoints(Integer studentId, AttendanceDAO attendanceDAO) {
        List<Attendance> attendances = attendanceDAO.findByStudentId(studentId);
        
        int count = 0;
        for (Attendance attendance : attendances) {
            if (attendance.getStatus() == Attendance.AttendanceStatus.PRESENT) {
                count++;
            }
        }
        
        return count;
    }
    
    /**
     * Calculates homework points for a student.
     * DONE = 3 points, PARTIALLY_DONE = 1 point, NOT_DONE = 0 points.
     * 
     * @param studentId The student ID
     * @param homeworkDAO The homework DAO
     * @return Total homework points
     */
    public static int calculateHomeworkPoints(Integer studentId, HomeworkDAO homeworkDAO) {
        List<Homework> homeworks = homeworkDAO.findByStudentId(studentId);
        
        int total = 0;
        for (Homework homework : homeworks) {
            total += homework.getPoints();
        }
        
        return total;
    }
    
    /**
     * Calculates target achievement points for a student.
     * Gets totalPointsEarned from TargetAchievementStreak.
     * 
     * @param studentId The student ID
     * @param streakDAO The streak DAO
     * @return Total target points
     */
    public static int calculateTargetPoints(Integer studentId, TargetAchievementStreakDAO streakDAO) {
        TargetAchievementStreak streak = streakDAO.findByStudentId(studentId);
        
        if (streak == null) {
            return 0;
        }
        
        return streak.getTotalPointsEarned();
    }
    
    
    // ========== Ranking Helpers ==========
    
    /**
     * Applies tie-breaking rules to rankings.
     * 
     * Tie-breaking order:
     * 1. Quiz points (DESC)
     * 2. Target points (DESC)
     * 3. Homework points (DESC)
     * 4. Attendance points (DESC)
     * 5. Registration date (ASC - older students first)
     * 6. Alphabetical by name (ASC)
     * 
     * @param points List of Fasee7Points
     * @param studentDAO The student DAO (to fetch registration dates and names)
     * @return Sorted list with tie-breaking applied
     */
    public static List<Fasee7Points> applyTieBreaking(List<Fasee7Points> points, StudentDAO studentDAO) {
        // Create a map of studentId → Student for efficient lookup
        Map<Integer, Student> studentMap = new HashMap<>();
        for (Fasee7Points p : points) {
            Student student = studentDAO.findById(p.getStudentId());
            if (student != null) {
                studentMap.put(p.getStudentId(), student);
            }
        }
        
        // Sort with comparator
        List<Fasee7Points> sorted = new ArrayList<>(points);
        sorted.sort((p1, p2) -> {
            // 1. Total points (DESC)
            int cmp = p2.getTotalPoints().compareTo(p1.getTotalPoints());
            if (cmp != 0) return cmp;
            
            // 2. Quiz points (DESC)
            cmp = p2.getQuizPoints().compareTo(p1.getQuizPoints());
            if (cmp != 0) return cmp;
            
            // 3. Target points (DESC)
            cmp = Integer.compare(p2.getTargetPoints(), p1.getTargetPoints());
            if (cmp != 0) return cmp;
            
            // 4. Homework points (DESC)
            cmp = Integer.compare(p2.getHomeworkPoints(), p1.getHomeworkPoints());
            if (cmp != 0) return cmp;
            
            // 5. Attendance points (DESC)
            cmp = Integer.compare(p2.getAttendancePoints(), p1.getAttendancePoints());
            if (cmp != 0) return cmp;
            
            // 6. Registration date (ASC - older first)
            Student s1 = studentMap.get(p1.getStudentId());
            Student s2 = studentMap.get(p2.getStudentId());
            
            if (s1 != null && s2 != null) {
                cmp = s1.getRegistrationDate().compareTo(s2.getRegistrationDate());
                if (cmp != 0) return cmp;
                
                // 7. Name (ASC - alphabetical)
                cmp = s1.getFullName().compareTo(s2.getFullName());
                if (cmp != 0) return cmp;
            }
            
            return 0;
        });
        
        return sorted;
    }
    
    
    // ========== Snapshot Serialization Helpers ==========
    
    /**
     * Serializes rankings to JSON string.
     * 
     * @param rankings List of Fasee7Points (ranked)
     * @return JSON string
     */
    public static String serializeRankings(List<Fasee7Points> rankings) {
        // Create a simplified structure for serialization
        List<Map<String, Object>> data = new ArrayList<>();
        
        int rank = 1;
        for (Fasee7Points points : rankings) {
            Map<String, Object> entry = new HashMap<>();
            entry.put("studentId", points.getStudentId());
            entry.put("rank", rank++);
            entry.put("quizPoints", points.getQuizPoints());
            entry.put("attendancePoints", points.getAttendancePoints());
            entry.put("homeworkPoints", points.getHomeworkPoints());
            entry.put("targetPoints", points.getTargetPoints());
            entry.put("totalPoints", points.getTotalPoints());
            
            data.add(entry);
        }
        
        return gson.toJson(data);
    }
    
    /**
     * Deserializes JSON string to ranking map.
     * 
     * @param jsonData JSON string from snapshot
     * @return Map of studentId → rank
     */
    public static Map<Integer, Integer> deserializeRankings(String jsonData) {
        Type type = new TypeToken<List<Map<String, Object>>>(){}.getType();
        List<Map<String, Object>> data = gson.fromJson(jsonData, type);
        
        Map<Integer, Integer> rankMap = new HashMap<>();
        
        for (Map<String, Object> entry : data) {
            // Gson deserializes numbers as Double by default
            Integer studentId = ((Double) entry.get("studentId")).intValue();
            Integer rank = ((Double) entry.get("rank")).intValue();
            
            rankMap.put(studentId, rank);
        }
        
        return rankMap;
    }
    
    /**
     * Builds rank comparison map between two snapshots.
     * 
     * @param rank1Map Rankings from first snapshot
     * @param rank2Map Rankings from second snapshot
     * @return Map of studentId → rank change (positive = improved)
     */
    public static Map<Integer, Integer> buildRankComparison(Map<Integer, Integer> rank1Map, 
                                                             Map<Integer, Integer> rank2Map) {
        Map<Integer, Integer> comparison = new HashMap<>();
        
        // Get all student IDs from both snapshots
        Set<Integer> allStudentIds = new HashSet<>();
        allStudentIds.addAll(rank1Map.keySet());
        allStudentIds.addAll(rank2Map.keySet());
        
        for (Integer studentId : allStudentIds) {
            Integer oldRank = rank1Map.get(studentId);
            Integer newRank = rank2Map.get(studentId);
            
            // Handle cases where student doesn't exist in one snapshot
            if (oldRank == null) {
                oldRank = Integer.MAX_VALUE; // New student (not ranked before)
            }
            if (newRank == null) {
                newRank = Integer.MAX_VALUE; // Student removed (archived)
            }
            
            int rankChange = oldRank - newRank; // Positive = improved
            comparison.put(studentId, rankChange);
        }
        
        return comparison;
    }
}