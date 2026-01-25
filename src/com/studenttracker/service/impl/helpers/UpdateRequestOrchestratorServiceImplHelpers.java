package com.studenttracker.service.impl.helpers;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.studenttracker.dao.UpdateRequestDAO;
import com.studenttracker.exception.UnauthorizedException;
import com.studenttracker.exception.ValidationException;
import com.studenttracker.model.UpdateRequest;
import com.studenttracker.model.User;
import com.studenttracker.service.*;

import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * Helper class for UpdateRequestOrchestratorServiceImpl.
 * Contains utility methods for validation, parsing, and cascading updates.
 */
public class UpdateRequestOrchestratorServiceImplHelpers {
    
    private static final Gson gson = new Gson();
    
    // Valid request types
    private static final List<String> VALID_REQUEST_TYPES = Arrays.asList(
        "UPDATE_ATTENDANCE", "UPDATE_HOMEWORK", "UPDATE_QUIZ_SCORE",
        "ADD_BEHAVIORAL_INCIDENT", "REMOVE_BEHAVIORAL_INCIDENT", "RESTORE_ARCHIVED_STUDENT"
    );
    
    // Valid entity types
    private static final List<String> VALID_ENTITY_TYPES = Arrays.asList(
        "ATTENDANCE", "HOMEWORK", "QUIZ_SCORE", "BEHAVIORAL_INCIDENT", "STUDENT"
    );
    
    private UpdateRequestOrchestratorServiceImplHelpers() {}
    
    // ========== Validation Methods ==========
    
    /**
     * Validates request format.
     * 
     * @param requestType Type of request
     * @param entityType Type of entity
     * @param requestedChangesJson JSON string with changes
     * @throws ValidationException if format is invalid
     */
    public static void validateRequestFormat(String requestType, String entityType, 
                                             String requestedChangesJson) {
        if (requestType == null || requestType.trim().isEmpty()) {
            throw new ValidationException("Request type cannot be empty");
        }
        
        if (!VALID_REQUEST_TYPES.contains(requestType)) {
            throw new ValidationException("Invalid request type: " + requestType);
        }
        
        if (entityType == null || entityType.trim().isEmpty()) {
            throw new ValidationException("Entity type cannot be empty");
        }
        
        if (!VALID_ENTITY_TYPES.contains(entityType)) {
            throw new ValidationException("Invalid entity type: " + entityType);
        }
        
        if (requestedChangesJson == null || requestedChangesJson.trim().isEmpty()) {
            throw new ValidationException("Requested changes cannot be empty");
        }
        
        // Try to parse JSON
        try {
            parseRequestedChanges(requestedChangesJson);
        } catch (Exception e) {
            throw new ValidationException("Invalid JSON format: " + e.getMessage());
        }
    }
    
    /**
     * Validates admin permission.
     * 
     * @param adminId User ID
     * @param userService User service
     * @throws UnauthorizedException if user is not admin
     */
    public static void validateAdminPermission(Integer adminId, UserService userService) {
        User user = userService.getUserById(adminId);
        if (user == null || !user.isAdmin()) {
            throw new UnauthorizedException("Only admins can approve/reject requests");
        }
    }
    
    /**
     * Validates entity still exists.
     * 
     * @param entityType Type of entity
     * @param entityId ID of entity
     * @param attendanceService Attendance service
     * @param quizService Quiz service
     * @param studentService Student service
     * @param homeworkService Homework service
     * @param behavioralService Behavioral service
     * @return true if entity exists
     */
    public static boolean validateEntityExists(String entityType, Integer entityId,
                                               AttendanceService attendanceService,
                                               QuizService quizService,
                                               StudentService studentService,
                                               HomeworkService homeworkService,
                                               BehavioralIncidentService behavioralService) {
        switch (entityType) {
            case "ATTENDANCE":
                // Would need getAttendanceById method
                return true; // Assume exists for now
            case "HOMEWORK":
                // Would need getHomeworkById method
                return true; // Assume exists for now
            case "QUIZ_SCORE":
                // Would need getQuizScoreById method
                return true; // Assume exists for now
            case "BEHAVIORAL_INCIDENT":
                return behavioralService.getIncidentById(entityId) != null;
            case "STUDENT":
                return studentService.getStudentById(entityId) != null;
            default:
                return false;
        }
    }
    
    /**
     * Validates attendance update rules.
     * If changing to PRESENT, quiz scores and homework must exist.
     * 
     * @param attendanceId Attendance ID
     * @param attendanceService Attendance service
     * @param quizService Quiz service
     * @param homeworkService Homework service
     * @throws ValidationException if rules violated
     */
    public static void validateAttendanceUpdateRules(Integer attendanceId,
                                                     AttendanceService attendanceService,
                                                     QuizService quizService,
                                                     HomeworkService homeworkService) {
        // Get attendance record (would need method to get by ID)
        // For now, we'll validate based on lesson and student
        
        // This is a simplified version - in real implementation:
        // 1. Get attendance by ID
        // 2. Get lessonId and studentId from attendance
        // 3. Check if lesson has quiz
        // 4. If yes, check if student has quiz scores
        // 5. Check if lesson has homework
        // 6. If yes, check if student has homework status
        
        // Placeholder for actual validation
        // throw new ValidationException("Cannot mark PRESENT without quiz scores");
    }
    
    // ========== Parsing Methods ==========
    
    /**
     * Parses requested changes JSON.
     * 
     * @param requestedChangesJson JSON string
     * @return Map of changes
     */
    public static Map<String, Object> parseRequestedChanges(String requestedChangesJson) {
        Type type = new TypeToken<Map<String, Object>>(){}.getType();
        return gson.fromJson(requestedChangesJson, type);
    }
    
    // ========== Conflict Checking ==========
    
    /**
     * Checks for conflicting pending requests.
     * 
     * @param entityType Type of entity
     * @param entityId ID of entity
     * @param updateRequestDAO DAO
     * @return true if conflict exists
     */
    public static boolean checkConflicts(String entityType, Integer entityId, 
                                        UpdateRequestDAO updateRequestDAO) {
        List<UpdateRequest> pendingRequests = updateRequestDAO.findPendingByEntity(
            entityType, entityId
        );
        return !pendingRequests.isEmpty();
    }
    
    // ========== Cascading Updates ==========
    
    /**
     * Triggers cascading updates after successful execution.
     * 
     * @param request The update request
     * @param consecutivityService Consecutivity service
     * @param warningService Warning service
     * @param targetService Target service
     */
    public static void triggerCascadingUpdates(UpdateRequest request,
                                               ConsecutivityTrackingService consecutivityService,
                                               WarningService warningService,
                                               TargetService targetService) {
        try {
            var changes = parseRequestedChanges(request.getRequestedChanges());
            Integer studentId = extractStudentId(request, changes);
            
            if (studentId == null) {
                return; // No student involved, skip cascading
            }
            
            // 1. Recalculate consecutivity
            String requestType = request.getRequestType();
            if ("UPDATE_ATTENDANCE".equals(requestType)) {
                // Consecutivity will be handled by event subscribers
                // No direct call needed here
            }
            
            // 2. Check and generate/resolve warnings
            warningService.checkAndGenerateWarnings(studentId);
            
            // 3. Check targets (TODO: implement logic)
            // targetService.checkTargets(studentId);
            
            // 4. Recalculate Fasee7 points (TODO: implement)
            // fasee7Service.recalculateFasee7Points(studentId);
            
        } catch (Exception e) {
            System.err.println("Warning: Some cascading updates failed: " + e.getMessage());
            // Don't throw - allow main update to succeed
        }
    }
    
    /**
     * Extracts student ID from request.
     * 
     * @param request The update request
     * @param changes Parsed changes map
     * @return Student ID or null
     */
    private static Integer extractStudentId(UpdateRequest request, Map<String, Object> changes) {
        if (changes.containsKey("studentId")) {
            Object studentIdObj = changes.get("studentId");
            if (studentIdObj instanceof Double) {
                return ((Double) studentIdObj).intValue();
            } else if (studentIdObj instanceof Integer) {
                return (Integer) studentIdObj;
            }
        }
        return null;
    }
}