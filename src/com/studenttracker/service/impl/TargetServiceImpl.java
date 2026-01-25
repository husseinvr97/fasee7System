package com.studenttracker.service.impl;

import com.google.common.eventbus.Subscribe;
import com.studenttracker.dao.TargetDAO;
import com.studenttracker.dao.TargetAchievementStreakDAO;
import com.studenttracker.model.Target;
import com.studenttracker.model.Target.TopicCategory;
import com.studenttracker.model.TargetAchievementStreak;
import com.studenttracker.service.EventBusService;
import com.studenttracker.service.TargetService;
import com.studenttracker.service.event.*;
import com.studenttracker.service.impl.helpers.TargetServiceImplHelpers;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Implementation of TargetService.
 * Handles automatic target generation and achievement tracking.
 * Subscribes to performance events to trigger target operations.
 */
public class TargetServiceImpl implements TargetService {
    
    private final TargetDAO targetDAO;
    private final TargetAchievementStreakDAO streakDAO;
    private final EventBusService eventBusService;
    
    /**
     * Constructor with dependency injection.
     * Registers this service as an event subscriber.
     */
    public TargetServiceImpl(TargetDAO targetDAO,
                            TargetAchievementStreakDAO streakDAO,
                            EventBusService eventBus) {
        this.targetDAO = targetDAO;
        this.streakDAO = streakDAO;
        this.eventBusService = eventBus;
        
        // Register as event subscriber
        this.eventBusService.register(this);
    }
    
    
    // ========== Event Subscribers ==========
    
    /**
     * Handles PerformanceDegradationDetectedEvent.
     * Generates stacked targets to restore PI.
     */
    @Subscribe
    public void onPerformanceDegradation(PerformanceDegradationDetectedEvent event) {
        generateTargetsOnDegradation(
            event.getStudentId(),
            com.studenttracker.model.Target.TopicCategory.valueOf(event.getCategory().name()),
            event.getPreviousPi(),
            event.getCurrentPi()
        );
    }
    
    /**
     * Handles PerformanceImprovementDetectedEvent.
     * Checks and achieves targets that student has reached.
     */
    @Subscribe
    public void onPerformanceImprovement(PerformanceImprovementDetectedEvent event) {
        checkAndAchieveTargets(
            event.getStudentId(),
            com.studenttracker.model.Target.TopicCategory.valueOf(event.getCategory().name()),
            event.getCurrentPi()
        );
    }
    
    
    // ========== Target Generation ==========
    
    @Override
    public Integer createTarget(Integer studentId, TopicCategory category, int targetPiValue) {
        // Step 1: Validate target doesn't already exist
        TargetServiceImplHelpers.validateTargetDoesNotExist(studentId, category, targetPiValue, targetDAO);
        
        // Step 2: Create Target object
        Target target = new Target();
        target.setStudentId(studentId);
        target.setCategory(category);
        target.setTargetPiValue(targetPiValue);
        target.setCreatedAt(LocalDateTime.now());
        target.setAchieved(false);
        target.setAchievedAt(null);
        
        // Step 3: Insert into database
        Integer targetId = targetDAO.insert(target);
        
        // Step 4: Publish TargetCreatedEvent
        TargetCreatedEvent event = new TargetCreatedEvent(
            targetId,
            studentId,
            category,
            targetPiValue,
            target.getCreatedAt()
        );
        eventBusService.publish(event);
        
        // Step 5: Return target ID
        return targetId;
    }
    
    @Override
    public void generateTargetsOnDegradation(Integer studentId, TopicCategory category, 
                                            int previousPi, int currentPi) {
        // Step 1: Calculate degradation
        int degradationAmount = previousPi - currentPi;
        
        if (degradationAmount <= 0) {
            return; // No degradation, nothing to do
        }
        
        // Step 2: Reset streak (degradation breaks streak)
        resetStreak(studentId);
        
        // Step 3: Create stacked targets
        TargetServiceImplHelpers.createStackedTargets(studentId, category, previousPi, currentPi, this);
    }
    
    
    // ========== Target Achievement ==========
    
    @Override
    public void checkAndAchieveTargets(Integer studentId, TopicCategory category, int newPi) {
        // Step 1: Get all active targets for student
        List<Target> activeTargets = targetDAO.findActiveByStudent(studentId);
        
        // Step 2: Filter achievable targets (category matches and targetPiValue <= newPi)
        List<Target> achievableTargets = TargetServiceImplHelpers.filterAchievableTargets(
            activeTargets, category, newPi
        );
        
        // Step 3: Achieve each target
        for (Target target : achievableTargets) {
            achieveTarget(target.getTargetId());
        }
    }
    
    @Override
    public boolean achieveTarget(Integer targetId) {
        // Step 1: Get target
        Target target = targetDAO.findById(targetId);
        if (target == null) {
            return false;
        }
        
        // Step 2: Mark as achieved
        target.achieve();
        
        // Step 3: Update in database
        boolean success = targetDAO.update(target);
        
        if (success) {
            // Step 4: Update streak
            updateStreak(target.getStudentId(), true);
            
            // Step 5: Publish TargetAchievedEvent
            TargetAchievedEvent event = new TargetAchievedEvent(
                targetId,
                target.getStudentId(),
                target.getCategory(),
                target.getAchievedAt()
            );
            eventBusService.publish(event);
        }
        
        return success;
    }
    
    
    // ========== Streak Management ==========
    
    @Override
    public void updateStreak(Integer studentId, boolean achieved) {
        // Step 1: Get or create streak record
        TargetAchievementStreak streak = TargetServiceImplHelpers.getOrCreateStreak(studentId, streakDAO);
        
        // Step 2: Update streak based on achievement
        if (achieved) {
            // Increment streak
            streak.incrementStreak();
        } else {
            // Reset streak (degradation)
            streak.resetStreak();
        }
        
        // Step 3: Upsert to database
        streakDAO.upsert(streak);
        
        // Step 4: Publish TargetStreakUpdatedEvent
        TargetStreakUpdatedEvent event = new TargetStreakUpdatedEvent(
            studentId,
            streak.getCurrentStreak(),
            streak.getTotalPointsEarned()
        );
        eventBusService.publish(event);
    }
    
    @Override
    public void resetStreak(Integer studentId) {
        // Step 1: Get streak record
        TargetAchievementStreak streak = TargetServiceImplHelpers.getOrCreateStreak(studentId, streakDAO);
        
        // Step 2: Reset to 0
        streak.resetStreak();
        
        // Step 3: Update in database
        streakDAO.upsert(streak);
        
        // Step 4: Publish event
        TargetStreakUpdatedEvent event = new TargetStreakUpdatedEvent(
            studentId,
            streak.getCurrentStreak(),
            streak.getTotalPointsEarned()
        );
        eventBusService.publish(event);
    }
    
    
    // ========== Retrieval ==========
    
    @Override
    public Target getTargetById(Integer targetId) {
        return targetDAO.findById(targetId);
    }
    
    @Override
    public List<Target> getActiveTargets(Integer studentId) {
        return targetDAO.findActiveByStudent(studentId);
    }
    
    @Override
    public List<Target> getAchievedTargets(Integer studentId) {
        return targetDAO.findAchievedByStudent(studentId);
    }
    
    @Override
    public List<Target> getTargetsByCategory(Integer studentId, TopicCategory category) {
        return targetDAO.findByStudentAndCategory(studentId, category);
    }
    
    @Override
    public TargetAchievementStreak getStreak(Integer studentId) {
        return TargetServiceImplHelpers.getOrCreateStreak(studentId, streakDAO);
    }
    
    @Override
    public int getCurrentStreak(Integer studentId) {
        TargetAchievementStreak streak = getStreak(studentId);
        return streak.getCurrentStreak();
    }
    
    @Override
    public int getTotalTargetPoints(Integer studentId) {
        TargetAchievementStreak streak = getStreak(studentId);
        return streak.getTotalPointsEarned();
    }
    
    
    // ========== Statistics ==========
    
    @Override
    public int getActiveTargetCount(Integer studentId) {
        return targetDAO.countActiveByStudent(studentId);
    }
    
    @Override
    public int getAchievedTargetCount(Integer studentId) {
        return targetDAO.countAchievedByStudent(studentId);
    }
    
    @Override
    public List<TargetAchievementStreak> getTopStreaks(int limit) {
        return streakDAO.getTopStreaks(limit);
    }
    
    
    // ========== Default Target ==========
    
    @Override
    public boolean hasActiveTargets(Integer studentId) {
        return getActiveTargetCount(studentId) > 0;
    }
    
    @Override
    public String getDefaultTargetMessage(Integer studentId) {
        if (!hasActiveTargets(studentId)) {
            return "Improve PI in any category";
        }
        return "";
    }
}