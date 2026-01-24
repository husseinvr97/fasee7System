package com.studenttracker.service.impl;

import com.google.common.eventbus.Subscribe;
import com.studenttracker.dao.WarningDAO;
import com.studenttracker.model.Warning;
import com.studenttracker.model.Warning.WarningType;
import com.studenttracker.service.BehavioralIncidentService;
import com.studenttracker.service.ConsecutivityTrackingService;
import com.studenttracker.service.EventBusService;
import com.studenttracker.service.WarningService;
import com.studenttracker.service.event.*;
import com.studenttracker.service.impl.helpers.WarningServiceImplHelpers;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Implementation of WarningService.
 * Handles automatic warning generation, resolution, and statistics.
 * Subscribes to events to trigger warning generation.
 */
public class WarningServiceImpl implements WarningService {
    
    private final WarningDAO warningDAO;
    private final ConsecutivityTrackingService consecutivityService;
    private final BehavioralIncidentService behavioralService;
    private final EventBusService eventBusService;
    
    /**
     * Constructor with dependency injection.
     * Registers this service as an event subscriber.
     */
    public WarningServiceImpl(WarningDAO warningDAO,
                             ConsecutivityTrackingService consecutivityService,
                             BehavioralIncidentService behavioralService,
                             EventBusService eventBus) {
        this.warningDAO = warningDAO;
        this.consecutivityService = consecutivityService;
        this.behavioralService = behavioralService;
        this.eventBusService = eventBus;
        
        // Register as event subscriber
        this.eventBusService.register(this);
    }
    
    
    // ========== Event Subscribers ==========
    
    /**
     * Handles ConsecutiveThresholdReachedEvent.
     * Generates absence warnings when thresholds are reached.
     */
    @Subscribe
    public void onConsecutiveThresholdReached(ConsecutiveThresholdReachedEvent event) {
        if (event.getTrackingType() == com.studenttracker.model.ConsecutivityTracking.TrackingType.ABSENCE) {
            generateAbsenceWarning(event.getStudentId(), event.getConsecutiveCount());
        }
    }
    
    /**
     * Handles BehavioralIncidentAddedEvent.
     * Checks if behavioral warnings should be generated.
     */
    @Subscribe
    public void onBehavioralIncidentAdded(BehavioralIncidentAddedEvent event) {
        checkAndGenerateWarnings(event.getStudentId());
    }
    
    /**
     * Handles StudentRestoredEvent.
     * Resolves all active warnings when student is restored.
     */
    @Subscribe
    public void onStudentRestored(StudentRestoredEvent event) {
        // Resolve ARCHIVED warnings
        resolveWarningsByStudent(event.getStudentId(), WarningType.ARCHIVED);
        
        // Resolve CONSECUTIVE_ABSENCE warnings
        resolveWarningsByStudent(event.getStudentId(), WarningType.CONSECUTIVE_ABSENCE);
    }
    
    
    // ========== Generate Warnings ==========
    
    @Override
    public Integer generateAbsenceWarning(Integer studentId, int consecutiveCount) {
        // Step 1: Determine warning type based on count
        WarningType warningType = WarningServiceImplHelpers.determineWarningType(consecutiveCount);
        
        // Step 2: Create warning reason string
        String reason = WarningServiceImplHelpers.createWarningReason(consecutiveCount);
        
        // Step 3: Create Warning object
        Warning warning = new Warning();
        warning.setStudentId(studentId);
        warning.setWarningType(warningType);
        warning.setWarningReason(reason);
        warning.setCreatedAt(LocalDateTime.now());
        warning.setActive(true);
        warning.setResolvedAt(null);
        
        // Step 4: Insert into database
        Integer warningId = warningDAO.insert(warning);
        
        // Step 5: Publish WarningGeneratedEvent
        WarningGeneratedEvent event = new WarningGeneratedEvent(
            warningId,
            studentId,
            warningType,
            reason,
            warning.getCreatedAt()
        );
        eventBusService.publish(event);
        
        // Step 6: Return warning ID
        return warningId;
    }
    
    @Override
    public Integer generateBehavioralWarning(Integer studentId, String reason) {
        // Step 1: Create Warning object
        Warning warning = new Warning();
        warning.setStudentId(studentId);
        warning.setWarningType(WarningType.BEHAVIORAL);
        warning.setWarningReason(reason);
        warning.setCreatedAt(LocalDateTime.now());
        warning.setActive(true);
        warning.setResolvedAt(null);
        
        // Step 2: Insert into database
        Integer warningId = warningDAO.insert(warning);
        
        // Step 3: Publish WarningGeneratedEvent
        WarningGeneratedEvent event = new WarningGeneratedEvent(
            warningId,
            studentId,
            WarningType.BEHAVIORAL,
            reason,
            warning.getCreatedAt()
        );
        eventBusService.publish(event);
        
        // Step 4: Return warning ID
        return warningId;
    }
    
    
    // ========== Resolve Warnings ==========
    
    @Override
    public boolean resolveWarning(Integer warningId, String resolvedReason) {
        // Step 1: Get warning
        Warning warning = warningDAO.findById(warningId);
        if (warning == null) {
            return false;
        }
        
        // Step 2: Call resolve()
        warning.resolve();
        
        // Step 3: Update in database
        boolean success = warningDAO.update(warning);
        
        if (success) {
            // Step 4: Publish WarningResolvedEvent
            WarningResolvedEvent event = new WarningResolvedEvent(
                warningId,
                warning.getStudentId(),
                warning.getResolvedAt(),
                resolvedReason
            );
            eventBusService.publish(event);
        }
        
        return success;
    }
    
    @Override
    public boolean resolveWarningsByStudent(Integer studentId, WarningType type) {
        // Step 1: Get all active warnings of this type for student
        List<Warning> warnings = warningDAO.findByStudentAndActive(studentId, true);
        
        // Step 2: Filter by type and resolve each one
        boolean anyResolved = false;
        for (Warning warning : warnings) {
            if (warning.getWarningType() == type) {
                warning.resolve();
                boolean success = warningDAO.update(warning);
                
                if (success) {
                    anyResolved = true;
                    
                    // Publish WarningResolvedEvent
                    WarningResolvedEvent event = new WarningResolvedEvent(
                        warning.getWarningId(),
                        studentId,
                        warning.getResolvedAt(),
                        "Auto-resolved: conditions changed"
                    );
                    eventBusService.publish(event);
                }
            }
        }
        
        return anyResolved;
    }
    
    
    // ========== Retrieval ==========
    
    @Override
    public Warning getWarningById(Integer warningId) {
        return warningDAO.findById(warningId);
    }
    
    @Override
    public List<Warning> getActiveWarnings() {
        return warningDAO.findByActive(true);
    }
    
    @Override
    public List<Warning> getResolvedWarnings() {
        return warningDAO.findByActive(false);
    }
    
    @Override
    public List<Warning> getWarningsByStudent(Integer studentId) {
        return warningDAO.findByStudentId(studentId);
    }
    
    @Override
    public List<Warning> getActiveWarningsByStudent(Integer studentId) {
        return warningDAO.findByStudentAndActive(studentId, true);
    }
    
    @Override
    public List<Warning> getWarningsByType(WarningType type) {
        return warningDAO.findByType(type);
    }
    
    
    // ========== Statistics ==========
    
    @Override
    public int getActiveWarningCount() {
        return warningDAO.countActive();
    }
    
    @Override
    public int getWarningCountByType(WarningType type) {
        return warningDAO.countByType(type);
    }
    
    @Override
    public Map<WarningType, Integer> getWarningTypeBreakdown() {
        List<Warning> activeWarnings = warningDAO.findByActive(true);
        
        Map<WarningType, Integer> breakdown = new HashMap<>();
        for (WarningType type : WarningType.values()) {
            breakdown.put(type, 0);
        }
        
        for (Warning warning : activeWarnings) {
            WarningType type = warning.getWarningType();
            breakdown.put(type, breakdown.get(type) + 1);
        }
        
        return breakdown;
    }
    
    
    // ========== Check and Generate ==========
    
    @Override
    public void checkAndGenerateWarnings(Integer studentId) {
        // Step 1: Check consecutive absences
        WarningServiceImplHelpers.checkConsecutiveAbsences(studentId, consecutivityService, this);
        
        // Step 2: Check behavioral incidents
        WarningServiceImplHelpers.checkBehavioralIncidents(studentId, behavioralService, this);
    }
}