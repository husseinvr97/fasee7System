package com.studenttracker.util;

import com.studenttracker.dao.*;
import com.studenttracker.dao.impl.*;
import com.studenttracker.service.*;
import com.studenttracker.service.impl.*;

import java.util.HashMap;
import java.util.Map;


/**
 * ServiceLocator - Centralized service instance management.
 * 
 * <p><b>Design Pattern:</b> Service Locator + Factory Method</p>
 * 
 * <p><b>Responsibilities:</b></p>
 * <ul>
 *   <li>Lazy initialization of service instances</li>
 *   <li>Single source of truth for service dependencies</li>
 *   <li>Simplified testing with mock service injection</li>
 *   <li>Prevents "new" proliferation across codebase</li>
 * </ul>
 * 
 * <p><b>Usage Example - Production:</b></p>
 * <pre>
 * ServiceLocator locator = ServiceLocator.getInstance();
 * UserService userService = locator.getUserService();
 * StudentService studentService = locator.getStudentService();
 * </pre>
 * 
 * <p><b>Usage Example - Testing:</b></p>
 * <pre>
 * // Inject mock service for testing
 * UserService mockUserService = mock(UserService.class);
 * ServiceLocator.getInstance().registerService(UserService.class, mockUserService);
 * 
 * // Reset after tests
 * ServiceLocator.resetInstance();
 * </pre>
 * 
 * <p><b>SOLID Principles:</b></p>
 * <ul>
 *   <li><b>Single Responsibility:</b> Manages service lifecycle only</li>
 *   <li><b>Open/Closed:</b> Easy to add new services without modifying clients</li>
 *   <li><b>Dependency Inversion:</b> Clients depend on interfaces, not implementations</li>
 * </ul>
 * 
 * @author fasee7System
 * @version 1.0.0
 * @since 2026-01-28
 */
public class ServiceLocator {
    
    // ==================== SINGLETON PATTERN ====================
    
    private static ServiceLocator instance;
    
    /**
     * Private constructor to enforce singleton pattern.
     */
    private ServiceLocator() {
        this.serviceCache = new HashMap<>();
    }
    
    /**
     * Gets the singleton instance of ServiceLocator.
     * Thread-safe lazy initialization.
     * 
     * @return The ServiceLocator instance
     */
    public static synchronized ServiceLocator getInstance() {
        if (instance == null) {
            instance = new ServiceLocator();
        }
        return instance;
    }
    
    /**
     * Resets the singleton instance.
     * <b>WARNING:</b> This method should only be used for testing purposes.
     */
    public static synchronized void resetInstance() {
        if (instance != null) {
            instance.serviceCache.clear();
            instance = null;
        }
    }
    
    // ==================== SERVICE CACHE ====================
    
    /**
     * Cache to store initialized service instances.
     * Key: Service interface class
     * Value: Service instance
     */
    private final Map<Class<?>, Object> serviceCache;
    
    /**
     * Registers a service instance (primarily for testing with mocks).
     * 
     * @param serviceClass The service interface class
     * @param serviceInstance The service instance to register
     * @param <T> The service type
     */
    public <T> void registerService(Class<T> serviceClass, T serviceInstance) {
        serviceCache.put(serviceClass, serviceInstance);
    }
    
    /**
     * Clears all cached services.
     * Forces re-initialization on next access.
     */
    public void clearCache() {
        serviceCache.clear();
    }
    
    // ==================== DAO INSTANCES ====================
    
    /**
     * Gets or creates UserDAO instance.
     * 
     * @return UserDAO implementation
     */
    public UserDAO getUserDAO() {
        return getOrCreate(UserDAO.class, () -> new UserDAOImpl());
    }
    
    /**
     * Gets or creates StudentDAO instance.
     * 
     * @return StudentDAO implementation
     */
    public StudentDAO getStudentDAO() {
        return getOrCreate(StudentDAO.class, () -> new StudentDAOImpl());
    }

    /**
     * Gets or creates MissionDraftDAO instance.
     * 
     * @return MissionDraftDAO implementation
     */
    public MissionDraftDAO getMissionDraftDAO() {
        return getOrCreate(MissionDraftDAO.class, () -> new MissionDraftDAOImpl());
    }

    /**
     * Gets or creates ConsecutivityDAO instance.
     * 
     * @return ConsecutivityDAO implementation
     */

    public ConsecutivityTrackingDAO getConsecutivityDAO() {
        return getOrCreate(ConsecutivityTrackingDAO.class, () -> new ConsecutivityTrackingDAOImpl());
    }
    
    /**
     * Gets or creates LessonDAO instance.
     * 
     * @return LessonDAO implementation
     */
    public LessonDAO getLessonDAO() {
        return getOrCreate(LessonDAO.class, () -> new LessonDAOImpl());
    }
    
    /**
     * Gets or creates MissionDAO instance.
     * 
     * @return MissionDAO implementation
     */
    public MissionDAO getMissionDAO() {
        return getOrCreate(MissionDAO.class, () -> new MissionDAOImpl());
    }

    /**
     * Gets or creates MissionDAO instance.
     * 
     * @return BehavioralIncidentDAO implementation
     */

    public BehavioralIncidentDAO getBehavioralIncidentDAO() {
        return getOrCreate(BehavioralIncidentDAO.class, () -> new BehavioralIncidentDAOImpl());
    }
    
    /**
     * Gets or creates WarningDAO instance.
     * 
     * @return WarningDAO implementation
     */
    public WarningDAO getWarningDAO() {
        return getOrCreate(WarningDAO.class, () -> new WarningDAOImpl());
    }
    
    /**
     * Gets or creates AttendanceDAO instance.
     * 
     * @return AttendanceDAO implementation
     */
    public AttendanceDAO getAttendanceDAO() {
        return getOrCreate(AttendanceDAO.class, () -> new AttendanceDAOImpl());
    }
    
    /**
     * Gets or creates HomeworkDAO instance.
     * 
     * @return HomeworkDAO implementation
     */
    public HomeworkDAO getHomeworkDAO() {
        return getOrCreate(HomeworkDAO.class, () -> new HomeworkDAOImpl());
    }
    
    /**
     * Gets or creates QuizDAO instance.
     * 
     * @return QuizDAO implementation
     */
    public QuizDAO getQuizDAO() {
        return getOrCreate(QuizDAO.class, () -> new QuizDAOImpl());
    }

    /**
     * Gets or creates LessonTopicDAO instance.
     * 
     * @return LessonTopicDAO implementation
     */
    public LessonTopicDAO getLessonTopicDAO() {
        return getOrCreate(LessonTopicDAO.class, () -> new LessonTopicDAOImpl());
    }

    /**
     * Gets or creates UpdateRequestDAO instance.
     * 
     * @return UpdateRequestDAO implementation
     */
    public UpdateRequestDAO getUpdateRequestDAO() {
        return getOrCreate(UpdateRequestDAO.class, () -> new UpdateRequestDAOImpl());
    }

    /**
     * Gets or creates QuizQuestionDAO instance.
     * 
     * @return QuizQuestionDAO implementation
     */
    public QuizQuestionDAO getQuizQuestionDAO() {
        return getOrCreate(QuizQuestionDAO.class, () -> new QuizQuestionDAOImpl());
    }

    /**
     * Gets or creates QuizScoreDAO instance.
     * 
     * @return QuizScoreDAO implementation
     */
    public QuizScoreDAO getQuizScoreDAO() {
        return getOrCreate(QuizScoreDAO.class, () -> new QuizScoreDAOImpl());
    }

    /**
     * Gets or creates QuizCategoryTotalDAO instance.
     * 
     * @return QuizCategoryTotalDAO implementation
     */
    public QuizCategoryTotalDAO getQuizCategoryTotalDAO() {
        return getOrCreate(QuizCategoryTotalDAO.class, () -> new QuizCategoryTotalDAOImpl());
    }

    public <T> T getClassDAO(Class<T> clazz) {
        return getOrCreate(clazz, () ->{
            try {
                return clazz.getDeclaredConstructor().newInstance();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
    }

    
    
    // ==================== SERVICE INSTANCES ====================
    
    /**
     * Gets or creates UserService instance.
     * 
     * @return UserService implementation
     */
    public UserService getUserService() {
        return getOrCreate(UserService.class, () -> new UserServiceImpl(getUserDAO()));
    }

    /**
     * Gets or creates QuizService instance.
     * 
     * @return QuizService implementation
     */
    public QuizService getQuizService() {
        return getOrCreate(QuizService.class, () -> new QuizServiceImpl(getQuizDAO(),
        getQuizQuestionDAO(),
        getQuizScoreDAO(),
        getQuizCategoryTotalDAO(),
        getAttendanceDAO(),
        getUserDAO(),
        EventBusService.getInstance()
    ));
    }

    /**
     * Gets or creates UserService instance.
     * 
     * @return ConsecutivityService implementation
     */

    public ConsecutivityTrackingService getConsecutivityService() {
        return getOrCreate(ConsecutivityTrackingService.class, () -> new ConsecutivityTrackingServiceImpl(
            getConsecutivityDAO(),
            getBehavioralIncidentDAO(),
            EventBusService.getInstance()
        ));
    }

    /**
     * Gets or creates BehavioralIncidentService instance.
     * 
     * @return BehavioralIncidentService implementation
     */
    public BehavioralIncidentService getBehavioralIncidentService() {
        return getOrCreate(BehavioralIncidentService.class, () -> new BehavioralIncidentServiceImpl(
            getBehavioralIncidentDAO(),
            getStudentDAO(),
            getAttendanceDAO(),
            getUserDAO(),
            getUpdateRequestDAO(),
            getLessonDAO(),
            EventBusService.getInstance()
        ));
    }
    
    /**
     * Gets or creates StudentService instance.
     * 
     * @return StudentService implementation
     */
    public StudentService getStudentService() {
        return getOrCreate(StudentService.class, () -> new StudentServiceImpl(
            getStudentDAO(),
            EventBusService.getInstance(),
            getConsecutivityService()
        ));
    }
    
    /**
     * Gets or creates LessonService instance.
     * 
     * @return LessonService implementation
     */
    public LessonService getLessonService() {
        return getOrCreate(LessonService.class, () -> new LessonServiceImpl(
            getLessonDAO(),
            getLessonTopicDAO(),
            getQuizDAO(),
            getAttendanceDAO(),
            getHomeworkDAO(),
            getUserDAO(),
            EventBusService.getInstance()
        ));
    }
    
    /**
     * Gets or creates MissionService instance.
     * 
     * @return MissionService implementation
     */
    public MissionService getMissionService() {
        return getOrCreate(MissionService.class, () -> new MissionServiceImpl(
            getMissionDAO(),
            getMissionDraftDAO(),
            getUserDAO(),
            EventBusService.getInstance()
        ));
    }
    
    /**
     * Gets or creates WarningService instance.
     * 
     * @return WarningService implementation
     */
    public WarningService getWarningService() {
        return getOrCreate(WarningService.class, () -> new WarningServiceImpl(
            getWarningDAO(),
            getConsecutivityService(),
            getBehavioralIncidentService(),
            EventBusService.getInstance()
        ));
    }

    /**
     * Gets or creates HomeworkService instance.
     * 
     * @return HomeworkService implementation
     */
    public HomeworkService getHomeworkService() {
        return getOrCreate(HomeworkService.class, () -> new HomeworkServiceImpl(
            getClassDAO(HomeworkDAOImpl.class),
            getClassDAO(StudentDAOImpl.class),
            getClassDAO(AttendanceDAOImpl.class),
            EventBusService.getInstance()
        ));
    }

    /**
     * Gets or creates TargetService instance.
     * 
     * @return TargetService implementation
     */
    public TargetService getTargetService() {
        return getOrCreate(TargetService.class, () -> new TargetServiceImpl(
            getClassDAO(TargetDAOImpl.class),
            getClassDAO(TargetAchievementStreakDAOImpl.class),
            EventBusService.getInstance()
        ));
    }

    /**
     * Gets or creates Fasee7Service instance.
     * 
     * @return Fasee7Service implementation
     */
    public Fasee7TableService getFasee7TableService() {
        return getOrCreate(Fasee7TableService.class, () -> new Fasee7TableServiceImpl(
            getClassDAO(Fasee7PointsDAOImpl.class),
            getClassDAO(Fasee7SnapshotDAOImpl.class),
            getClassDAO(QuizScoreDAOImpl.class),
            getClassDAO(AttendanceDAOImpl.class),
            getClassDAO(HomeworkDAOImpl.class),
            getClassDAO(TargetAchievementStreakDAOImpl.class),
            getClassDAO(StudentDAOImpl.class),
            EventBusService.getInstance()
        ));
    }
    /**
     * Gets or creates AttendanceService instance.
     * 
     * @return AtendanceService implementation
     */
    public AttendanceService getAttendanceService() {
        return getOrCreate(AttendanceService.class, () -> new AttendanceServiceImpl(
            getAttendanceDAO(),
            getStudentDAO(),
            EventBusService.getInstance()
        ));
    }
    /**
     * Gets or creates UpdateRequestOrchestratorService instance.
     * 
     * @return UpdateRequestOrchestratorService implementation
     */
    public UpdateRequestOrchestratorService getUpdateRequestService() {
        return getOrCreate(UpdateRequestOrchestratorService.class, 
            () -> new UpdateRequestOrchestratorServiceImpl(
                getUpdateRequestDAO(),
                getAttendanceService(),
                getQuizService(),
                getStudentService(),
                getHomeworkService(),
                getBehavioralIncidentService(),
                getUserService(),
                getConsecutivityService(),
                getWarningService(),
                getTargetService(),
                getFasee7TableService(),
                EventBusService.getInstance(),
                DatabaseConnection.getInstance()
            )
        );
    }

    /**
     * Gets or creates RecentactivityService instance.
     * 
     * @return RecentactivityService implementation
     */
    public RecentActivityService getRecentActivityService() {
        return getOrCreate(RecentActivityService.class, () -> new RecentActivityServiceImpl(
            getClassDAO(RecentActivityDAOImpl.class),
            getClassDAO(MissionDAOImpl.class),
            EventBusService.getInstance(),
            getClassDAO(QuizDAOImpl.class)
        ));
    }
    
    // Note: RecentActivityService will be added in Stage 3
    
    // ==================== HELPER METHOD ====================
    
    /**
     * Generic method to get or create a service instance.
     * Implements lazy initialization with caching.
     * 
     * @param serviceClass The service interface class
     * @param factory Factory function to create the service
     * @param <T> The service type
     * @return The service instance
     */
    @SuppressWarnings("unchecked")
    private synchronized <T> T getOrCreate(Class<T> serviceClass, ServiceFactory<T> factory) {
        // Check if already cached
        if (serviceCache.containsKey(serviceClass)) {
            return (T) serviceCache.get(serviceClass);
        }
        
        // Create new instance
        T service = factory.create();
        
        // Cache it
        serviceCache.put(serviceClass, service);
        
        return service;
    }
    
    /**
     * Functional interface for service creation.
     * Allows lambda expressions for factory methods.
     */
    @FunctionalInterface
    private interface ServiceFactory<T> {
        T create();
    }
}