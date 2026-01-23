package com.studenttracker.dao;

import com.studenttracker.model.UpdateRequest;
import com.studenttracker.model.UpdateRequest.RequestStatus;
import java.time.LocalDate;
import java.util.List;

public interface UpdateRequestDAO {
    Integer insert(UpdateRequest request);
    boolean update(UpdateRequest request);
    boolean delete(int requestId);
    UpdateRequest findById(int requestId);
    List<UpdateRequest> findAll();
    List<UpdateRequest> findByStatus(RequestStatus status);
    List<UpdateRequest> findByRequestedBy(int userId);
    int countByStatus(RequestStatus status);
    List<UpdateRequest> findByEntity(String entityType, int entityId);
    List<UpdateRequest> findPendingByEntity(String entityType, int entityId);
    List<UpdateRequest> findByDateRange(LocalDate start, LocalDate end);
}