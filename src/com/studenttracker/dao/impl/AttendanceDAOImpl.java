package com.studenttracker.dao.impl;

import com.studenttracker.dao.AttendanceDAO;
import com.studenttracker.exception.DAOException;
import com.studenttracker.model.Attendance;
import com.studenttracker.util.DatabaseConnection;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class AttendanceDAOImpl implements AttendanceDAO {
    
    private final DatabaseConnection dbConnection = DatabaseConnection.getInstance();
    private static final int BATCH_SIZE = 100;
    
    @Override
    public Integer insert(Attendance attendance) {
        String sql = "INSERT INTO attendance (lesson_id, student_id, status, entered_at, entered_by) " +
                     "VALUES (?, ?, ?, ?, ?)";
        Connection conn = null;
        try {
            conn = dbConnection.getConnection();
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
            
            ResultSet generatedKeys = pstmt.getGeneratedKeys();
            if (generatedKeys.next()) {
                return generatedKeys.getInt(1);
            } else {
                throw new DAOException("Insert attendance failed, no ID obtained");
            }
        } catch (SQLException e) {
            throw new DAOException("Failed to insert attendance", e);
        } finally {
            dbConnection.closeConnection(conn);
        }
    }
    
    @Override
    public boolean update(Attendance attendance) {
        String sql = "UPDATE attendance SET status = ?, entered_at = ?, entered_by = ? " +
                     "WHERE attendance_id = ?";
        Connection conn = null;
        try {
            conn = dbConnection.getConnection();
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, attendance.getStatus().name());
            pstmt.setString(2, attendance.getMarkedAt().toString());
            pstmt.setInt(3, attendance.getMarkedBy());
            pstmt.setInt(4, attendance.getAttendanceId());
            
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new DAOException("Failed to update attendance ID: " + attendance.getAttendanceId(), e);
        } finally {
            dbConnection.closeConnection(conn);
        }
    }
    
    @Override
    public boolean delete(int attendanceId) {
        String sql = "DELETE FROM attendance WHERE attendance_id = ?";
        Connection conn = null;
        try {
            conn = dbConnection.getConnection();
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, attendanceId);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new DAOException("Failed to delete attendance ID: " + attendanceId, e);
        } finally {
            dbConnection.closeConnection(conn);
        }
    }
    
    @Override
    public Attendance findById(int attendanceId) {
        String sql = "SELECT * FROM attendance WHERE attendance_id = ?";
        Connection conn = null;
        try {
            conn = dbConnection.getConnection();
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, attendanceId);
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                return mapResultSetToAttendance(rs);
            }
            return null;
        } catch (SQLException e) {
            throw new DAOException("Failed to find attendance by ID: " + attendanceId, e);
        } finally {
            dbConnection.closeConnection(conn);
        }
    }
    
    @Override
    public List<Attendance> findAll() {
        String sql = "SELECT * FROM attendance ORDER BY entered_at DESC";
        Connection conn = null;
        List<Attendance> attendanceList = new ArrayList<>();
        try {
            conn = dbConnection.getConnection();
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(sql);
            
            while (rs.next()) {
                attendanceList.add(mapResultSetToAttendance(rs));
            }
            return attendanceList;
        } catch (SQLException e) {
            throw new DAOException("Failed to retrieve all attendance records", e);
        } finally {
            dbConnection.closeConnection(conn);
        }
    }
    
    @Override
    public List<Attendance> findByLessonId(int lessonId) {
        String sql = "SELECT * FROM attendance WHERE lesson_id = ? ORDER BY student_id";
        Connection conn = null;
        List<Attendance> attendanceList = new ArrayList<>();
        try {
            conn = dbConnection.getConnection();
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, lessonId);
            ResultSet rs = pstmt.executeQuery();
            
            while (rs.next()) {
                attendanceList.add(mapResultSetToAttendance(rs));
            }
            return attendanceList;
        } catch (SQLException e) {
            throw new DAOException("Failed to find attendance by lesson ID: " + lessonId, e);
        } finally {
            dbConnection.closeConnection(conn);
        }
    }
    
    @Override
    public List<Attendance> findByStudentId(int studentId) {
        String sql = "SELECT * FROM attendance WHERE student_id = ? ORDER BY entered_at DESC";
        Connection conn = null;
        List<Attendance> attendanceList = new ArrayList<>();
        try {
            conn = dbConnection.getConnection();
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, studentId);
            ResultSet rs = pstmt.executeQuery();
            
            while (rs.next()) {
                attendanceList.add(mapResultSetToAttendance(rs));
            }
            return attendanceList;
        } catch (SQLException e) {
            throw new DAOException("Failed to find attendance by student ID: " + studentId, e);
        } finally {
            dbConnection.closeConnection(conn);
        }
    }
    
    @Override
    public Attendance findByLessonAndStudent(int lessonId, int studentId) {
        String sql = "SELECT * FROM attendance WHERE lesson_id = ? AND student_id = ?";
        Connection conn = null;
        try {
            conn = dbConnection.getConnection();
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, lessonId);
            pstmt.setInt(2, studentId);
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                return mapResultSetToAttendance(rs);
            }
            return null;
        } catch (SQLException e) {
            throw new DAOException("Failed to find attendance for lesson " + lessonId + 
                                   " and student " + studentId, e);
        } finally {
            dbConnection.closeConnection(conn);
        }
    }
    
    @Override
    public int countByStudentAndStatus(int studentId, Attendance.AttendanceStatus status) {
        String sql = "SELECT COUNT(*) FROM attendance WHERE student_id = ? AND status = ?";
        Connection conn = null;
        try {
            conn = dbConnection.getConnection();
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, studentId);
            pstmt.setString(2, status.name());
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                return rs.getInt(1);
            }
            return 0;
        } catch (SQLException e) {
            throw new DAOException("Failed to count attendance for student " + studentId + 
                                   " with status " + status, e);
        } finally {
            dbConnection.closeConnection(conn);
        }
    }
    
    @Override
    public List<Attendance> findConsecutiveAbsences(int studentId, int limit) {
        String sql = "SELECT a.* FROM attendance a " +
                     "JOIN lessons l ON a.lesson_id = l.lesson_id " +
                     "WHERE a.student_id = ? AND a.status = 'ABSENT' " +
                     "ORDER BY l.lesson_date DESC LIMIT ?";
        Connection conn = null;
        List<Attendance> attendanceList = new ArrayList<>();
        try {
            conn = dbConnection.getConnection();
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, studentId);
            pstmt.setInt(2, limit);
            ResultSet rs = pstmt.executeQuery();
            
            while (rs.next()) {
                attendanceList.add(mapResultSetToAttendance(rs));
            }
            return attendanceList;
        } catch (SQLException e) {
            throw new DAOException("Failed to find consecutive absences for student: " + studentId, e);
        } finally {
            dbConnection.closeConnection(conn);
        }
    }
    
    @Override
    public boolean bulkInsert(List<Attendance> attendanceList) {
        if (attendanceList == null || attendanceList.isEmpty()) {
            return false;
        }
        
        String sql = "INSERT INTO attendance (lesson_id, student_id, status, entered_at, entered_by) " +
                     "VALUES (?, ?, ?, ?, ?)";
        Connection conn = null;
        try {
            conn = dbConnection.getConnection();
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
            
            pstmt.executeBatch();
            conn.commit();
            return true;
        } catch (SQLException e) {
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException ex) {
                    System.err.println("Rollback failed: " + ex.getMessage());
                }
            }
            throw new DAOException("Failed to bulk insert attendance records", e);
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                } catch (SQLException e) {
                    System.err.println("Failed to reset auto-commit: " + e.getMessage());
                }
            }
            dbConnection.closeConnection(conn);
        }
    }
    
    @Override
    public double getAttendanceRate(int studentId) {
        String sql = "SELECT " +
                     "SUM(CASE WHEN status = 'PRESENT' THEN 1 ELSE 0 END) * 100.0 / COUNT(*) as rate " +
                     "FROM attendance WHERE student_id = ?";
        Connection conn = null;
        try {
            conn = dbConnection.getConnection();
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, studentId);
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                return rs.getDouble("rate");
            }
            return 0.0;
        } catch (SQLException e) {
            throw new DAOException("Failed to calculate attendance rate for student: " + studentId, e);
        } finally {
            dbConnection.closeConnection(conn);
        }
    }
    
    private Attendance mapResultSetToAttendance(ResultSet rs) throws SQLException {
        Attendance attendance = new Attendance();
        attendance.setAttendanceId(rs.getInt("attendance_id"));
        attendance.setLessonId(rs.getInt("lesson_id"));
        attendance.setStudentId(rs.getInt("student_id"));
        attendance.setStatus(Attendance.AttendanceStatus.valueOf(rs.getString("status")));
        attendance.setMarkedAt(LocalDateTime.parse(rs.getString("entered_at")));
        attendance.setMarkedBy(rs.getInt("entered_by"));
        return attendance;
    }
}