package com.studenttracker.dao.impl;

import com.studenttracker.dao.MissionDraftDAO;
import com.studenttracker.exception.DAOException;
import com.studenttracker.model.MissionDraft;
import com.studenttracker.util.DatabaseConnection;
import com.studenttracker.util.ResultSetExtractor;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class MissionDraftDAOImpl implements MissionDraftDAO {
    private final DatabaseConnection dbConnection;

    public MissionDraftDAOImpl() {
        this.dbConnection = DatabaseConnection.getInstance();
    }

    @Override
    public Integer insert(MissionDraft draft) {
        String sql = "INSERT INTO mission_drafts (mission_id, draft_data, last_saved) VALUES (?, ?, ?)";
        Connection conn = null;

        try {
            conn = dbConnection.getConnection();
            PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            
            pstmt.setInt(1, draft.getMissionId());
            pstmt.setString(2, draft.getDraftData());
            pstmt.setObject(3, draft.getLastSaved());

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
            if (e.getMessage().contains("UNIQUE constraint failed")) {
                throw new DAOException("Draft already exists for mission ID: " + draft.getMissionId(), e);
            }
            throw new DAOException("Failed to insert mission draft", e);
        } finally {
            dbConnection.closeConnection(conn);
        }
    }

    @Override
    public boolean update(MissionDraft draft) {
        String sql = "UPDATE mission_drafts SET draft_data = ?, last_saved = ? WHERE draft_id = ?";
        Connection conn = null;

        try {
            conn = dbConnection.getConnection();
            PreparedStatement pstmt = conn.prepareStatement(sql);
            
            pstmt.setString(1, draft.getDraftData());
            pstmt.setObject(2, draft.getLastSaved());
            pstmt.setInt(3, draft.getDraftId());

            return pstmt.executeUpdate() > 0;

        } catch (SQLException e) {
            throw new DAOException("Failed to update mission draft with ID: " + draft.getDraftId(), e);
        } finally {
            dbConnection.closeConnection(conn);
        }
    }

    @Override
    public boolean delete(int draftId) {
        String sql = "DELETE FROM mission_drafts WHERE draft_id = ?";
        Connection conn = null;

        try {
            conn = dbConnection.getConnection();
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, draftId);

            return pstmt.executeUpdate() > 0;

        } catch (SQLException e) {
            throw new DAOException("Failed to delete mission draft with ID: " + draftId, e);
        } finally {
            dbConnection.closeConnection(conn);
        }
    }

    @Override
    public MissionDraft findById(int draftId) {
        String sql = "SELECT * FROM mission_drafts WHERE draft_id = ?";
        Connection conn = null;

        try {
            conn = dbConnection.getConnection();
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, draftId);

            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return extractFromResultSet(rs);
            }
            return null;

        } catch (Exception e) {
            throw new DAOException("Failed to find mission draft with ID: " + draftId, e);
        } finally {
            dbConnection.closeConnection(conn);
        }
    }

    @Override
    public List<MissionDraft> findAll() {
        String sql = "SELECT * FROM mission_drafts ORDER BY last_saved DESC";
        Connection conn = null;
        List<MissionDraft> drafts = new ArrayList<>();

        try {
            conn = dbConnection.getConnection();
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(sql);

            while (rs.next()) {
                drafts.add(extractFromResultSet(rs));
            }
            return drafts;

        } catch (Exception e) {
            throw new DAOException("Failed to retrieve all mission drafts", e);
        } finally {
            dbConnection.closeConnection(conn);
        }
    }

    @Override
    public MissionDraft findByMissionId(int missionId) {
        String sql = "SELECT * FROM mission_drafts WHERE mission_id = ?";
        Connection conn = null;

        try {
            conn = dbConnection.getConnection();
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, missionId);

            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return extractFromResultSet(rs);
            }
            return null;

        } catch (Exception e) {
            throw new DAOException("Failed to find mission draft for mission ID: " + missionId, e);
        } finally {
            dbConnection.closeConnection(conn);
        }
    }

    @Override
    public boolean upsert(MissionDraft draft) {
        Connection conn = null;

        try {
            conn = dbConnection.getConnection();
            
            // Check if draft exists for this mission
            MissionDraft existing = findByMissionId(draft.getMissionId());
            
            if (existing != null) {
                // Update existing draft
                draft.setDraftId(existing.getDraftId());
                return update(draft);
            } else {
                // Insert new draft
                Integer id = insert(draft);
                draft.setDraftId(id);
                return id != null;
            }

        } catch (Exception e) {
            throw new DAOException("Failed to upsert mission draft for mission ID: " + draft.getMissionId(), e);
        } finally {
            dbConnection.closeConnection(conn);
        }
    }

    @Override
    public boolean deleteByMissionId(int missionId) {
        String sql = "DELETE FROM mission_drafts WHERE mission_id = ?";
        Connection conn = null;

        try {
            conn = dbConnection.getConnection();
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, missionId);

            return pstmt.executeUpdate() > 0;

        } catch (SQLException e) {
            throw new DAOException("Failed to delete mission draft for mission ID: " + missionId, e);
        } finally {
            dbConnection.closeConnection(conn);
        }
    }

    private MissionDraft extractFromResultSet(ResultSet rs) throws Exception {
        return ResultSetExtractor.extractObjectFromResultSet(rs, MissionDraft.class);
    }
}