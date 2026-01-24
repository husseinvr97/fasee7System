package com.studenttracker.service.impl.helpers;

import com.studenttracker.exception.ServiceException;
import com.studenttracker.model.BehavioralIncident;
import com.studenttracker.model.BehavioralIncident.IncidentType;
import com.studenttracker.model.Warning.WarningType;
import com.studenttracker.service.BehavioralIncidentService;
import com.studenttracker.service.ConsecutivityTrackingService;
import com.studenttracker.service.WarningService;

import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Helper class for WarningServiceImpl.
 * Contains utility methods for warning generation logic.
 */
public class WarningServiceImplHelpers {
    
    private WarningServiceImplHelpers() {}
    
    
    // ========== Warning Type and Reason Helpers ==========
    
    /**
     * Determines warning type based on consecutive absence count.
     * 
     * @param consecutiveCount The consecutive absence count
     * @return CONSECUTIVE_ABSENCE for 2, ARCHIVED for 3+
     */
    public static WarningType determineWarningType(int consecutiveCount) {
        if (consecutiveCount >= 3) {
            return WarningType.ARCHIVED;
        } else if (consecutiveCount == 2) {
            return WarningType.CONSECUTIVE_ABSENCE;
        }
        return WarningType.CONSECUTIVE_ABSENCE; // Default
    }
    
    /**
     * Creates warning reason string for absence warnings.
     * 
     * @param consecutiveCount The consecutive absence count
     * @return Formatted reason string
     */
    public static String createWarningReason(int consecutiveCount) {
        return String.format("Student has %d consecutive absences", consecutiveCount);
    }
    
    
    // ========== Warning Check Methods ==========
    
    /**
     * Checks consecutive absences and generates warnings if thresholds reached.
     * 
     * @param studentId The student ID
     * @param consecutivityService The consecutivity tracking service
     * @param warningService The warning service (to generate warnings)
     */
    public static void checkConsecutiveAbsences(Integer studentId,
                                               ConsecutivityTrackingService consecutivityService,
                                               WarningService warningService) {
        try {
            int consecutiveCount = consecutivityService.getConsecutiveAbsenceCount(studentId);
            
            if (consecutiveCount == 2) {
                warningService.generateAbsenceWarning(studentId, 2);
            } else if (consecutiveCount >= 3) {
                warningService.generateAbsenceWarning(studentId, 3);
            }
        } catch (ServiceException e) {
            // Log error but don't throw
            System.err.println("Error checking consecutive absences for student " + 
                             studentId + ": " + e.getMessage());
        }
    }
    
    /**
     * Checks behavioral incidents and generates warnings if rules violated.
     * 
     * @param studentId The student ID
     * @param behavioralService The behavioral incident service
     * @param warningService The warning service (to generate warnings)
     */
    public static void checkBehavioralIncidents(Integer studentId,
                                               BehavioralIncidentService behavioralService,
                                               WarningService warningService) {
        // Check consecutive same-type incidents
        checkConsecutiveSameType(studentId, behavioralService, warningService);
        
        // Check 3 incidents in a month
        checkThreeInMonth(studentId, behavioralService, warningService);
    }
    
    /**
     * Checks if student has 2+ consecutive same-type behavioral incidents.
     * 
     * @param studentId The student ID
     * @param behavioralService The behavioral incident service
     * @param warningService The warning service
     */
    public static void checkConsecutiveSameType(Integer studentId,
                                               BehavioralIncidentService behavioralService,
                                               WarningService warningService) {
        List<BehavioralIncident> consecutiveIncidents = 
            behavioralService.getConsecutiveSameTypeIncidents(studentId);
        
        if (consecutiveIncidents.size() >= 2) {
            IncidentType type = consecutiveIncidents.get(0).getIncidentType();
            String reason = String.format("2 consecutive %s incidents", type.name());
            warningService.generateBehavioralWarning(studentId, reason);
        }
    }
    
    /**
     * Checks if student has 3+ behavioral incidents in any single month.
     * 
     * @param studentId The student ID
     * @param behavioralService The behavioral incident service
     * @param warningService The warning service
     */
    public static void checkThreeInMonth(Integer studentId,
                                        BehavioralIncidentService behavioralService,
                                        WarningService warningService) {
        List<BehavioralIncident> allIncidents = behavioralService.getIncidentsByStudent(studentId);
        
        // Group incidents by month
        Map<YearMonth, Integer> monthCounts = new HashMap<>();
        for (BehavioralIncident incident : allIncidents) {
            LocalDateTime createdAt = incident.getCreatedAt();
            YearMonth month = YearMonth.from(createdAt);
            monthCounts.put(month, monthCounts.getOrDefault(month, 0) + 1);
        }
        
        // Check if any month has 3+ incidents
        for (Map.Entry<YearMonth, Integer> entry : monthCounts.entrySet()) {
            if (entry.getValue() >= 3) {
                String reason = String.format("3 behavioral incidents in %s", entry.getKey());
                warningService.generateBehavioralWarning(studentId, reason);
                break; // Only generate one warning (for most recent month with 3+)
            }
        }
    }
}