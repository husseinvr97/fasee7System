package com.studenttracker.service.impl;

import com.google.common.eventbus.Subscribe;
import com.studenttracker.dao.*;
import com.studenttracker.model.Attendance;
import com.studenttracker.model.Fasee7Points;
import com.studenttracker.model.Fasee7Snapshot;
import com.studenttracker.model.Homework;
import com.studenttracker.model.QuizScore;
import com.studenttracker.model.Student;
import com.studenttracker.service.EventBusService;
import com.studenttracker.service.Fasee7TableService;
import com.studenttracker.service.event.*;
import com.studenttracker.service.impl.helpers.Fasee7TableServiceImplHelpers;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Implementation of Fasee7TableService.
 * Manages Fasee7 points calculation, rankings, and snapshots.
 */
public class Fasee7TableServiceImpl implements Fasee7TableService {
    
    private final Fasee7PointsDAO pointsDAO;
    private final Fasee7SnapshotDAO snapshotDAO;
    private final QuizScoreDAO quizScoreDAO;
    private final AttendanceDAO attendanceDAO;
    private final HomeworkDAO homeworkDAO;
    private final TargetAchievementStreakDAO streakDAO;
    private final StudentDAO studentDAO;
    private final EventBusService eventBus;
    
    /**
     * Constructor with dependency injection.
     * Registers this service as an event subscriber.
     */
    public Fasee7TableServiceImpl(Fasee7PointsDAO pointsDAO,
                                  Fasee7SnapshotDAO snapshotDAO,
                                  QuizScoreDAO quizScoreDAO,
                                  AttendanceDAO attendanceDAO,
                                  HomeworkDAO homeworkDAO,
                                  TargetAchievementStreakDAO streakDAO,
                                  StudentDAO studentDAO,
                                  EventBusService eventBus) {
        this.pointsDAO = pointsDAO;
        this.snapshotDAO = snapshotDAO;
        this.quizScoreDAO = quizScoreDAO;
        this.attendanceDAO = attendanceDAO;
        this.homeworkDAO = homeworkDAO;
        this.streakDAO = streakDAO;
        this.studentDAO = studentDAO;
        this.eventBus = eventBus;
        
        // Register as event subscriber
        this.eventBus.register(this);
    }
    
    
    // ========== Event Subscribers ==========
    
    /**
     * Handles QuizGradingCompletedEvent.
     * Updates quiz points for all students who took this quiz.
     */
    @Subscribe
    public void onQuizGradingCompleted(QuizGradingCompletedEvent event) {
        // Get all students who took this quiz
        List<QuizScore> scores = quizScoreDAO.findByQuizId(event.getQuizId());
        
        // Get unique student IDs
        Set<Integer> studentIds = scores.stream()
            .map(QuizScore::getStudentId)
            .collect(Collectors.toSet());
        
        // Update points for each student
        for (Integer studentId : studentIds) {
            updateQuizPoints(studentId);
        }
    }
    
    /**
     * Handles AttendanceBatchCompletedEvent.
     * Updates attendance points for all students in this lesson.
     */
    @Subscribe
    public void onAttendanceBatchCompleted(AttendanceBatchCompletedEvent event) {
        // Get all attendance records for this lesson
        List<Attendance> attendances = attendanceDAO.findByLessonId(event.getLessonId());
        
        // Get unique student IDs
        Set<Integer> studentIds = attendances.stream()
            .map(Attendance::getStudentId)
            .collect(Collectors.toSet());
        
        // Update points for each student
        for (Integer studentId : studentIds) {
            updateAttendancePoints(studentId);
        }
    }
    
    /**
     * Handles HomeworkBatchCompletedEvent.
     * Updates homework points for all students in this lesson.
     */
    @Subscribe
    public void onHomeworkBatchCompleted(HomeworkBatchCompletedEvent event) {
        // Get all homework records for this lesson
        List<Homework> homeworks = homeworkDAO.findByLessonId(event.getLessonId());
        
        // Get unique student IDs
        Set<Integer> studentIds = homeworks.stream()
            .map(Homework::getStudentId)
            .collect(Collectors.toSet());
        
        // Update points for each student
        for (Integer studentId : studentIds) {
            updateHomeworkPoints(studentId);
        }
    }
    
    /**
     * Handles TargetAchievedEvent.
     * Updates target points when a target is achieved.
     */
    @Subscribe
    public void onTargetAchieved(TargetAchievedEvent event) {
        updateTargetPoints(event.getStudentId());
    }
    
    /**
     * Handles StudentRegisteredEvent.
     * Initializes points for new students.
     */
    @Subscribe
    public void onStudentRegistered(StudentRegisteredEvent event) {
        initializePoints(event.getStudentId());
    }
    
    
    // ========== Points Calculation ==========
    
    @Override
    public void recalculatePoints(Integer studentId) {
        // Step 1: Calculate all point components
        Double quizPoints = Fasee7TableServiceImplHelpers.calculateQuizPoints(studentId, quizScoreDAO);
        int attendancePoints = Fasee7TableServiceImplHelpers.calculateAttendancePoints(studentId, attendanceDAO);
        int homeworkPoints = Fasee7TableServiceImplHelpers.calculateHomeworkPoints(studentId, homeworkDAO);
        int targetPoints = Fasee7TableServiceImplHelpers.calculateTargetPoints(studentId, streakDAO);
        
        // Step 2: Create or update Fasee7Points object
        Fasee7Points points = pointsDAO.findByStudentId(studentId);
        
        if (points == null) {
            points = new Fasee7Points();
            points.setStudentId(studentId);
        }
        
        points.setQuizPoints(quizPoints);
        points.setAttendancePoints(attendancePoints);
        points.setHomeworkPoints(homeworkPoints);
        points.setTargetPoints(targetPoints);
        
        // Step 3: Recalculate total
        points.recalculateTotal();
        
        // Step 4: Upsert to database
        pointsDAO.upsert(points);
        
        // Step 5: Publish event
        Fasee7PointsUpdatedEvent event = new Fasee7PointsUpdatedEvent(
            studentId,
            points.getQuizPoints(),
            points.getAttendancePoints(),
            points.getHomeworkPoints(),
            points.getTargetPoints(),
            points.getTotalPoints()
        );
        eventBus.publish(event);
    }
    
    @Override
    public void updateQuizPoints(Integer studentId) {
        // Step 1: Get current points record
        Fasee7Points points = pointsDAO.findByStudentId(studentId);
        if (points == null) {
            // Initialize if doesn't exist
            initializePoints(studentId);
            points = pointsDAO.findByStudentId(studentId);
        }
        
        // Step 2: Calculate new quiz points
        Double quizPoints = Fasee7TableServiceImplHelpers.calculateQuizPoints(studentId, quizScoreDAO);
        
        // Step 3: Update and recalculate
        points.setQuizPoints(quizPoints);
        points.recalculateTotal();
        
        // Step 4: Update in database
        pointsDAO.update(points);
        
        // Step 5: Publish event
        Fasee7PointsUpdatedEvent event = new Fasee7PointsUpdatedEvent(
            studentId,
            points.getQuizPoints(),
            points.getAttendancePoints(),
            points.getHomeworkPoints(),
            points.getTargetPoints(),
            points.getTotalPoints()
        );
        eventBus.publish(event);
    }
    
    @Override
    public void updateAttendancePoints(Integer studentId) {
        // Step 1: Get current points record
        Fasee7Points points = pointsDAO.findByStudentId(studentId);
        if (points == null) {
            initializePoints(studentId);
            points = pointsDAO.findByStudentId(studentId);
        }
        
        // Step 2: Calculate new attendance points
        int attendancePoints = Fasee7TableServiceImplHelpers.calculateAttendancePoints(studentId, attendanceDAO);
        
        // Step 3: Update and recalculate
        points.setAttendancePoints(attendancePoints);
        points.recalculateTotal();
        
        // Step 4: Update in database
        pointsDAO.update(points);
        
        // Step 5: Publish event
        Fasee7PointsUpdatedEvent event = new Fasee7PointsUpdatedEvent(
            studentId,
            points.getQuizPoints(),
            points.getAttendancePoints(),
            points.getHomeworkPoints(),
            points.getTargetPoints(),
            points.getTotalPoints()
        );
        eventBus.publish(event);
    }
    
    @Override
    public void updateHomeworkPoints(Integer studentId) {
        // Step 1: Get current points record
        Fasee7Points points = pointsDAO.findByStudentId(studentId);
        if (points == null) {
            initializePoints(studentId);
            points = pointsDAO.findByStudentId(studentId);
        }
        
        // Step 2: Calculate new homework points
        int homeworkPoints = Fasee7TableServiceImplHelpers.calculateHomeworkPoints(studentId, homeworkDAO);
        
        // Step 3: Update and recalculate
        points.setHomeworkPoints(homeworkPoints);
        points.recalculateTotal();
        
        // Step 4: Update in database
        pointsDAO.update(points);
        
        // Step 5: Publish event
        Fasee7PointsUpdatedEvent event = new Fasee7PointsUpdatedEvent(
            studentId,
            points.getQuizPoints(),
            points.getAttendancePoints(),
            points.getHomeworkPoints(),
            points.getTargetPoints(),
            points.getTotalPoints()
        );
        eventBus.publish(event);
    }
    
    @Override
    public void updateTargetPoints(Integer studentId) {
        // Step 1: Get current points record
        Fasee7Points points = pointsDAO.findByStudentId(studentId);
        if (points == null) {
            initializePoints(studentId);
            points = pointsDAO.findByStudentId(studentId);
        }
        
        // Step 2: Calculate new target points
        int targetPoints = Fasee7TableServiceImplHelpers.calculateTargetPoints(studentId, streakDAO);
        
        // Step 3: Update and recalculate
        points.setTargetPoints(targetPoints);
        points.recalculateTotal();
        
        // Step 4: Update in database
        pointsDAO.update(points);
        
        // Step 5: Publish event
        Fasee7PointsUpdatedEvent event = new Fasee7PointsUpdatedEvent(
            studentId,
            points.getQuizPoints(),
            points.getAttendancePoints(),
            points.getHomeworkPoints(),
            points.getTargetPoints(),
            points.getTotalPoints()
        );
        eventBus.publish(event);
    }
    
    
    // ========== Rankings ==========
    
    @Override
    public List<Fasee7Points> getRankings() {
        // Step 1: Get all points ordered by total
        List<Fasee7Points> allPoints = pointsDAO.findAllOrderedByTotal();
        
        // Step 2: Get active students
        List<Student> activeStudents = studentDAO.findByStatus(Student.StudentStatus.ACTIVE);
        Set<Integer> activeStudentIds = activeStudents.stream()
            .map(Student::getStudentId)
            .collect(Collectors.toSet());
        
        // Step 3: Filter by active students only
        List<Fasee7Points> filteredPoints = allPoints.stream()
            .filter(p -> activeStudentIds.contains(p.getStudentId()))
            .collect(Collectors.toList());
        
        // Step 4: Apply tie-breaking
        List<Fasee7Points> ranked = Fasee7TableServiceImplHelpers.applyTieBreaking(filteredPoints, studentDAO);

        List<Student> top10 = ranked.subList(0, 10).stream()
            .map(Fasee7Points::getStudentId)
            .map(studentDAO::findById)
            .collect(Collectors.toList());
        
        Fasee7RankingsChangedEvent event = new Fasee7RankingsChangedEvent(
top10,
LocalDateTime.now()
);
eventBus.publish(event);
    return ranked;
}

@Override
public int getStudentRank(Integer studentId) {
    List<Fasee7Points> rankings = getRankings();
    
    for (int i = 0; i < rankings.size(); i++) {
        if (rankings.get(i).getStudentId().equals(studentId)) {
            return i + 1; // 1-based rank
        }
    }
    
    return -1; // Not found
}

@Override
public List<Fasee7Points> getTopN(int limit) {
    List<Fasee7Points> rankings = getRankings();
    
    return rankings.stream()
        .limit(limit)
        .collect(Collectors.toList());
}

@Override
public Fasee7Points getStudentPoints(Integer studentId) {
    return pointsDAO.findByStudentId(studentId);
}


// ========== Snapshots ==========

@Override
public Integer createSnapshot(LocalDate snapshotDate, Integer createdBy) {
    // Step 1: Get current rankings
    List<Fasee7Points> rankings = getRankings();
    
    // Step 2: Serialize to JSON
    String jsonData = Fasee7TableServiceImplHelpers.serializeRankings(rankings);
    
    // Step 3: Create snapshot object
    Fasee7Snapshot snapshot = new Fasee7Snapshot();
    snapshot.setSnapshotDate(snapshotDate);
    snapshot.setSnapshotData(jsonData);
    snapshot.setCreatedAt(LocalDateTime.now());
    
    // Step 4: Insert into database
    Integer snapshotId = snapshotDAO.insert(snapshot);
    
    return snapshotId;
}

@Override
public Fasee7Snapshot getSnapshot(LocalDate date) {
    return snapshotDAO.findByDate(date);
}

@Override
public Fasee7Snapshot getLatestSnapshot() {
    return snapshotDAO.findLatest();
}

@Override
public List<Fasee7Snapshot> getAllSnapshots() {
    return snapshotDAO.findAllOrderedByDate();
}


// ========== Comparison ==========

@Override
public Map<Integer, Integer> compareRankings(LocalDate date1, LocalDate date2) {
    // Step 1: Get both snapshots
    Fasee7Snapshot snapshot1 = snapshotDAO.findByDate(date1);
    Fasee7Snapshot snapshot2 = snapshotDAO.findByDate(date2);
    
    if (snapshot1 == null || snapshot2 == null) {
        return new HashMap<>(); // Return empty if either snapshot not found
    }
    
    // Step 2: Deserialize rankings
    Map<Integer, Integer> rank1Map = Fasee7TableServiceImplHelpers.deserializeRankings(
        snapshot1.getSnapshotData()
    );
    Map<Integer, Integer> rank2Map = Fasee7TableServiceImplHelpers.deserializeRankings(
        snapshot2.getSnapshotData()
    );
    
    // Step 3: Build comparison
    return Fasee7TableServiceImplHelpers.buildRankComparison(rank1Map, rank2Map);
}


// ========== Statistics ==========

@Override
public Double getAveragePoints() {
    List<Fasee7Points> rankings = getRankings();
    
    if (rankings.isEmpty()) {
        return 0.0;
    }
    
    Double sum = 0.0;
    for (Fasee7Points points : rankings) {
        sum += points.getTotalPoints();
    }
    
    return sum / rankings.size();
}

@Override
public Double getHighestPoints() {
    List<Fasee7Points> rankings = getRankings();
    
    if (rankings.isEmpty()) {
        return 0.0;
    }
    
    return rankings.get(0).getTotalPoints();
}


// ========== Initialization ==========

@Override
public void initializePoints(Integer studentId) {
    // Step 1: Create Fasee7Points with all zeros
    Fasee7Points points = new Fasee7Points();
    points.setStudentId(studentId);
    points.setQuizPoints(0.0);
    points.setAttendancePoints(0);
    points.setHomeworkPoints(0);
    points.setTargetPoints(0);
    points.setTotalPoints(0.0);
    points.setLastUpdated(LocalDateTime.now());
    
    // Step 2: Insert into database
    pointsDAO.insert(points);
    
    // Step 3: Publish event
    Fasee7PointsUpdatedEvent event = new Fasee7PointsUpdatedEvent(
        studentId,
        points.getQuizPoints(),
        points.getAttendancePoints(),
        points.getHomeworkPoints(),
        points.getTargetPoints(),
        points.getTotalPoints()
    );
    eventBus.publish(event);
}
}