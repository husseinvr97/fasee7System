package com.studenttracker.service.impl.helpers;

import com.studenttracker.dao.LessonDAO;
import com.studenttracker.dao.UserDAO;
import com.studenttracker.exception.UnauthorizedException;
import com.studenttracker.exception.UserNotFoundException;
import com.studenttracker.model.BehavioralIncident;
import com.studenttracker.model.BehavioralIncident.IncidentType;
import com.studenttracker.model.Lesson;
import com.studenttracker.model.User;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Helper methods for BehavioralIncidentServiceImpl.
 * Keeps the main service class clean and focused.
 */
public class BehavioralIncidentServiceImplHelpers {
    
    private BehavioralIncidentServiceImplHelpers() {}
    
    /**
     * Validates that the user is an admin.
     * 
     * @param userId The user ID
     * @param userDAO The UserDAO instance
     * @throws UserNotFoundException if user not found
     * @throws UnauthorizedException if user is not admin
     */
    public static void validateAdminPermission(Integer userId, UserDAO userDAO) {
        User user = userDAO.findById(userId);
        if (user == null) {
            throw new UserNotFoundException("the user with id " + userId + " does not exist");
        }
        if (!user.isAdmin()) {
            throw new UnauthorizedException("Only admins can perform this action");
        }
    }
    
    /**
     * Groups consecutive incidents of the same type and returns the longest sequence.
     * 
     * @param incidents List of incidents (should be ordered by date)
     * @return List of consecutive same-type incidents (longest sequence)
     */
    public static List<BehavioralIncident> groupConsecutiveSameType(List<BehavioralIncident> incidents) {
        if (incidents == null || incidents.isEmpty()) {
            return new ArrayList<>();
        }
        
        // Sort by createdAt ascending to process chronologically
        List<BehavioralIncident> sorted = new ArrayList<>(incidents);
        sorted.sort(Comparator.comparing(BehavioralIncident::getCreatedAt));
        
        List<BehavioralIncident> longestSequence = new ArrayList<>();
        List<BehavioralIncident> currentSequence = new ArrayList<>();
        IncidentType currentType = null;
        
        for (BehavioralIncident incident : sorted) {
            if (currentType == null || incident.getIncidentType() == currentType) {
                // Same type or first incident
                currentSequence.add(incident);
                currentType = incident.getIncidentType();
            } else {
                // Type changed - check if current sequence is longest
                if (currentSequence.size() > longestSequence.size()) {
                    longestSequence = new ArrayList<>(currentSequence);
                }
                // Start new sequence
                currentSequence.clear();
                currentSequence.add(incident);
                currentType = incident.getIncidentType();
            }
        }
        
        // Check final sequence
        if (currentSequence.size() > longestSequence.size()) {
            longestSequence = new ArrayList<>(currentSequence);
        }
        
        return longestSequence;
    }
    
    /**
     * Filters incidents by month group.
     * 
     * @param incidents List of incidents
     * @param monthGroup The target month group
     * @param lessonDAO The LessonDAO instance
     * @return Filtered list of incidents
     */
    public static List<BehavioralIncident> filterByMonthGroup(List<BehavioralIncident> incidents, 
                                                              String monthGroup, 
                                                              LessonDAO lessonDAO) {
        if (incidents == null || incidents.isEmpty()) {
            return new ArrayList<>();
        }
        
        // Extract unique lesson IDs
        Set<Integer> lessonIds = incidents.stream()
            .map(BehavioralIncident::getLessonId)
            .collect(Collectors.toSet());
        
        // Create map: lessonId -> monthGroup
        Map<Integer, String> lessonMonthMap = new HashMap<>();
        for (Integer lessonId : lessonIds) {
            Lesson lesson = lessonDAO.findById(lessonId);
            if (lesson != null) {
                lessonMonthMap.put(lessonId, lesson.getMonthGroup());
            }
        }
        
        // Filter incidents by month group
        return incidents.stream()
            .filter(incident -> {
                String incidentMonthGroup = lessonMonthMap.get(incident.getLessonId());
                return monthGroup.equals(incidentMonthGroup);
            })
            .collect(Collectors.toList());
    }
    
    /**
     * Builds a breakdown of incident counts by type.
     * 
     * @param incidents List of incidents
     * @return Map of incident type to count
     */
    public static Map<IncidentType, Integer> buildIncidentTypeBreakdown(List<BehavioralIncident> incidents) {
        // Initialize map with all types set to 0
        Map<IncidentType, Integer> breakdown = new HashMap<>();
        for (IncidentType type : IncidentType.values()) {
            breakdown.put(type, 0);
        }
        
        // Count incidents by type
        if (incidents != null && !incidents.isEmpty()) {
            for (BehavioralIncident incident : incidents) {
                IncidentType type = incident.getIncidentType();
                breakdown.put(type, breakdown.get(type) + 1);
            }
        }
        
        return breakdown;
    }
}