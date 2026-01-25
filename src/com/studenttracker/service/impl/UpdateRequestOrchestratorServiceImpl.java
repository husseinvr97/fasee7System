package com.studenttracker.service.impl;

import com.studenttracker.dao.UpdateRequestDAO;
import com.studenttracker.exception.ServiceException;
import com.studenttracker.exception.UnauthorizedException;
import com.studenttracker.exception.ValidationException;
import com.studenttracker.model.UpdateRequest;
import com.studenttracker.model.UpdateRequest.RequestStatus;
import com.studenttracker.service.*;
import com.studenttracker.service.event.UpdateRequestApprovedEvent;
import com.studenttracker.service.event.UpdateRequestRejectedEvent;
import com.studenttracker.service.event.UpdateRequestSubmittedEvent;
import com.studenttracker.service.impl.helpers.UpdateRequestOrchestratorServiceImplHelpers;
import com.studenttracker.util.DatabaseConnection;

import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Implementation of UpdateRequestOrchestratorService.
 * Orchestrates the complete lifecycle of update requests.
 */
public class UpdateRequestOrchestratorServiceImpl implements UpdateRequestOrchestratorService {
    
    private final UpdateRequestDAO updateRequestDAO;
    private final AttendanceService attendanceService;
    private final QuizService quizService;
    private final StudentService studentService;
    private final HomeworkService homeworkService;
    private final BehavioralIncidentService behavioralService;
    private final UserService userService;
    private final ConsecutivityTrackingService consecutivityService;
    private final WarningService warningService;
    private final TargetService targetService;
    private final EventBusService eventBusService;
    private final DatabaseConnection databaseConnection;
    
    public UpdateRequestOrchestratorServiceImpl(
            UpdateRequestDAO updateRequestDAO,
            AttendanceService attendanceService,
            QuizService quizService,
            StudentService studentService,
            HomeworkService homeworkService,
            BehavioralIncidentService behavioralService,
            UserService userService,
            ConsecutivityTrackingService consecutivityService,
            WarningService warningService,
            TargetService targetService,
            EventBusService eventBusService,
            DatabaseConnection databaseConnection) {
        this.updateRequestDAO = updateRequestDAO;
        this.attendanceService = attendanceService;
        this.quizService = quizService;
        this.studentService = studentService;
        this.homeworkService = homeworkService;
        this.behavioralService = behavioralService;
        this.userService = userService;
        this.consecutivityService = consecutivityService;
        this.warningService = warningService;
        this.targetService = targetService;
        this.eventBusService = eventBusService;
        this.databaseConnection = databaseConnection;
    }
    
    // ========== Submit Request ==========
    
    @Override
    public Integer submitUpdateRequest(String requestType, String entityType, Integer entityId,
                                       String requestedChangesJson, String reason, Integer requestedBy) {
        try {
            // Step 1: Validate request format
            UpdateRequestOrchestratorServiceImplHelpers.validateRequestFormat(
                requestType, entityType, requestedChangesJson
            );
            
            // Step 2: Check for conflicts
            if (UpdateRequestOrchestratorServiceImplHelpers.checkConflicts(
                    entityType, entityId, updateRequestDAO)) {
                throw new ValidationException(
                    "A pending request already exists for this entity"
                );
            }
            
            // Step 3: Create UpdateRequest object
            UpdateRequest request = new UpdateRequest();
            request.setRequestType(requestType);
            request.setEntityType(entityType);
            request.setEntityId(entityId);
            request.setRequestedChanges(requestedChangesJson);
            request.setRequestedBy(requestedBy);
            request.setRequestedAt(LocalDateTime.now());
            request.setStatus(RequestStatus.PENDING);
            
            // Step 4: Insert into database
            Integer requestId = updateRequestDAO.insert(request);

            if(requestId == null) {
                throw new ServiceException("Failed to submit update request");
            }
            
            // Step 5: Publish event
            UpdateRequestSubmittedEvent event = new UpdateRequestSubmittedEvent(
                requestId,
                requestType,
                requestedBy,
                entityType,
                entityId
            );
            eventBusService.publish(event);
            
            // Step 6: Return request ID
            return requestId;
            
        } catch (ValidationException e) {
            throw e;
        } catch (Exception e) {
            throw new ServiceException("Failed to submit update request", e);
        }
    }
    
    // ========== Admin Actions ==========
    
    @Override
    public boolean approveRequest(Integer requestId, Integer adminId) {
        try {
            // Step 1: Validate admin permission
            UpdateRequestOrchestratorServiceImplHelpers.validateAdminPermission(adminId, userService);
            
            // Step 2: Get request details
            UpdateRequest request = updateRequestDAO.findById(requestId);
            if (request == null) {
                return false;
            }
            
            // Step 3: Update status to APPROVED
            request.approve(adminId);
            updateRequestDAO.update(request);
            
            // Step 4: Execute request
            boolean success = executeRequest(requestId);
            
            // Step 5: Publish event if successful
            if (success) {
                UpdateRequestApprovedEvent event = new UpdateRequestApprovedEvent(
                    requestId,
                    adminId,
                    request.getEntityType(),
                    request.getEntityId(),
                    request.getRequestType()
                );
                eventBusService.publish(event);
            }
            
            return success;
            
        } catch (UnauthorizedException e) {
            throw e;
        } catch (Exception e) {
            throw new ServiceException("Failed to approve request", e);
        }
    }
    
    @Override
    public boolean rejectRequest(Integer requestId, Integer adminId, String reason) {
        try {
            // Step 1: Validate admin permission
            UpdateRequestOrchestratorServiceImplHelpers.validateAdminPermission(adminId, userService);
            
            // Step 2: Get request
            UpdateRequest request = updateRequestDAO.findById(requestId);
            if (request == null) {
                return false;
            }
            
            // Step 3: Reject request
            request.reject(adminId, reason);
            
            // Step 4: Update in database
            boolean success = updateRequestDAO.update(request);
            
            // Step 5: Publish event
            if (success) {
                UpdateRequestRejectedEvent event = new UpdateRequestRejectedEvent(
                    requestId,
                    adminId,
                    reason,
                    request.getRequestType()
                );
                eventBusService.publish(event);
            }
            
            return success;
            
        } catch (UnauthorizedException e) {
            throw e;
        } catch (Exception e) {
            throw new ServiceException("Failed to reject request", e);
        }
    }
    
    // ========== Execute Request (Internal) ==========
    
    private boolean executeRequest(Integer requestId) {
        Connection conn = null;
        
        try {
            // Step 1: Get request
            UpdateRequest request = updateRequestDAO.findById(requestId);
            if (request == null) {
                return false;
            }
            
            // Step 2: Validate entity still exists
            if (!UpdateRequestOrchestratorServiceImplHelpers.validateEntityExists(
                    request.getEntityType(), request.getEntityId(), 
                    attendanceService, quizService, studentService, homeworkService, behavioralService)) {
                request.setStatus(RequestStatus.FAILED);
                request.setReviewNotes("Entity no longer exists");
                updateRequestDAO.update(request);
                return false;
            }
            
            // Step 3: Begin transaction
            conn = databaseConnection.getConnection();
            conn.setAutoCommit(false);
            
            // Step 4: Route to handler
            boolean handlerSuccess = false;
            String requestType = request.getRequestType();
            
            switch (requestType) {
                case "UPDATE_ATTENDANCE":
                    handlerSuccess = handleAttendanceUpdate(request);
                    break;
                case "UPDATE_HOMEWORK":
                    handlerSuccess = handleHomeworkUpdate(request);
                    break;
                case "UPDATE_QUIZ_SCORE":
                    handlerSuccess = handleQuizScoreUpdate(request);
                    break;
                case "ADD_BEHAVIORAL_INCIDENT":
                    handlerSuccess = handleAddIncident(request);
                    break;
                case "REMOVE_BEHAVIORAL_INCIDENT":
                    handlerSuccess = handleRemoveIncident(request);
                    break;
                case "RESTORE_ARCHIVED_STUDENT":
                    handlerSuccess = handleRestoreStudent(request);
                    break;
                default:
                    request.setStatus(RequestStatus.FAILED);
                    request.setReviewNotes("Unknown request type: " + requestType);
                    updateRequestDAO.update(request);
                    conn.rollback();
                    return false;
            }
            
            // Step 5: Handle result
            if (handlerSuccess) {
                // Update status to APPLIED
                request.setStatus(RequestStatus.APPLIED);
                updateRequestDAO.update(request);
                
                // Trigger cascading updates
                try {
                    UpdateRequestOrchestratorServiceImplHelpers.triggerCascadingUpdates(
                        request, consecutivityService, warningService, targetService
                    );
                    
                    // Update status to COMPLETED
                    request.setStatus(RequestStatus.COMPLETED);
                    updateRequestDAO.update(request);
                    
                    // Commit transaction
                    conn.commit();
                    return true;
                    
                } catch (Exception e) {
                    // Cascading failed
                    request.setStatus(RequestStatus.FAILED);
                    request.setReviewNotes("Cascading updates failed: " + e.getMessage());
                    updateRequestDAO.update(request);
                    conn.rollback();
                    return false;
                }
            } else {
                // Handler failed
                request.setStatus(RequestStatus.FAILED);
                updateRequestDAO.update(request);
                conn.rollback();
                return false;
            }
            
        } catch (Exception e) {
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException ex) {
                    System.err.println("Failed to rollback: " + ex.getMessage());
                }
            }
            throw new ServiceException("Failed to execute request", e);
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                    databaseConnection.closeConnection(conn);
                } catch (SQLException e) {
                    System.err.println("Failed to reset auto-commit: " + e.getMessage());
                }
            }
        }
    }
    
    // ========== Request Handlers (Private) ==========
    
    private boolean handleAttendanceUpdate(UpdateRequest request) {
        try {
            var changes = UpdateRequestOrchestratorServiceImplHelpers.parseRequestedChanges(
                request.getRequestedChanges()
            );
            
            Integer attendanceId = (Integer) changes.get("attendanceId");
            String newStatusStr = (String) changes.get("newStatus");
            
            // Validate if changing to PRESENT
            if ("PRESENT".equals(newStatusStr)) {
                UpdateRequestOrchestratorServiceImplHelpers.validateAttendanceUpdateRules(
                    attendanceId, attendanceService, quizService, homeworkService
                );
            }
            
            // Update attendance
            com.studenttracker.model.Attendance.AttendanceStatus newStatus = 
                com.studenttracker.model.Attendance.AttendanceStatus.valueOf(newStatusStr);
            
            return attendanceService.updateAttendance(attendanceId, newStatus);
            
        } catch (Exception e) {
            System.err.println("Attendance update failed: " + e.getMessage());
            return false;
        }
    }
    
    private boolean handleHomeworkUpdate(UpdateRequest request) {
        try {
            var changes = UpdateRequestOrchestratorServiceImplHelpers.parseRequestedChanges(
                request.getRequestedChanges()
            );
            
            Integer homeworkId = (Integer) changes.get("homeworkId");
            String newStatusStr = (String) changes.get("newStatus");
            
            com.studenttracker.model.Homework.HomeworkStatus newStatus = 
                com.studenttracker.model.Homework.HomeworkStatus.valueOf(newStatusStr);
            
            return homeworkService.updateHomework(homeworkId, newStatus);
            
        } catch (Exception e) {
            System.err.println("Homework update failed: " + e.getMessage());
            return false;
        }
    }
    
    private boolean handleQuizScoreUpdate(UpdateRequest request) {
        try {
            var changes = UpdateRequestOrchestratorServiceImplHelpers.parseRequestedChanges(
                request.getRequestedChanges()
            );
            
            Integer scoreId = (Integer) changes.get("scoreId");
            Object pointsObj = changes.get("newPoints");
            Double newPoints;
            
            if (pointsObj instanceof Integer) {
                newPoints = (Double) pointsObj;
            } else if (pointsObj instanceof Double) {
                newPoints = (Double) pointsObj;
            } else {
                newPoints = Double.parseDouble(pointsObj.toString());
            }
            
            return quizService.updateQuizScore(scoreId, newPoints);
            
        } catch (Exception e) {
            System.err.println("Quiz score update failed: " + e.getMessage());
            return false;
        }
    }
    
    private boolean handleAddIncident(UpdateRequest request) {
        try {
            var changes = UpdateRequestOrchestratorServiceImplHelpers.parseRequestedChanges(
                request.getRequestedChanges()
            );
            
            Integer studentId = (Integer) changes.get("studentId");
            Integer lessonId = (Integer) changes.get("lessonId");
            String typeStr = (String) changes.get("incidentType");
            String notes = (String) changes.get("notes");
            
            com.studenttracker.model.BehavioralIncident.IncidentType type = 
                com.studenttracker.model.BehavioralIncident.IncidentType.valueOf(typeStr);
            
            Integer incidentId = behavioralService.addIncident(
                studentId, lessonId, type, notes, request.getReviewedBy()
            );
            
            return incidentId != null;
            
        } catch (Exception e) {
            System.err.println("Add incident failed: " + e.getMessage());
            return false;
        }
    }
    
    private boolean handleRemoveIncident(UpdateRequest request) {
        try {
            var changes = UpdateRequestOrchestratorServiceImplHelpers.parseRequestedChanges(
                request.getRequestedChanges()
            );
            
            Integer incidentId = (Integer) changes.get("incidentId");
            
            return behavioralService.deleteIncident(incidentId, request.getReviewedBy());
            
        } catch (Exception e) {
            System.err.println("Remove incident failed: " + e.getMessage());
            return false;
        }
    }
    
    private boolean handleRestoreStudent(UpdateRequest request) {
        try {
            var changes = UpdateRequestOrchestratorServiceImplHelpers.parseRequestedChanges(
                request.getRequestedChanges()
            );
            
            Integer studentId = (Integer) changes.get("studentId");
            
            return studentService.restoreStudent(studentId, request.getReviewedBy());
            
        } catch (Exception e) {
            System.err.println("Restore student failed: " + e.getMessage());
            return false;
        }
    }
    
    // ========== Retrieval ==========
    
    @Override
    public UpdateRequest getRequestById(Integer requestId) {
        return updateRequestDAO.findById(requestId);
    }
    
    @Override
    public List<UpdateRequest> getPendingRequests() {
        return updateRequestDAO.findByStatus(RequestStatus.PENDING);
    }
    
    @Override
    public List<UpdateRequest> getRequestsByAssistant(Integer assistantId) {
        return updateRequestDAO.findByRequestedBy(assistantId);
    }
    
    @Override
    public List<UpdateRequest> getRequestsByStatus(RequestStatus status) {
        return updateRequestDAO.findByStatus(status);
    }
    
    @Override
    public List<UpdateRequest> getRequestHistory(Integer entityId, String entityType) {
        return updateRequestDAO.findByEntity(entityType, entityId);
    }
    
    // ========== Validation ==========
    
    @Override
    public boolean hasConflictingRequest(String entityType, Integer entityId) {
        return UpdateRequestOrchestratorServiceImplHelpers.checkConflicts(
            entityType, entityId, updateRequestDAO
        );
    }
    
    // ========== Statistics ==========
    
    @Override
    public int getPendingRequestCount() {
        return updateRequestDAO.countByStatus(RequestStatus.PENDING);
    }
}