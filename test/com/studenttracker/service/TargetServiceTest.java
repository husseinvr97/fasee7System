package com.studenttracker.service;

import com.studenttracker.dao.TargetDAO;
import com.studenttracker.dao.TargetAchievementStreakDAO;
import com.studenttracker.exception.ValidationException;
import com.studenttracker.model.Target;
import com.studenttracker.model.TargetAchievementStreak;
import com.studenttracker.service.event.*;
import com.studenttracker.service.impl.TargetServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static com.studenttracker.model.LessonTopic.TopicCategory;

@DisplayName("TargetService Tests")
public class TargetServiceTest {

    @Mock
    private TargetDAO targetDAO;

    @Mock
    private TargetAchievementStreakDAO streakDAO;

    @Mock
    private EventBusService eventBusService;

    private TargetService targetService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        targetService = new TargetServiceImpl(targetDAO, streakDAO, eventBusService);
    }

    // ==================== Test 13.1: Create Target - Single Target ====================
    @Test
    @DisplayName("Test 13.1: Create single target successfully")
    void testCreateTarget_SingleTarget_Success() {
        // Arrange
        Integer studentId = 1;
        TopicCategory category = TopicCategory.NAHW;
        int targetPiValue = 6;

        when(targetDAO.hasActiveTarget(studentId, category, targetPiValue)).thenReturn(false);
        when(targetDAO.insert(any(Target.class))).thenReturn(100);

        // Act
        Integer targetId = targetService.createTarget(studentId, category, targetPiValue);

        // Assert
        assertNotNull(targetId);
        assertEquals(100, targetId);
        
        // Verify DAO calls
        verify(targetDAO, times(1)).hasActiveTarget(studentId, category, targetPiValue);
        verify(targetDAO, times(1)).insert(any(Target.class));
        
        // Verify event published
        ArgumentCaptor<TargetCreatedEvent> eventCaptor = ArgumentCaptor.forClass(TargetCreatedEvent.class);
        verify(eventBusService, times(1)).publish(eventCaptor.capture());
        
        TargetCreatedEvent event = eventCaptor.getValue();
        assertEquals(100, event.getTargetId());
        assertEquals(studentId, event.getStudentId());
        assertEquals(category, event.getCategory());
        assertEquals(targetPiValue, event.getTargetPiValue());
    }

    // ==================== Test 13.2: Create Target - Duplicate ====================
    @Test
    @DisplayName("Test 13.2: Create duplicate target should throw ValidationException")
    void testCreateTarget_Duplicate_ThrowsException() {
        // Arrange
        Integer studentId = 1;
        TopicCategory category = TopicCategory.NAHW;
        int targetPiValue = 6;

        when(targetDAO.hasActiveTarget(studentId, category, targetPiValue)).thenReturn(true);

        // Act & Assert
        ValidationException exception = assertThrows(
            ValidationException.class,
            () -> targetService.createTarget(studentId, category, targetPiValue)
        );

        assertTrue(exception.getMessage().contains("already exists"));
        verify(targetDAO, times(1)).hasActiveTarget(studentId, category, targetPiValue);
        verify(targetDAO, never()).insert(any(Target.class));
        verify(eventBusService, never()).publish(any());
    }

    // ==================== Test 13.3: Generate Targets on Degradation - Single Point Drop ====================
    @Test
    @DisplayName("Test 13.3: Generate targets on single point degradation")
    void testGenerateTargetsOnDegradation_SinglePointDrop_CreatesOneTarget() {
        // Arrange
        Integer studentId = 1;
        TopicCategory category = TopicCategory.NAHW;
        int previousPi = 6;
        int currentPi = 5;

        // Mock existing streak
        TargetAchievementStreak streak = new TargetAchievementStreak(studentId, 3, LocalDateTime.now(), 6);
        when(streakDAO.findByStudentId(studentId)).thenReturn(streak);
        when(streakDAO.upsert(any(TargetAchievementStreak.class))).thenReturn(true);

        when(targetDAO.hasActiveTarget(studentId, category, 6)).thenReturn(false);
        when(targetDAO.insert(any(Target.class))).thenReturn(101);

        // Act
        targetService.generateTargetsOnDegradation(studentId, category, previousPi, currentPi);

        // Assert
        // Verify streak reset
        ArgumentCaptor<TargetAchievementStreak> streakCaptor = ArgumentCaptor.forClass(TargetAchievementStreak.class);
        verify(streakDAO, times(1)).upsert(streakCaptor.capture());
        assertEquals(0, streakCaptor.getValue().getCurrentStreak());

        // Verify 1 target created for value 6
        verify(targetDAO, times(1)).hasActiveTarget(studentId, category, 6);
        verify(targetDAO, times(1)).insert(any(Target.class));
        
        // Verify events published (1 TargetCreatedEvent + 1 TargetStreakUpdatedEvent)
        verify(eventBusService, times(2)).publish(any());
    }

    // ==================== Test 13.4: Generate Targets on Degradation - Multiple Point Drop ====================
    @Test
    @DisplayName("Test 13.4: Generate stacked targets on multiple point degradation")
    void testGenerateTargetsOnDegradation_MultiplePointDrop_CreatesStackedTargets() {
        // Arrange
        Integer studentId = 1;
        TopicCategory category = TopicCategory.ADAB;
        int previousPi = 10;
        int currentPi = 7;

        // Mock streak
        TargetAchievementStreak streak = new TargetAchievementStreak(studentId, 5, LocalDateTime.now(), 15);
        when(streakDAO.findByStudentId(studentId)).thenReturn(streak);
        when(streakDAO.upsert(any(TargetAchievementStreak.class))).thenReturn(true);

        // Mock no existing targets
        when(targetDAO.hasActiveTarget(eq(studentId), eq(category), anyInt())).thenReturn(false);
        when(targetDAO.insert(any(Target.class))).thenReturn(102, 103, 104);

        // Act
        targetService.generateTargetsOnDegradation(studentId, category, previousPi, currentPi);

        // Assert
        // Verify 3 targets created: 8, 9, 10
        verify(targetDAO, times(1)).hasActiveTarget(studentId, category, 8);
        verify(targetDAO, times(1)).hasActiveTarget(studentId, category, 9);
        verify(targetDAO, times(1)).hasActiveTarget(studentId, category, 10);
        verify(targetDAO, times(3)).insert(any(Target.class));
        
        // Verify 3 TargetCreatedEvent + 1 TargetStreakUpdatedEvent
        verify(eventBusService, times(4)).publish(any());
    }

    // ==================== Test 13.5: Check and Achieve Targets - Single Achievement ====================
    @Test
    @DisplayName("Test 13.5: Check and achieve targets - single achievement")
    void testCheckAndAchieveTargets_SingleAchievement() {
        // Arrange
        Integer studentId = 1;
        TopicCategory category = TopicCategory.ADAB;
        int newPi = 6;

        Target target1 = createTarget(1, studentId, category, 6, false);
        Target target2 = createTarget(2, studentId, category, 8, false);

        when(targetDAO.findActiveByStudent(studentId)).thenReturn(Arrays.asList(target1, target2));
        when(targetDAO.findById(1)).thenReturn(target1);
        when(targetDAO.update(any(Target.class))).thenReturn(true);

        // Mock streak
        TargetAchievementStreak streak = new TargetAchievementStreak(studentId, 0, null, 0);
        when(streakDAO.findByStudentId(studentId)).thenReturn(streak);
        when(streakDAO.upsert(any(TargetAchievementStreak.class))).thenReturn(true);

        // Act
        targetService.checkAndAchieveTargets(studentId, category, newPi);

        // Assert
        // Verify only target1 (value 6) was achieved
        ArgumentCaptor<Target> targetCaptor = ArgumentCaptor.forClass(Target.class);
        verify(targetDAO, times(1)).update(targetCaptor.capture());
        assertTrue(targetCaptor.getValue().isAchieved());
        assertEquals(6, targetCaptor.getValue().getTargetPiValue());

        // Verify streak updated
        ArgumentCaptor<TargetAchievementStreak> streakCaptor = ArgumentCaptor.forClass(TargetAchievementStreak.class);
        verify(streakDAO, times(1)).upsert(streakCaptor.capture());
        assertEquals(1, streakCaptor.getValue().getCurrentStreak());
        assertEquals(1, streakCaptor.getValue().getTotalPointsEarned());

        // Verify events
        verify(eventBusService, times(2)).publish(any()); // TargetAchievedEvent + TargetStreakUpdatedEvent
    }

    // ==================== Test 13.6: Check and Achieve Targets - Multiple Achievements ====================
    @Test
    @DisplayName("Test 13.6: Check and achieve targets - multiple achievements in one quiz")
    void testCheckAndAchieveTargets_MultipleAchievements() {
        // Arrange
        Integer studentId = 1;
        TopicCategory category = TopicCategory.ADAB;
        int newPi = 10;

        Target target1 = createTarget(1, studentId, category, 8, false);
        Target target2 = createTarget(2, studentId, category, 9, false);
        Target target3 = createTarget(3, studentId, category, 10, false);

        when(targetDAO.findActiveByStudent(studentId)).thenReturn(Arrays.asList(target1, target2, target3));
        when(targetDAO.findById(anyInt())).thenAnswer(inv -> {
            int id = inv.getArgument(0);
            if (id == 1) return target1;
            if (id == 2) return target2;
            if (id == 3) return target3;
            return null;
        });
        when(targetDAO.update(any(Target.class))).thenReturn(true);

        // Mock streak starting at 0
        TargetAchievementStreak streak = new TargetAchievementStreak(studentId, 0, null, 0);
        when(streakDAO.findByStudentId(studentId)).thenReturn(streak);
        when(streakDAO.upsert(any(TargetAchievementStreak.class))).thenReturn(true);

        // Act
        targetService.checkAndAchieveTargets(studentId, category, newPi);

        // Assert
        // Verify all 3 targets achieved
        verify(targetDAO, times(3)).update(any(Target.class));

        // Verify streak updated 3 times (once per achievement)
        // Final streak should be 3, total points = 1+2+3 = 6
        ArgumentCaptor<TargetAchievementStreak> streakCaptor = ArgumentCaptor.forClass(TargetAchievementStreak.class);
        verify(streakDAO, times(3)).upsert(streakCaptor.capture());
        
        List<TargetAchievementStreak> capturedStreaks = streakCaptor.getAllValues();
        TargetAchievementStreak finalStreak = capturedStreaks.get(2);
        assertEquals(3, finalStreak.getCurrentStreak());
        assertEquals(6, finalStreak.getTotalPointsEarned());

        // Verify events: 3 TargetAchievedEvent + 3 TargetStreakUpdatedEvent
        verify(eventBusService, times(6)).publish(any());
    }

    // ==================== Test 13.7: Update Streak - Increment on Achievement ====================
    @Test
    @DisplayName("Test 13.7: Update streak - increment on achievement")
    void testUpdateStreak_IncrementOnAchievement() {
        // Arrange
        Integer studentId = 1;
        TargetAchievementStreak streak = new TargetAchievementStreak(studentId, 2, LocalDateTime.now(), 3);
        
        when(streakDAO.findByStudentId(studentId)).thenReturn(streak);
        when(streakDAO.upsert(any(TargetAchievementStreak.class))).thenReturn(true);

        // Act
        targetService.updateStreak(studentId, true);

        // Assert
        ArgumentCaptor<TargetAchievementStreak> streakCaptor = ArgumentCaptor.forClass(TargetAchievementStreak.class);
        verify(streakDAO, times(1)).upsert(streakCaptor.capture());
        
        TargetAchievementStreak updatedStreak = streakCaptor.getValue();
        assertEquals(3, updatedStreak.getCurrentStreak());
        assertEquals(6, updatedStreak.getTotalPointsEarned()); // 3 + 3
        assertNotNull(updatedStreak.getLastAchievementAt());

        // Verify event published
        verify(eventBusService, times(1)).publish(any(TargetStreakUpdatedEvent.class));
    }

    // ==================== Test 13.8: Update Streak - Reset on Degradation ====================
    @Test
    @DisplayName("Test 13.8: Update streak - reset on degradation")
    void testUpdateStreak_ResetOnDegradation() {
        // Arrange
        Integer studentId = 1;
        TargetAchievementStreak streak = new TargetAchievementStreak(studentId, 5, LocalDateTime.now(), 15);
        
        when(streakDAO.findByStudentId(studentId)).thenReturn(streak);
        when(streakDAO.upsert(any(TargetAchievementStreak.class))).thenReturn(true);

        // Act
        targetService.updateStreak(studentId, false);

        // Assert
        ArgumentCaptor<TargetAchievementStreak> streakCaptor = ArgumentCaptor.forClass(TargetAchievementStreak.class);
        verify(streakDAO, times(1)).upsert(streakCaptor.capture());
        
        TargetAchievementStreak updatedStreak = streakCaptor.getValue();
        assertEquals(0, updatedStreak.getCurrentStreak());
        assertEquals(15, updatedStreak.getTotalPointsEarned()); // Historical points preserved

        // Verify event published
        verify(eventBusService, times(1)).publish(any(TargetStreakUpdatedEvent.class));
    }

    // ==================== Test 13.9: Get Active Targets ====================
    @Test
    @DisplayName("Test 13.9: Get active targets returns only active targets")
    void testGetActiveTargets_ReturnsOnlyActive() {
        // Arrange
        Integer studentId = 1;
        
        Target active1 = createTarget(1, studentId, TopicCategory.NAHW, 6, false);
        Target active2 = createTarget(2, studentId, TopicCategory.ADAB, 8, false);
        Target active3 = createTarget(3, studentId, TopicCategory.NUSUS, 7, false);
        
        when(targetDAO.findActiveByStudent(studentId)).thenReturn(Arrays.asList(active1, active2, active3));

        // Act
        List<Target> activeTargets = targetService.getActiveTargets(studentId);

        // Assert
        assertEquals(3, activeTargets.size());
        verify(targetDAO, times(1)).findActiveByStudent(studentId);
    }

    // ==================== Test 13.10: Get Current Streak ====================
    @Test
    @DisplayName("Test 13.10: Get current streak returns correct value")
    void testGetCurrentStreak_ReturnsCorrectValue() {
        // Arrange
        Integer studentId = 1;
        TargetAchievementStreak streak = new TargetAchievementStreak(studentId, 4, LocalDateTime.now(), 10);
        
        when(streakDAO.findByStudentId(studentId)).thenReturn(streak);

        // Act
        int currentStreak = targetService.getCurrentStreak(studentId);

        // Assert
        assertEquals(4, currentStreak);
        verify(streakDAO, times(1)).findByStudentId(studentId);
    }

    // ==================== Test 13.11: Get Total Target Points ====================
    @Test
    @DisplayName("Test 13.11: Get total target points returns correct value")
    void testGetTotalTargetPoints_ReturnsCorrectValue() {
        // Arrange
        Integer studentId = 1;
        TargetAchievementStreak streak = new TargetAchievementStreak(studentId, 4, LocalDateTime.now(), 18);
        
        when(streakDAO.findByStudentId(studentId)).thenReturn(streak);

        // Act
        int totalPoints = targetService.getTotalTargetPoints(studentId);

        // Assert
        assertEquals(18, totalPoints);
        verify(streakDAO, times(1)).findByStudentId(studentId);
    }

    // ==================== Test 13.12: Has Active Targets - True ====================
    @Test
    @DisplayName("Test 13.12: Has active targets returns true when targets exist")
    void testHasActiveTargets_True() {
        // Arrange
        Integer studentId = 1;
        when(targetDAO.countActiveByStudent(studentId)).thenReturn(2);

        // Act
        boolean hasActive = targetService.hasActiveTargets(studentId);

        // Assert
        assertTrue(hasActive);
        verify(targetDAO, times(1)).countActiveByStudent(studentId);
    }

    // ==================== Test 13.13: Has Active Targets - False ====================
    @Test
    @DisplayName("Test 13.13: Has active targets returns false when no targets (default applies)")
    void testHasActiveTargets_False_DefaultTargetApplies() {
        // Arrange
        Integer studentId = 5;
        when(targetDAO.countActiveByStudent(studentId)).thenReturn(0);

        // Act
        boolean hasActive = targetService.hasActiveTargets(studentId);
        String defaultMessage = targetService.getDefaultTargetMessage(studentId);

        // Assert
        assertFalse(hasActive);
        assertEquals("Improve PI in any category", defaultMessage);
        verify(targetDAO, times(2)).countActiveByStudent(studentId); // Called twice: hasActiveTargets + getDefaultTargetMessage
    }

    // ==================== Additional Edge Case Tests ====================

    @Test
    @DisplayName("Generate targets with no degradation should do nothing")
    void testGenerateTargetsOnDegradation_NoDegradation_DoesNothing() {
        // Arrange
        Integer studentId = 1;
        TopicCategory category = TopicCategory.NAHW;
        int previousPi = 6;
        int currentPi = 6; // No change

        // Act
        targetService.generateTargetsOnDegradation(studentId, category, previousPi, currentPi);

        // Assert
        verify(targetDAO, never()).insert(any(Target.class));
        verify(streakDAO, never()).upsert(any(TargetAchievementStreak.class));
    }

    @Test
    @DisplayName("Achieve target with non-existent target ID returns false")
    void testAchieveTarget_NonExistentId_ReturnsFalse() {
        // Arrange
        Integer targetId = 999;
        when(targetDAO.findById(targetId)).thenReturn(null);

        // Act
        boolean result = targetService.achieveTarget(targetId);

        // Assert
        assertFalse(result);
        verify(targetDAO, never()).update(any(Target.class));
        verify(eventBusService, never()).publish(any());
    }

    @Test
    @DisplayName("Update streak creates new streak record if none exists")
    void testUpdateStreak_CreatesNewStreak_IfNotExists() {
        // Arrange
        Integer studentId = 10;
        when(streakDAO.findByStudentId(studentId)).thenReturn(null);
        when(streakDAO.upsert(any(TargetAchievementStreak.class))).thenReturn(true);

        // Act
        targetService.updateStreak(studentId, true);

        // Assert
        ArgumentCaptor<TargetAchievementStreak> streakCaptor = ArgumentCaptor.forClass(TargetAchievementStreak.class);
        verify(streakDAO, times(1)).upsert(streakCaptor.capture());
        
        TargetAchievementStreak newStreak = streakCaptor.getValue();
        assertEquals(studentId, newStreak.getStudentId());
        assertEquals(1, newStreak.getCurrentStreak());
        assertEquals(1, newStreak.getTotalPointsEarned());
    }

    @Test
    @DisplayName("Get active targets with no targets returns empty list")
    void testGetActiveTargets_NoTargets_ReturnsEmptyList() {
        // Arrange
        Integer studentId = 1;
        when(targetDAO.findActiveByStudent(studentId)).thenReturn(Collections.emptyList());

        // Act
        List<Target> activeTargets = targetService.getActiveTargets(studentId);

        // Assert
        assertTrue(activeTargets.isEmpty());
    }

    // ==================== Helper Methods ====================

    private Target createTarget(Integer targetId, Integer studentId, TopicCategory category, 
                               int targetValue, boolean isAchieved) {
        Target target = new Target();
        target.setTargetId(targetId);
        target.setStudentId(studentId);
        target.setCategory(category);
        target.setTargetPiValue(targetValue);
        target.setCreatedAt(LocalDateTime.now());
        target.setAchieved(isAchieved);
        target.setAchievedAt(isAchieved ? LocalDateTime.now() : null);
        return target;
    }
}