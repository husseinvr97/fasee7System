package com.studenttracker.controller.student.tabs;

import com.google.common.eventbus.Subscribe;
import com.studenttracker.controller.BaseController;
import com.studenttracker.model.Lesson;
import com.studenttracker.model.LessonTopic;
import com.studenttracker.service.*;
import com.studenttracker.service.event.*;
import com.studenttracker.util.*;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 * Controller for the Student Lessons Tab.
 * Displays paginated list of lessons with student-specific data (attendance, homework, quiz scores).
 * Supports real-time updates via EventBus and advanced filtering/pagination.
 * 
 * <p><b>Design Patterns Used:</b></p>
 * <ul>
 *   <li>Observer Pattern - EventBus subscriptions for real-time updates</li>
 *   <li>Template Method - Extends BaseController lifecycle</li>
 *   <li>Facade Pattern - Simplifies interaction with multiple services</li>
 * </ul>
 * 
 * <p><b>SOLID Principles:</b></p>
 * <ul>
 *   <li>SRP - Single responsibility: manage lessons tab UI</li>
 *   <li>DIP - Depends on service interfaces, not implementations</li>
 *   <li>OCP - Open for extension (new filters), closed for modification</li>
 * </ul>
 * 
 * @author fasee7System
 * @version 1.0.0
 * @since 2026-01-30
 */
public class StudentLessonsTabController extends BaseController {
    
    private static final Logger LOGGER = Logger.getLogger(StudentLessonsTabController.class.getName());
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("MMM dd, yyyy");
    private static final int DEFAULT_PAGE_SIZE = 20;
    
    // ==================== UI COMPONENTS ====================
    
    @FXML private DatePicker dateFromPicker;
    @FXML private DatePicker dateToPicker;
    @FXML private ComboBox<String> filterCombo;
    @FXML private TableView<LessonRecord> lessonsTable;
    @FXML private TableColumn<LessonRecord, String> dateColumn;
    @FXML private TableColumn<LessonRecord, String> topicsColumn;
    @FXML private TableColumn<LessonRecord, String> attendanceColumn;
    @FXML private TableColumn<LessonRecord, String> homeworkColumn;
    @FXML private TableColumn<LessonRecord, String> quizColumn;
    @FXML private TableColumn<LessonRecord, Void> actionsColumn;
    @FXML private Button firstPageBtn;
    @FXML private Button prevPageBtn;
    @FXML private Label pageLabel;
    @FXML private Button nextPageBtn;
    @FXML private Button lastPageBtn;
    @FXML private ComboBox<String> pageSizeCombo;
    
    // ==================== DATA COLLECTIONS ====================
    
    private int studentId;
    private ObservableList<LessonRecord> allLessons = FXCollections.observableArrayList();
    private ObservableList<LessonRecord> filteredLessons = FXCollections.observableArrayList();
    private ObservableList<LessonRecord> currentPage = FXCollections.observableArrayList();
    
    // ==================== PAGINATION STATE ====================
    
    private int currentPageNumber = 1;
    private int pageSize = DEFAULT_PAGE_SIZE;
    private int totalPages = 1;
    
    // ==================== SERVICES ====================
    
    private final LessonService lessonService;
    private final AttendanceService attendanceService;
    private final HomeworkService homeworkService;
    private final QuizService quizService;
    private final EventBusService eventBus;
    
    // ==================== CONSTRUCTOR ====================
    
    /**
     * Constructor - initializes services via ServiceLocator.
     * Follows Dependency Inversion Principle by depending on interfaces.
     */
    public StudentLessonsTabController() {
        super();
        ServiceLocator services = ServiceLocator.getInstance();
        this.lessonService = services.getLessonService();
        this.attendanceService = services.getAttendanceService();
        this.homeworkService = services.getHomeworkService();
        this.quizService = services.getQuizService();
        this.eventBus = EventBusService.getInstance();
        
        LOGGER.info("StudentLessonsTabController created");
    }
    
    // ==================== LIFECYCLE METHODS ====================
    
    /**
     * Initialize method - called by JavaFX after FXML injection.
     * Sets up table columns, bindings, and event subscriptions.
     */
    @Override
    public void initialize() {
        super.initialize();
        
        try {
            // Setup table columns
            setupTableColumns();
            
            // Setup default values
            filterCombo.setValue("All");
            pageSizeCombo.setValue(String.valueOf(DEFAULT_PAGE_SIZE));
            
            // Bind table to current page
            lessonsTable.setItems(currentPage);
            
            // Subscribe to events for real-time updates
            eventBus.register(this);
            
            LOGGER.info("StudentLessonsTabController initialized successfully");
            
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error during initialization", e);
            showError("Failed to initialize lessons tab: " + e.getMessage());
        }
    }
    
    /**
     * Cleanup method - called before controller is destroyed.
     * Unregisters from EventBus to prevent memory leaks.
     */
    @Override
    public void cleanup() {
        try {
            eventBus.unregister(this);
            LOGGER.info("StudentLessonsTabController cleaned up");
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Error during cleanup", e);
        }
        super.cleanup();
    }
    
    // ==================== PUBLIC API ====================
    
    /**
     * Set the student ID and load lessons.
     * Called by parent controller to initialize tab with student data.
     * 
     * @param studentId The ID of the student whose lessons to display
     */
    public void setStudentId(int studentId) {
        this.studentId = studentId;
        loadLessons();
        LOGGER.info("Student ID set to: " + studentId);
    }
    
    // ==================== SETUP METHODS ====================
    
    /**
     * Setup table columns with cell value factories and custom cell factories.
     * Implements presentation logic for displaying lesson data.
     */
    private void setupTableColumns() {
        // Date column
        dateColumn.setCellValueFactory(cellData -> 
            new SimpleStringProperty(cellData.getValue().getDate().format(DATE_FORMATTER))
        );
        
        // Topics column
        topicsColumn.setCellValueFactory(cellData -> 
            new SimpleStringProperty(cellData.getValue().getTopics())
        );
        
        // Attendance column with color coding
        attendanceColumn.setCellValueFactory(cellData -> 
            new SimpleStringProperty(cellData.getValue().getAttendanceStatus())
        );
        attendanceColumn.setCellFactory(column -> new TableCell<LessonRecord, String>() {
            @Override
            protected void updateItem(String status, boolean empty) {
                super.updateItem(status, empty);
                if (empty || status == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(status);
                    if ("PRESENT".equals(status)) {
                        setStyle("-fx-text-fill: #28a745; -fx-font-weight: bold;");
                    } else if ("ABSENT".equals(status)) {
                        setStyle("-fx-text-fill: #dc3545; -fx-font-weight: bold;");
                    } else {
                        setStyle("");
                    }
                }
            }
        });
        
        // Homework column
        homeworkColumn.setCellValueFactory(cellData -> 
            new SimpleStringProperty(cellData.getValue().getHomeworkStatus())
        );
        
        // Quiz score column
        quizColumn.setCellValueFactory(cellData -> 
            new SimpleStringProperty(cellData.getValue().getQuizScore())
        );
        
        // Actions column - View button
        actionsColumn.setCellFactory(column -> new TableCell<LessonRecord, Void>() {
            private final Button viewButton = new Button("View");
            
            {
                viewButton.getStyleClass().add("action-button");
                viewButton.setOnAction(e -> {
                    LessonRecord record = getTableView().getItems().get(getIndex());
                    handleViewLesson(record.getLessonId());
                });
            }
            
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : viewButton);
            }
        });
        
        LOGGER.fine("Table columns configured");
    }
    
    // ==================== DATA LOADING ====================
    
    /**
     * Load all lessons with student-specific data.
     * Aggregates data from multiple services (Facade pattern).
     * Implements error handling and data transformation.
     */
    private void loadLessons() {
        try {
            LOGGER.info("Loading lessons for student: " + studentId);
            
            // Get all lessons
            List<Lesson> lessons = lessonService.getAllLessons();
            
            if (lessons == null) {
                LOGGER.warning("LessonService returned null");
                allLessons.clear();
                applyFilters();
                return;
            }
            
            // Clear existing data
            allLessons.clear();
            
            // For each lesson, aggregate student-specific data
            for (Lesson lesson : lessons) {
                try {
                    LessonRecord record = createLessonRecord(lesson);
                    allLessons.add(record);
                } catch (Exception e) {
                    LOGGER.log(Level.WARNING, "Error processing lesson " + lesson.getLessonId(), e);
                    // Continue with other lessons
                }
            }
            
            // Sort by date descending (most recent first)
            allLessons.sort((a, b) -> b.getDate().compareTo(a.getDate()));
            
            // Apply filters and update display
            applyFilters();
            
            LOGGER.info("Loaded " + allLessons.size() + " lessons");
            
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Failed to load lessons", e);
            showError("Failed to load lessons: " + e.getMessage());
        }
    }
    
    /**
     * Create a LessonRecord from a Lesson by aggregating data from multiple services.
     * 
     * @param lesson The lesson domain object
     * @return A LessonRecord for table display
     */
    private LessonRecord createLessonRecord(Lesson lesson) {
        LessonRecord record = new LessonRecord();
        record.setLessonId(lesson.getLessonId());
        record.setDate(lesson.getLessonDate());
        
        // Get topics
        String topics = getTopicsString(lesson.getLessonId());
        record.setTopics(topics);
        
        // Get attendance status
        String attendanceStatus = getAttendanceStatus(lesson.getLessonId());
        record.setAttendanceStatus(attendanceStatus);
        
        // Get homework and quiz only if student attended
        if ("PRESENT".equals(attendanceStatus)) {
            String homeworkStatus = getHomeworkStatus(lesson.getLessonId());
            record.setHomeworkStatus(homeworkStatus != null ? homeworkStatus : "-");
            
            String quizScore = getQuizScore(lesson.getLessonId());
            record.setQuizScore(quizScore != null ? quizScore : "-");
        } else {
            record.setHomeworkStatus("-");
            record.setQuizScore("-");
        }
        
        return record;
    }
    
    /**
     * Get topics as a comma-separated string.
     * 
     * @param lessonId The lesson ID
     * @return Topics string or "N/A" if none
     */
    private String getTopicsString(Integer lessonId) {
        try {
            List<LessonTopic> topics = lessonService.getLessonTopics(lessonId);
            if (topics == null || topics.isEmpty()) {
                return "N/A";
            }
            return topics.stream()
                .map(LessonTopic::getSpecificTopic)
                .collect(Collectors.joining(", "));
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Error getting topics for lesson " + lessonId, e);
            return "N/A";
        }
    }
    
    /**
     * Get attendance status for student in lesson.
     * 
     * @param lessonId The lesson ID
     * @return "PRESENT" or "ABSENT"
     */
    private String getAttendanceStatus(Integer lessonId) {
        try {
            com.studenttracker.model.Attendance attendance = attendanceService.getAttendance(lessonId, studentId);
            if (attendance != null && attendance.getStatus() != null) {
                return attendance.getStatus().toString();
            }
            return "ABSENT";
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Error getting attendance for lesson " + lessonId, e);
            return "ABSENT";
        }
    }
    
    /**
     * Get homework status for student in lesson.
     * 
     * @param lessonId The lesson ID
     * @return Homework status string or null
     */
    private String getHomeworkStatus(Integer lessonId) {
        try {
            com.studenttracker.model.Homework homework = homeworkService.getHomework(lessonId, studentId);
            if (homework != null && homework.getStatus() != null) {
                return homework.getStatus().toString();
            }
            return null;
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Error getting homework for lesson " + lessonId, e);
            return null;
        }
    }
    
    /**
     * Get quiz score for student in lesson.
     * 
     * @param lessonId The lesson ID
     * @return Quiz score string (e.g., "18/20") or null
     */
    private String getQuizScore(Integer lessonId) {
        try {
            com.studenttracker.model.Quiz quiz = quizService.getQuizByLesson(lessonId);
            if (quiz == null) {
                return null;
            }
            
            Double total = quizService.getStudentQuizTotal(quiz.getQuizId(), studentId);
            if (total == null) {
                return null;
            }
            
            // Get max possible score from quiz questions
            List<com.studenttracker.model.QuizQuestion> questions = quizService.getQuizQuestions(quiz.getQuizId());
            double maxScore = questions.stream()
                .mapToDouble(q -> q.getPoints())
                .sum();
            
            return String.format("%.1f/%.1f", total, maxScore);
            
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Error getting quiz score for lesson " + lessonId, e);
            return null;
        }
    }
    
    // ==================== FILTERING AND PAGINATION ====================
    
    /**
     * Apply filters and update pagination.
     * Implements the filtering logic based on selected criteria.
     */
    private void applyFilters() {
        List<LessonRecord> result = allLessons.stream().collect(Collectors.toList());
        
        // Date range filter
        LocalDate from = dateFromPicker.getValue();
        LocalDate to = dateToPicker.getValue();
        
        if (from != null) {
            result = result.stream()
                .filter(r -> !r.getDate().isBefore(from))
                .collect(Collectors.toList());
        }
        
        if (to != null) {
            result = result.stream()
                .filter(r -> !r.getDate().isAfter(to))
                .collect(Collectors.toList());
        }
        
        // Status filter
        String filter = filterCombo.getValue();
        if (filter != null) {
            switch (filter) {
                case "Attended Only":
                    result = result.stream()
                        .filter(r -> "PRESENT".equals(r.getAttendanceStatus()))
                        .collect(Collectors.toList());
                    break;
                case "Absent Only":
                    result = result.stream()
                        .filter(r -> "ABSENT".equals(r.getAttendanceStatus()))
                        .collect(Collectors.toList());
                    break;
                case "Done Homework":
                    result = result.stream()
                        .filter(r -> "DONE".equals(r.getHomeworkStatus()))
                        .collect(Collectors.toList());
                    break;
                case "Quiz Score > 70%":
                    result = result.stream()
                        .filter(r -> isQuizScoreAbove70(r.getQuizScore()))
                        .collect(Collectors.toList());
                    break;
                case "All":
                default:
                    // No additional filtering
                    break;
            }
        }
        
        filteredLessons.setAll(result);
        
        // Reset to page 1
        currentPageNumber = 1;
        updatePagination();
        
        LOGGER.fine("Filters applied, " + filteredLessons.size() + " lessons after filtering");
    }
    
    /**
     * Check if quiz score is above 70%.
     * 
     * @param scoreStr Quiz score string (e.g., "18.0/20.0")
     * @return true if score > 70%, false otherwise
     */
    private boolean isQuizScoreAbove70(String scoreStr) {
        if (scoreStr == null || "-".equals(scoreStr)) {
            return false;
        }
        try {
            String[] parts = scoreStr.split("/");
            if (parts.length != 2) {
                return false;
            }
            double earned = Double.parseDouble(parts[0].trim());
            double total = Double.parseDouble(parts[1].trim());
            return (earned / total) > 0.7;
        } catch (Exception e) {
            LOGGER.log(Level.FINE, "Error parsing quiz score: " + scoreStr, e);
            return false;
        }
    }
    
    /**
     * Update pagination state and current page display.
     * Implements pagination logic with boundary checks.
     */
    private void updatePagination() {
        // Calculate total pages
        int totalRecords = filteredLessons.size();
        totalPages = (int) Math.ceil((double) totalRecords / pageSize);
        if (totalPages == 0) {
            totalPages = 1;
        }
        
        // Ensure current page is valid
        if (currentPageNumber > totalPages) {
            currentPageNumber = totalPages;
        }
        if (currentPageNumber < 1) {
            currentPageNumber = 1;
        }
        
        // Calculate start/end indexes
        int startIndex = (currentPageNumber - 1) * pageSize;
        int endIndex = Math.min(startIndex + pageSize, totalRecords);
        
        // Update current page
        if (startIndex < totalRecords && startIndex >= 0) {
            currentPage.setAll(filteredLessons.subList(startIndex, endIndex));
        } else {
            currentPage.clear();
        }
        
        // Update UI controls
        pageLabel.setText("Page " + currentPageNumber + " of " + totalPages);
        
        // Enable/disable buttons
        firstPageBtn.setDisable(currentPageNumber == 1);
        prevPageBtn.setDisable(currentPageNumber == 1);
        nextPageBtn.setDisable(currentPageNumber == totalPages);
        lastPageBtn.setDisable(currentPageNumber == totalPages);
        
        LOGGER.fine("Pagination updated: page " + currentPageNumber + " of " + totalPages);
    }
    
    // ==================== EVENT HANDLERS (FXML) ====================
    
    /**
     * Handle Apply Filter button click.
     */
    @FXML
    private void handleApplyFilter() {
        LOGGER.fine("Apply filter clicked");
        applyFilters();
    }
    
    /**
     * Handle Clear Filter button click.
     */
    @FXML
    private void handleClearFilter() {
        LOGGER.fine("Clear filter clicked");
        dateFromPicker.setValue(null);
        dateToPicker.setValue(null);
        filterCombo.setValue("All");
        applyFilters();
    }
    
    /**
     * Handle First Page button click.
     */
    @FXML
    private void handleFirstPage() {
        if (currentPageNumber > 1) {
            currentPageNumber = 1;
            updatePagination();
            LOGGER.fine("Navigated to first page");
        }
    }
    
    /**
     * Handle Previous Page button click.
     */
    @FXML
    private void handlePrevPage() {
        if (currentPageNumber > 1) {
            currentPageNumber--;
            updatePagination();
            LOGGER.fine("Navigated to previous page");
        }
    }
    
    /**
     * Handle Next Page button click.
     */
    @FXML
    private void handleNextPage() {
        if (currentPageNumber < totalPages) {
            currentPageNumber++;
            updatePagination();
            LOGGER.fine("Navigated to next page");
        }
    }
    
    /**
     * Handle Last Page button click.
     */
    @FXML
    private void handleLastPage() {
        if (currentPageNumber < totalPages) {
            currentPageNumber = totalPages;
            updatePagination();
            LOGGER.fine("Navigated to last page");
        }
    }
    
    /**
     * Handle Page Size ComboBox change.
     */
    @FXML
    private void handlePageSizeChange() {
        try {
            String sizeStr = pageSizeCombo.getValue();
            if (sizeStr != null) {
                pageSize = Integer.parseInt(sizeStr);
                currentPageNumber = 1;
                updatePagination();
                LOGGER.fine("Page size changed to: " + pageSize);
            }
        } catch (NumberFormatException e) {
            LOGGER.log(Level.WARNING, "Invalid page size", e);
        }
    }
    
    /**
     * Handle View Lesson button click.
     * Navigates to lesson detail view.
     * 
     * @param lessonId The lesson ID to view
     */
    private void handleViewLesson(int lessonId) {
        try {
            LOGGER.info("Viewing lesson: " + lessonId);
            // Note: This requires SceneManager to have showLessonProfile method
            // For now, show info message
            showInfo("Lesson Details", "Navigate to lesson " + lessonId + " details");
            
            // TODO: Uncomment when SceneManager.showLessonProfile is implemented
            // sceneManager.showLessonProfile(lessonId);
            
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Failed to open lesson profile", e);
            showError("Failed to open lesson profile: " + e.getMessage());
        }
    }
    
    // ==================== EVENT SUBSCRIBERS (EventBus) ====================
    
    /**
     * Handle AttendanceMarkedEvent.
     * Reloads lessons when attendance is marked for this student.
     * 
     * @param event The attendance marked event
     */
    @Subscribe
    public void onAttendanceMarked(AttendanceMarkedEvent event) {
        if (event.getStudentId() == studentId) {
            LOGGER.info("Attendance marked event received for student " + studentId);
            Platform.runLater(this::loadLessons);
        }
    }
    
    /**
     * Handle HomeworkRecordedEvent.
     * Reloads lessons when homework is recorded for this student.
     * 
     * @param event The homework recorded event
     */
    @Subscribe
    public void onHomeworkRecorded(HomeworkRecordedEvent event) {
        if (event.getStudentId() == studentId) {
            LOGGER.info("Homework recorded event received for student " + studentId);
            Platform.runLater(this::loadLessons);
        }
    }
    
    /**
     * Handle QuizGradedEvent.
     * Reloads lessons when quiz is graded for this student.
     * 
     * @param event The quiz graded event
     */
    @Subscribe
    public void onQuizGraded(QuizGradedEvent event) {
        if (event.getStudentId() == studentId) {
            LOGGER.info("Quiz graded event received for student " + studentId);
            Platform.runLater(this::loadLessons);
        }
    }
    
    // ==================== INNER CLASS ====================
    
    /**
     * LessonRecord - Data transfer object for table display.
     * Encapsulates lesson data in presentation format.
     * 
     * <p><b>Design Pattern:</b> Data Transfer Object (DTO)</p>
     */
    public static class LessonRecord {
        private int lessonId;
        private LocalDate date;
        private String topics;
        private String attendanceStatus;
        private String homeworkStatus;
        private String quizScore;
        
        // Constructors
        public LessonRecord() {}
        
        // Getters and Setters
        public int getLessonId() {
            return lessonId;
        }
        
        public void setLessonId(int lessonId) {
            this.lessonId = lessonId;
        }
        
        public LocalDate getDate() {
            return date;
        }
        
        public void setDate(LocalDate date) {
            this.date = date;
        }
        
        public String getTopics() {
            return topics;
        }
        
        public void setTopics(String topics) {
            this.topics = topics;
        }
        
        public String getAttendanceStatus() {
            return attendanceStatus;
        }
        
        public void setAttendanceStatus(String attendanceStatus) {
            this.attendanceStatus = attendanceStatus;
        }
        
        public String getHomeworkStatus() {
            return homeworkStatus;
        }
        
        public void setHomeworkStatus(String homeworkStatus) {
            this.homeworkStatus = homeworkStatus;
        }
        
        public String getQuizScore() {
            return quizScore;
        }
        
        public void setQuizScore(String quizScore) {
            this.quizScore = quizScore;
        }
        
        @Override
        public String toString() {
            return "LessonRecord{" +
                    "lessonId=" + lessonId +
                    ", date=" + date +
                    ", attendance=" + attendanceStatus +
                    ", homework=" + homeworkStatus +
                    ", quiz=" + quizScore +
                    '}';
        }
    }
}