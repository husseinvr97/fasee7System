package com.studenttracker.service;

import com.studenttracker.model.UpdateRequest;
import com.studenttracker.model.UpdateRequest.RequestStatus;
import java.util.List;

/**
 * Service interface for orchestrating update requests.
 * Handles submission, approval, rejection, and execution of change requests.
 */
public interface UpdateRequestOrchestratorService {
    
    // ========== Submit Request ==========
    
    /**
     * Submits a new update request (Assistant or Admin).
     * 
     * @param requestType Type of request (UPDATE_ATTENDANCE, UPDATE_QUIZ_SCORE, etc.)
     * @param entityType Type of entity being updated (ATTENDANCE, QUIZ_SCORE, etc.)
     * @param entityId ID of the entity
     * @param requestedChangesJson JSON string with requested changes
     * @param reason Reason for the request
     * @param requestedBy User ID submitting the request
     * @return request ID
     */
    Integer submitUpdateRequest(String requestType, String entityType, Integer entityId,
                               String requestedChangesJson, String reason, Integer requestedBy);
    
    // ========== Admin Actions ==========
    
    /**
     * Approves and executes an update request (Admin only).
     * 
     * @param requestId ID of the request
     * @param adminId ID of the admin approving
     * @return true if successful, false otherwise
     */
    boolean approveRequest(Integer requestId, Integer adminId);
    
    /**
     * Rejects an update request (Admin only).
     * 
     * @param requestId ID of the request
     * @param adminId ID of the admin rejecting
     * @param reason Reason for rejection
     * @return true if successful, false otherwise
     */
    boolean rejectRequest(Integer requestId, Integer adminId, String reason);
    
    // ========== Retrieval ==========
    
    /**
     * Gets a request by ID.
     * 
     * @param requestId ID of the request
     * @return UpdateRequest or null if not found
     */
    UpdateRequest getRequestById(Integer requestId);
    
    /**
     * Gets all pending requests.
     * 
     * @return List of pending requests
     */
    List<UpdateRequest> getPendingRequests();
    
    /**
     * Gets all requests submitted by a specific user.
     * 
     * @param assistantId User ID
     * @return List of requests
     */
    List<UpdateRequest> getRequestsByAssistant(Integer assistantId);
    
    /**
     * Gets all requests with a specific status.
     * 
     * @param status Request status
     * @return List of requests
     */
    List<UpdateRequest> getRequestsByStatus(RequestStatus status);
    
    /**
     * Gets request history for a specific entity.
     * 
     * @param entityId ID of the entity
     * @param entityType Type of entity
     * @return List of requests
     */
    List<UpdateRequest> getRequestHistory(Integer entityId, String entityType);
    
    // ========== Validation ==========
    
    /**
     * Checks if there's a conflicting pending request for the same entity.
     * 
     * @param entityType Type of entity
     * @param entityId ID of entity
     * @return true if conflict exists
     */
    boolean hasConflictingRequest(String entityType, Integer entityId);
    
    // ========== Statistics ==========
    
    /**
     * Gets count of pending requests.
     * 
     * @return Number of pending requests
     */
    int getPendingRequestCount();
}