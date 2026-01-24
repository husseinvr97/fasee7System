package com.studenttracker.dao.impl;

import com.studenttracker.dao.TargetAchievementStreakDAO;
import com.studenttracker.dao.impl.helpers.TargetAchievementStreakDAOImplHelpers;
import com.studenttracker.exception.DAOException;
import com.studenttracker.model.TargetAchievementStreak;
import com.studenttracker.util.DatabaseConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

public class TargetAchievementStreakDAOImpl implements TargetAchievementStreakDAO {
    
    private final DatabaseConnection dbConn = DatabaseConnection.getInstance();
    private static final Map<String, Function<Object, Object>> transformers = 
        TargetAchievementStreakDAOImplHelpers.getTransformers();
    
    @Override
    public Integer insert(TargetAchievementStreak streak) {
        String sql = "INSERT INTO target_achievement_streak (student_id, current_streak, last_achievement_at, total_points_earned) " +
                    "VALUES (?, ?, ?, ?)";
        
        Connection conn = null;
        try {
            conn = dbConn.getConnection();
            PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            
            pstmt.setInt(1, streak.getStudentId());
            pstmt.setInt(2, streak.getCurrentStreak());
            pstmt.setString(3, streak.getLastAchievementAt() != null ? streak.getLastAchievementAt().toString() : null);
            pstmt.setInt(4, streak.getTotalPointsEarned());
            
            int affectedRows = pstmt.executeUpdate();
            if (affectedRows == 0) {
                throw new DAOException("Insert streak failed, no rows affected");
            }
            
            ResultSet rs = pstmt.getGeneratedKeys();
            if (rs.next()) {
                return rs.getInt(1);
            } else {
                throw new DAOException("Insert streak failed, no ID obtained");
            }
            
        } catch (SQLException e) {
            throw new DAOException("Failed to insert target achievement streak", e);
        } finally {
            dbConn.closeConnection(conn);
        }
    }
    
    @Override
    public boolean update(TargetAchievementStreak streak) {
        String sql = "UPDATE target_achievement_streak SET student_id = ?, current_streak = ?, " +
                    "last_achievement_at = ?, total_points_earned = ? WHERE streak_id = ?";
        
        Connection conn = null;
        try {
            conn = dbConn.getConnection();
            PreparedStatement pstmt = conn.prepareStatement(sql);
            
            pstmt.setInt(1, streak.getStudentId());
            pstmt.setInt(2, streak.getCurrentStreak());
            pstmt.setString(3, streak.getLastAchievementAt() != null ? streak.getLastAchievementAt().toString() : null);
            pstmt.setInt(4, streak.getTotalPointsEarned());
            pstmt.setInt(5, streak.getStreakId());
            
            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;
            
        } catch (SQLException e) {
            throw new DAOException("Failed to update target achievement streak", e);
        } finally {
            dbConn.closeConnection(conn);
        }
    }
    
    @Override
    public boolean delete(int streakId) {
        String sql = "DELETE FROM target_achievement_streak WHERE streak_id = ?";
        
        Connection conn = null;
        try {
            conn = dbConn.getConnection();
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, streakId);
            
            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;
            
        } catch (SQLException e) {
            throw new DAOException("Failed to delete target achievement streak", e);
        } finally {
            dbConn.closeConnection(conn);
        }
    }
    
    @Override
    public TargetAchievementStreak findById(int streakId) {
        String sql = "SELECT * FROM target_achievement_streak WHERE streak_id = ?";
        
        Connection conn = null;
        try {
            conn = dbConn.getConnection();
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, streakId);
            
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return TargetAchievementStreakDAOImplHelpers.extractStreakFromResultSet(rs, transformers);
            }
            return null;
            
        } catch (SQLException e) {
            throw new DAOException("Failed to find streak by ID", e);
        } finally {
            dbConn.closeConnection(conn);
        }
    }
    
    @Override
    public List<TargetAchievementStreak> findAll() {
        String sql = "SELECT * FROM target_achievement_streak ORDER BY current_streak DESC";
        
        Connection conn = null;
        try {
            conn = dbConn.getConnection();
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(sql);
            
            List<TargetAchievementStreak> streaks = new ArrayList<>();
            while (rs.next()) {
                streaks.add(TargetAchievementStreakDAOImplHelpers.extractStreakFromResultSet(rs, transformers));
            }
            return streaks;
            
        } catch (SQLException e) {
            throw new DAOException("Failed to find all streaks", e);
        } finally {
            dbConn.closeConnection(conn);
        }
    }
    
    @Override
    public TargetAchievementStreak findByStudentId(int studentId) {
        String sql = "SELECT * FROM target_achievement_streak WHERE student_id = ?";
        
        Connection conn = null;
        try {
            conn = dbConn.getConnection();
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, studentId);
            
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return TargetAchievementStreakDAOImplHelpers.extractStreakFromResultSet(rs, transformers);
            }
            return null;
            
        } catch (SQLException e) {
            throw new DAOException("Failed to find streak by student ID", e);
        } finally {
            dbConn.closeConnection(conn);
        }
    }
    
    @Override
    public boolean upsert(TargetAchievementStreak streak) {
        String sql = "INSERT INTO target_achievement_streak (student_id, current_streak, last_achievement_at, total_points_earned) " +
                    "VALUES (?, ?, ?, ?) " +
                    "ON CONFLICT(student_id) DO UPDATE SET " +
                    "current_streak = excluded.current_streak, " +
                    "last_achievement_at = excluded.last_achievement_at, " +
                    "total_points_earned = excluded.total_points_earned";
        
        Connection conn = null;
        try {
            conn = dbConn.getConnection();
            PreparedStatement pstmt = conn.prepareStatement(sql);
            
            pstmt.setInt(1, streak.getStudentId());
            pstmt.setInt(2, streak.getCurrentStreak());
            pstmt.setString(3, streak.getLastAchievementAt() != null ? streak.getLastAchievementAt().toString() : null);
            pstmt.setInt(4, streak.getTotalPointsEarned());
            
            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;
            
        } catch (SQLException e) {
            throw new DAOException("Failed to upsert target achievement streak", e);
        } finally {
            dbConn.closeConnection(conn);
        }
    }
    
    @Override
    public List<TargetAchievementStreak> findByMinStreak(int minStreak) {
        String sql = "SELECT * FROM target_achievement_streak WHERE current_streak >= ? ORDER BY current_streak DESC";
        
        Connection conn = null;
        try {
            conn = dbConn.getConnection();
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, minStreak);
            
            ResultSet rs = pstmt.executeQuery();
            List<TargetAchievementStreak> streaks = new ArrayList<>();
            while (rs.next()) {
                streaks.add(TargetAchievementStreakDAOImplHelpers.extractStreakFromResultSet(rs, transformers));
            }
            return streaks;
            
        } catch (SQLException e) {
            throw new DAOException("Failed to find streaks by minimum value", e);
        } finally {
            dbConn.closeConnection(conn);
        }
    }
    
    @Override
    public List<TargetAchievementStreak> getTopStreaks(int limit) {
        String sql = "SELECT * FROM target_achievement_streak ORDER BY current_streak DESC LIMIT ?";
        
        Connection conn = null;
        try {
            conn = dbConn.getConnection();
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, limit);
            
            ResultSet rs = pstmt.executeQuery();
            List<TargetAchievementStreak> streaks = new ArrayList<>();
            while (rs.next()) {
                streaks.add(TargetAchievementStreakDAOImplHelpers.extractStreakFromResultSet(rs, transformers));
            }
            return streaks;
            
        } catch (SQLException e) {
            throw new DAOException("Failed to get top streaks", e);
        } finally {
            dbConn.closeConnection(conn);
        }
    }
}