package com.studenttracker.dao.impl;

import com.studenttracker.dao.UpdateRequestDAO;
import com.studenttracker.exception.DAOException;
import com.studenttracker.model.UpdateRequest;
import com.studenttracker.model.UpdateRequest.RequestStatus;
import com.studenttracker.util.DatabaseConnection;
import com.studenttracker.util.ResultSetExtractor;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

public class UpdateRequestDAOImpl implements UpdateRequestDAO {
    private final DatabaseConnection dbConnection;

    public UpdateRequestDAOImpl() {
        this.dbConnection = DatabaseConnection.getInstance();
    }

    @Override
    public Integer insert(UpdateRequest request) {
        String sql = "INSERT INTO update_requests (request_type, entity_type, entity_id, " +
                    "requested_changes, requested_by, requested_at, status, reviewed_by, " +
                    "reviewed_at, review_notes) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        Connection conn = null;

        try {
            conn = dbConnection.getConnection();
            PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            
            pstmt.setString(1, request.getRequestType());
            pstmt.setString(2, request.getEntityType());
            pstmt.setInt(3, request.getEntityId());
            pstmt.setString(4, request.getRequestedChanges());
            pstmt.setInt(5, request.getRequestedBy());
            pstmt.setObject(6, request.getRequestedAt());
            pstmt.setString(7, request.getStatus().name());
            pstmt.setObject(8, request.getReviewedBy());
            pstmt.setObject(9, request.getReviewedAt());
            pstmt.setString(10, request.getReviewNotes());

            int affectedRows = pstmt.executeUpdate();
            
            if (affectedRows == 0) {
                throw new DAOException("Insert failed, no rows affected");
            }

            ResultSet generatedKeys = pstmt.getGeneratedKeys();
            if (generatedKeys.next()) {
                return generatedKeys.getInt(1);
            } else {
                throw new DAOException("Insert failed, no ID obtained");
            }

        } catch (SQLException e) {
            throw new DAOException("Failed to insert update request", e);
        } finally {
            dbConnection.closeConnection(conn);
        }
    }

    @Override
    public boolean update(UpdateRequest request) {
        String sql = "UPDATE update_requests SET request_type = ?, entity_type = ?, " +
                    "entity_id = ?, requested_changes = ?, requested_by = ?, requested_at = ?, " +
                    "status = ?, reviewed_by = ?, reviewed_at = ?, review_notes = ? " +
                    "WHERE request_id = ?";
        Connection conn = null;

        try {
            conn = dbConnection.getConnection();
            PreparedStatement pstmt = conn.prepareStatement(sql);
            
            pstmt.setString(1, request.getRequestType());
            pstmt.setString(2, request.getEntityType());
            pstmt.setInt(3, request.getEntityId());
            pstmt.setString(4, request.getRequestedChanges());
            pstmt.setInt(5, request.getRequestedBy());
            pstmt.setObject(6, request.getRequestedAt());
            pstmt.setString(7, request.getStatus().name());
            pstmt.setObject(8, request.getReviewedBy());
            pstmt.setObject(9, request.getReviewedAt());
            pstmt.setString(10, request.getReviewNotes());
            pstmt.setInt(11, request.getRequestId());

            return pstmt.executeUpdate() > 0;

        } catch (SQLException e) {
            throw new DAOException("Failed to update request with ID: " + request.getRequestId(), e);
        } finally {
            dbConnection.closeConnection(conn);
        }
    }

    @Override
    public boolean delete(int requestId) {
        String sql = "DELETE FROM update_requests WHERE request_id = ?";
        Connection conn = null;

        try {
            conn = dbConnection.getConnection();
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, requestId);

            return pstmt.executeUpdate() > 0;

        } catch (SQLException e) {
            throw new DAOException("Failed to delete request with ID: " + requestId, e);
        } finally {
            dbConnection.closeConnection(conn);
        }
    }

    @Override
    public UpdateRequest findById(int requestId) {
        String sql = "SELECT * FROM update_requests WHERE request_id = ?";
        Connection conn = null;

        try {
            conn = dbConnection.getConnection();
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, requestId);

            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return extractFromResultSet(rs);
            }
            return null;

        } catch (Exception e) {
            throw new DAOException("Failed to find request with ID: " + requestId, e);
        } finally {
            dbConnection.closeConnection(conn);
        }
    }

    @Override
    public List<UpdateRequest> findAll() {
        String sql = "SELECT * FROM update_requests ORDER BY requested_at DESC";
        Connection conn = null;
        List<UpdateRequest> requests = new ArrayList<>();

        try {
            conn = dbConnection.getConnection();
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(sql);

            while (rs.next()) {
                requests.add(extractFromResultSet(rs));
            }
            return requests;

        } catch (Exception e) {
            throw new DAOException("Failed to retrieve all update requests", e);
        } finally {
            dbConnection.closeConnection(conn);
        }
    }

    @Override
    public List<UpdateRequest> findByStatus(RequestStatus status) {
        String sql = "SELECT * FROM update_requests WHERE status = ? ORDER BY requested_at DESC";
        Connection conn = null;
        List<UpdateRequest> requests = new ArrayList<>();

        try {
            conn = dbConnection.getConnection();
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, status.name());

            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                requests.add(extractFromResultSet(rs));
            }
            return requests;

        } catch (Exception e) {
            throw new DAOException("Failed to find requests by status: " + status, e);
        } finally {
            dbConnection.closeConnection(conn);
        }
    }

    @Override
    public List<UpdateRequest> findByRequestedBy(int userId) {
        String sql = "SELECT * FROM update_requests WHERE requested_by = ? ORDER BY requested_at DESC";
        Connection conn = null;
        List<UpdateRequest> requests = new ArrayList<>();

        try {
            conn = dbConnection.getConnection();
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, userId);

            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                requests.add(extractFromResultSet(rs));
            }
            return requests;

        } catch (Exception e) {
            throw new DAOException("Failed to find requests for user ID: " + userId, e);
        } finally {
            dbConnection.closeConnection(conn);
        }
    }

    @Override
    public int countByStatus(RequestStatus status) {
        String sql = "SELECT COUNT(*) FROM update_requests WHERE status = ?";
        Connection conn = null;

        try {
            conn = dbConnection.getConnection();
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, status.name());

            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1);
            }
            return 0;

        } catch (SQLException e) {
            throw new DAOException("Failed to count requests by status: " + status, e);
        } finally {
            dbConnection.closeConnection(conn);
        }
    }

    @Override
    public List<UpdateRequest> findByEntity(String entityType, int entityId) {
        String sql = "SELECT * FROM update_requests WHERE entity_type = ? AND entity_id = ? " +
                    "ORDER BY requested_at DESC";
        Connection conn = null;
        List<UpdateRequest> requests = new ArrayList<>();

        try {
            conn = dbConnection.getConnection();
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, entityType);
            pstmt.setInt(2, entityId);

            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                requests.add(extractFromResultSet(rs));
            }
            return requests;

        } catch (Exception e) {
            throw new DAOException("Failed to find requests for entity: " + entityType + " ID: " + entityId, e);
        } finally {
            dbConnection.closeConnection(conn);
        }
    }

    @Override
    public List<UpdateRequest> findPendingByEntity(String entityType, int entityId) {
        String sql = "SELECT * FROM update_requests WHERE entity_type = ? AND entity_id = ? " +
                    "AND status = 'PENDING' ORDER BY requested_at DESC";
        Connection conn = null;
        List<UpdateRequest> requests = new ArrayList<>();

        try {
            conn = dbConnection.getConnection();
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, entityType);
            pstmt.setInt(2, entityId);

            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                requests.add(extractFromResultSet(rs));
            }
            return requests;

        } catch (Exception e) {
            throw new DAOException("Failed to find pending requests for entity: " + entityType + " ID: " + entityId, e);
        } finally {
            dbConnection.closeConnection(conn);
        }
    }

    @Override
    public List<UpdateRequest> findByDateRange(LocalDate start, LocalDate end) {
        String sql = "SELECT * FROM update_requests WHERE DATE(requested_at) BETWEEN ? AND ? " +
                    "ORDER BY requested_at DESC";
        Connection conn = null;
        List<UpdateRequest> requests = new ArrayList<>();

        try {
            conn = dbConnection.getConnection();
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, start.toString());
            pstmt.setString(2, end.toString());

            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                requests.add(extractFromResultSet(rs));
            }
            return requests;

        } catch (Exception e) {
            throw new DAOException("Failed to find requests in date range: " + start + " to " + end, e);
        } finally {
            dbConnection.closeConnection(conn);
        }
    }

    private UpdateRequest extractFromResultSet(ResultSet rs) throws Exception {
        Map<String, Function<Object, Object>> transformers = new HashMap<>();
        
        // Transform status string to enum
        transformers.put("status", value -> 
            value != null ? RequestStatus.valueOf(value.toString()) : RequestStatus.PENDING
        );

        return ResultSetExtractor.extractWithTransformers(rs, UpdateRequest.class, transformers);
    }
}