package com.studenttracker.dao.impl;

import com.studenttracker.dao.AttendanceDAO;
import com.studenttracker.exception.DAOException;
import com.studenttracker.model.Attendance;
import com.studenttracker.model.Attendance.AttendanceStatus;
import com.studenttracker.util.DatabaseConnection;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class AttendanceDAOImpl implements AttendanceDAO {
    
    private final DatabaseConnection dbConn = DatabaseConnection.getInstance();
    private static final int BATCH_SIZE = 100;
    
    @Override
    public Integer insert(Attendance attendance) {
        String sql = "INSERT INTO attendance (lesson_id, student_id, status, marked_at, marked_by) " +
                    "VALUES (?, ?, ?, ?, ?)";
        
        Connection conn = null;
        try {
            conn = dbConn.getConnection();
            PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            
            pstmt.setInt(1, attendance.getLessonId());
            pstmt.setInt(2, attendance.getStudentId());
            pstmt.setString(3, attendance.getStatus().name());
            pstmt.setString(4, attendance.getMarkedAt().toString());
            pstmt.setInt(5, attendance.getMarkedBy());
            
            int affectedRows = pstmt.executeUpdate();
            if (affectedRows == 0) {
                throw new DAOException("Insert attendance failed, no rows affected");
            }
            
            ResultSet rs = pstmt.getGeneratedKeys();
            if (rs.next()) {
                return rs.getInt(1);
            } else {
                throw new DAOException("Insert attendance failed, no ID obtained");
            }
            
        } catch (SQLException e) {
            throw new DAOException("Failed to insert attendance", e);
        } finally {
            dbConn.closeConnection(conn);
        }
    }
    
    @Override
    public boolean update(Attendance attendance) {
        String sql = "UPDATE attendance SET lesson_id = ?, student_id = ?, status = ?, " +
                    "marked_at = ?, marked_by = ? WHERE attendance_id = ?";
        
        Connection conn = null;
        try {
            conn = dbConn.getConnection();
            PreparedStatement pstmt = conn.prepareStatement(sql);
            
            pstmt.setInt(1, attendance.getLessonId());
            pstmt.setInt(2, attendance.getStudentId());
            pstmt.setString(3, attendance.getStatus().name());
            pstmt.setString(4, attendance.getMarkedAt().toString());
            pstmt.setInt(5, attendance.getMarkedBy());
            pstmt.setInt(6, attendance.getAttendanceId());
            
            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;
            
        } catch (SQLException e) {
            throw new DAOException("Failed to update attendance", e);
        } finally {
            dbConn.closeConnection(conn);
        }
    }
    
    @Override
    public boolean delete(int attendanceId) {
        String sql = "DELETE FROM attendance WHERE attendance_id = ?";
        
        Connection conn = null;
        try {
            conn = dbConn.getConnection();
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, attendanceId);
            
            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;
            
        } catch (SQLException e) {
            throw new DAOException("Failed to delete attendance", e);
        } finally {
            dbConn.closeConnection(conn);
        }
    }
    
    @Override
    public Attendance findById(int attendanceId) {
        String sql = "SELECT * FROM attendance WHERE attendance_id = ?";
        
        Connection conn = null;
        try {
            conn = dbConn.getConnection();
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, attendanceId);
            
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return extractAttendanceFromResultSet(rs);
            }
            return null;
            
        } catch (SQLException e) {
            throw new DAOException("Failed to find attendance by ID", e);
        } finally {
            dbConn.closeConnection(conn);
        }
    }
    
    @Override
    public List<Attendance> findAll() {
        String sql = "SELECT * FROM attendance ORDER BY marked_at DESC";
        
        Connection conn = null;
        try {
            conn = dbConn.getConnection();
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(sql);
            
            List<Attendance> attendances = new ArrayList<>();
            while (rs.next()) {
                attendances.add(extractAttendanceFromResultSet(rs));
            }
            return attendances;
            
        } catch (SQLException e) {
            throw new DAOException("Failed to find all attendance records", e);
        } finally {
            dbConn.closeConnection(conn);
        }
    }
    
    @Override
    public List<Attendance> findByLessonId(int lessonId) {
        String sql = "SELECT * FROM attendance WHERE lesson_id = ? ORDER BY student_id";
        
        Connection conn = null;
        try {
            conn = dbConn.getConnection();
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, lessonId);
            
            ResultSet rs = pstmt.executeQuery();
            List<Attendance> attendances = new ArrayList<>();
            while (rs.next()) {
                attendances.add(extractAttendanceFromResultSet(rs));
            }
            return attendances;
            
        } catch (SQLException e) {
            throw new DAOException("Failed to find attendance by lesson ID", e);
        } finally {
            dbConn.closeConnection(conn);
        }
    }
    
    @Override
    public List<Attendance> findByStudentId(int studentId) {
        String sql = "SELECT * FROM attendance WHERE student_id = ? ORDER BY marked_at DESC";
        
        Connection conn = null;
        try {
            conn = dbConn.getConnection();
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, studentId);
            
            ResultSet rs = pstmt.executeQuery();
            List<Attendance> attendances = new ArrayList<>();
            while (rs.next()) {
                attendances.add(extractAttendanceFromResultSet(rs));
            }
            return attendances;
            
        } catch (SQLException e) {
            throw new DAOException("Failed to find attendance by student ID", e);
        } finally {
            dbConn.closeConnection(conn);
        }
    }
    
    @Override
    public Attendance findByLessonAndStudent(int lessonId, int studentId) {
        String sql = "SELECT * FROM attendance WHERE lesson_id = ? AND student_id = ?";
        
        Connection conn = null;
        try {
            conn = dbConn.getConnection();
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, lessonId);
            pstmt.setInt(2, studentId);
            
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return extractAttendanceFromResultSet(rs);
            }
            return null;
            
        } catch (SQLException e) {
            throw new DAOException("Failed to find attendance by lesson and student", e);
        } finally {
            dbConn.closeConnection(conn);
        }
    }
    
    @Override
    public int countByStudentAndStatus(int studentId, AttendanceStatus status) {
        String sql = "SELECT COUNT(*) FROM attendance WHERE student_id = ? AND status = ?";
        
        Connection conn = null;
        try {
            conn = dbConn.getConnection();
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, studentId);
            pstmt.setString(2, status.name());
            
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1);
            }
            return 0;
            
        } catch (SQLException e) {
            throw new DAOException("Failed to count attendance by student and status", e);
        } finally {
            dbConn.closeConnection(conn);
        }
    }
    
    @Override
    public List<Attendance> findConsecutiveAbsences(int studentId, int limit) {
        String sql = "SELECT * FROM attendance WHERE student_id = ? AND status = ? " +
                    "ORDER BY marked_at DESC LIMIT ?";
        
        Connection conn = null;
        try {
            conn = dbConn.getConnection();
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, studentId);
            pstmt.setString(2, AttendanceStatus.ABSENT.name());
            pstmt.setInt(3, limit);
            
            ResultSet rs = pstmt.executeQuery();
            List<Attendance> absences = new ArrayList<>();
            while (rs.next()) {
                absences.add(extractAttendanceFromResultSet(rs));
            }
            return absences;
            
        } catch (SQLException e) {
            throw new DAOException("Failed to find consecutive absences", e);
        } finally {
            dbConn.closeConnection(conn);
        }
    }
    
    @Override
    public boolean bulkInsert(List<Attendance> attendanceList) {
        if (attendanceList == null || attendanceList.isEmpty()) {
            return false;
        }
        
        String sql = "INSERT INTO attendance (lesson_id, student_id, status, marked_at, marked_by) " +
                    "VALUES (?, ?, ?, ?, ?)";
        
        Connection conn = null;
        try {
            conn = dbConn.getConnection();
            conn.setAutoCommit(false);
            PreparedStatement pstmt = conn.prepareStatement(sql);
            
            int count = 0;
            for (Attendance attendance : attendanceList) {
                pstmt.setInt(1, attendance.getLessonId());
                pstmt.setInt(2, attendance.getStudentId());
                pstmt.setString(3, attendance.getStatus().name());
                pstmt.setString(4, attendance.getMarkedAt().toString());
                pstmt.setInt(5, attendance.getMarkedBy());
                
                pstmt.addBatch();
                count++;
                
                if (count % BATCH_SIZE == 0) {
                    pstmt.executeBatch();
                }
            }
            
            pstmt.executeBatch(); // Execute remaining
            conn.commit();
            return true;
            
        } catch (SQLException e) {
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException ex) {
                    throw new DAOException("Failed to rollback bulk insert", ex);
                }
            }
            throw new DAOException("Failed to bulk insert attendance", e);
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                } catch (SQLException e) {
                    // Ignore
                }
            }
            dbConn.closeConnection(conn);
        }
    }
    
    @Override
    public double getAttendanceRate(int studentId) {
        String sql = "SELECT " +
                    "(SELECT COUNT(*) FROM attendance WHERE student_id = ? AND status = ?) * 1.0 / " +
                    "(SELECT COUNT(*) FROM attendance WHERE student_id = ?) AS rate";
        
        Connection conn = null;
        try {
            conn = dbConn.getConnection();
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, studentId);
            pstmt.setString(2, AttendanceStatus.PRESENT.name());
            pstmt.setInt(3, studentId);
            
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getDouble("rate");
            }
            return 0.0;
            
        } catch (SQLException e) {
            throw new DAOException("Failed to calculate attendance rate", e);
        } finally {
            dbConn.closeConnection(conn);
        }
    }
    
    // Helper method to extract Attendance object from ResultSet
    private Attendance extractAttendanceFromResultSet(ResultSet rs) throws SQLException {
        Attendance attendance = new Attendance();
        attendance.setAttendanceId(rs.getInt("attendance_id"));
        attendance.setLessonId(rs.getInt("lesson_id"));
        attendance.setStudentId(rs.getInt("student_id"));
        attendance.setStatus(AttendanceStatus.valueOf(rs.getString("status")));
        
        String markedAt = rs.getString("marked_at");
        attendance.setMarkedAt(markedAt != null ? LocalDateTime.parse(markedAt) : null);
        
        attendance.setMarkedBy(rs.getInt("marked_by"));
        
        return attendance;
    }
}