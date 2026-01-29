package com.studenttracker.service.impl;

import com.studenttracker.dao.*;
import com.studenttracker.exception.*;
import com.studenttracker.model.*;
import com.studenttracker.model.BehavioralIncident.IncidentType;
import com.studenttracker.model.UpdateRequest.RequestStatus;
import com.studenttracker.service.BehavioralIncidentService;
import com.studenttracker.service.EventBusService;
import com.studenttracker.service.event.BehavioralIncidentAddedEvent;
import com.studenttracker.service.impl.helpers.BehavioralIncidentServiceImplHelpers;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * Implementation of BehavioralIncidentService.
 * Handles business logic, validation, and event publishing for behavioral incident operations.
 */
public class BehavioralIncidentServiceImpl implements BehavioralIncidentService {
    
    private final BehavioralIncidentDAO incidentDAO;
    private final StudentDAO studentDAO;
    private final AttendanceDAO attendanceDAO;
    private final UserDAO userDAO;
    private final UpdateRequestDAO updateRequestDAO;
    private final LessonDAO lessonDAO;
    private final EventBusService eventBusService;
    
    /**
     * Constructor with dependency injection.
     */
    public BehavioralIncidentServiceImpl(BehavioralIncidentDAO incidentDAO,
                                        StudentDAO studentDAO,
                                        AttendanceDAO attendanceDAO,
                                        UserDAO userDAO,
                                        UpdateRequestDAO updateRequestDAO,
                                        LessonDAO lessonDAO , EventBusService eventBusService) {
        this.incidentDAO = incidentDAO;
        this.studentDAO = studentDAO;
        this.attendanceDAO = attendanceDAO;
        this.userDAO = userDAO;
        this.updateRequestDAO = updateRequestDAO;
        this.lessonDAO = lessonDAO;
        this.eventBusService = eventBusService;
    }
    
    
    // ========== CRUD Operations ==========
    
    @Override
    public Integer addIncident(Integer studentId, Integer lessonId, IncidentType type, 
                              String notes, Integer createdBy) {
        // Step 1: Get the user to check their role
        User user = userDAO.findById(createdBy);
        if (user == null) {
            throw new UserNotFoundException("the user with id " +createdBy + " does not exist");
        }
        
        // Step 2: Validate student exists and is ACTIVE
        Student student = studentDAO.findById(studentId);
        if (student == null) {
            throw new StudentNotFoundException(studentId);
        }
        if (student.isArchived()) {
            throw new ValidationException("Cannot add incident for archived student");
        }
        
        // Step 3: Validate student attended this lesson (must be PRESENT)
        Attendance attendance = attendanceDAO.findByLessonAndStudent(lessonId, studentId);
        if (attendance == null) {
            throw new ValidationException("Student did not attend this lesson");
        }
        if (!attendance.isPresent()) {
            throw new ValidationException("Cannot add incident for absent student");
        }
        
        // Step 4: Handle based on user role
        if (user.isAssistant()) {
            // Assistant must create UpdateRequest for admin approval
            return createIncidentUpdateRequest(studentId, lessonId, type, notes, createdBy);
        } else if (user.isAdmin()) {
            // Admin can directly create incident
            return createIncidentDirectly(studentId, lessonId, type, notes, createdBy);
        } else {
            throw new UnauthorizedException("User does not have permission to add incidents");
        }
    }
    
    /**
     * Helper method: Create incident directly (for Admin).
     */
    private Integer createIncidentDirectly(Integer studentId, Integer lessonId, 
                                          IncidentType type, String notes, Integer createdBy) {
        // Create BehavioralIncident object
        BehavioralIncident incident = new BehavioralIncident();
        incident.setStudentId(studentId);
        incident.setLessonId(lessonId);
        incident.setIncidentType(type);
        incident.setNotes(notes);
        incident.setCreatedAt(LocalDateTime.now());
        incident.setCreatedBy(createdBy);
        
        // Insert into database
        Integer incidentId = incidentDAO.insert(incident);
        
        // Publish event
        BehavioralIncidentAddedEvent event = new BehavioralIncidentAddedEvent(
            incidentId,
            studentId,
            lessonId,
            type,
            createdBy,
            incident.getCreatedAt()
        );
        eventBusService.publish(event);
        
        return incidentId;
    }
    
    /**
     * Helper method: Create UpdateRequest (for Assistant).
     */
    private Integer createIncidentUpdateRequest(Integer studentId, Integer lessonId, 
                                               IncidentType type, String notes, Integer requestedBy) {
        // Build requestedChanges as JSON string
        String requestedChanges = String.format(
            "{\"studentId\":%d,\"lessonId\":%d,\"type\":\"%s\",\"notes\":\"%s\"}",
            studentId, lessonId, type.name(), notes != null ? notes.replace("\"", "\\\"") : ""
        );
        
        // Create UpdateRequest
        UpdateRequest request = new UpdateRequest();
        request.setRequestType("ADD_INCIDENT");
        request.setEntityType("BEHAVIORAL_INCIDENT");
        request.setEntityId(null); // No entity yet, will be created upon approval
        request.setRequestedChanges(requestedChanges);
        request.setRequestedBy(requestedBy);
        request.setRequestedAt(LocalDateTime.now());
        request.setStatus(RequestStatus.PENDING);
        
        // Insert and return request ID
        return updateRequestDAO.insert(request);
    }
    
    @Override
    public boolean updateIncident(Integer incidentId, IncidentType type, String notes, 
                                 Integer updatedBy) {
        // Step 1: Validate admin permission
        BehavioralIncidentServiceImplHelpers.validateAdminPermission(updatedBy, userDAO);
        
        // Step 2: Find the incident
        BehavioralIncident incident = incidentDAO.findById(incidentId);
        if (incident == null) {
            throw new ValidationException("Incident not found with ID: " + incidentId);
        }
        
        // Step 3: Update fields
        incident.setIncidentType(type);
        incident.setNotes(notes);
        
        // Step 4: Persist changes
        return incidentDAO.update(incident);
    }
    
    @Override
    public boolean deleteIncident(Integer incidentId, Integer deletedBy) {
        // Step 1: Validate admin permission
        BehavioralIncidentServiceImplHelpers.validateAdminPermission(deletedBy, userDAO);
        
        // Step 2: Verify incident exists
        BehavioralIncident incident = incidentDAO.findById(incidentId);
        if (incident == null) {
            throw new ValidationException("Incident not found with ID: " + incidentId);
        }
        
        // Step 3: Delete incident
        return incidentDAO.delete(incidentId);
    }
    
    
    // ========== Retrieval Operations ==========
    
    @Override
    public BehavioralIncident getIncidentById(Integer incidentId) {
        return incidentDAO.findById(incidentId);
    }
    
    @Override
    public List<BehavioralIncident> getIncidentsByStudent(Integer studentId) {
        return incidentDAO.findByStudentId(studentId);
    }
    
    @Override
    public List<BehavioralIncident> getIncidentsByLesson(Integer lessonId) {
        return incidentDAO.findByLessonId(lessonId);
    }
    
    @Override
    public List<BehavioralIncident> getIncidentsByType(IncidentType type) {
        return incidentDAO.findByType(type);
    }
    
    
    // ========== Analysis Operations ==========
    
    @Override
    public List<BehavioralIncident> getConsecutiveSameTypeIncidents(Integer studentId) {
        // Get all incidents for student
        List<BehavioralIncident> incidents = incidentDAO.findByStudentId(studentId);
        
        // Find consecutive same-type incidents
        return BehavioralIncidentServiceImplHelpers.groupConsecutiveSameType(incidents);
    }
    
    @Override
    public List<BehavioralIncident> getIncidentsInMonth(Integer studentId, String monthGroup) {
        // Get all incidents for student
        List<BehavioralIncident> incidents = incidentDAO.findByStudentId(studentId);
        
        // Filter by month group
        return BehavioralIncidentServiceImplHelpers.filterByMonthGroup(incidents, monthGroup, lessonDAO);
    }
    
    @Override
    public int countIncidentsByStudent(Integer studentId) {
        return incidentDAO.countByStudentId(studentId);
    }
    
    @Override
    public Map<IncidentType, Integer> getIncidentTypeBreakdown(Integer studentId) {
        // Get all incidents for student
        List<BehavioralIncident> incidents = incidentDAO.findByStudentId(studentId);
        
        // Build breakdown by type
        return BehavioralIncidentServiceImplHelpers.buildIncidentTypeBreakdown(incidents);
    }
}