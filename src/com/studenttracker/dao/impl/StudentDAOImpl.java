package com.studenttracker.dao.impl;

import com.studenttracker.dao.StudentDAO;
import com.studenttracker.exception.DAOException;
import com.studenttracker.model.Student;
import com.studenttracker.util.DatabaseConnection;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class StudentDAOImpl implements StudentDAO {
    
    private final DatabaseConnection dbConnection = DatabaseConnection.getInstance();
    
    @Override
    public Integer insert(Student student) {
        String sql = "INSERT INTO students (student_name, phone_number, status, created_at, archived_at, archived_by) " +
                     "VALUES (?, ?, ?, ?, ?, ?)";
        Connection conn = null;
        try {
            conn = dbConnection.getConnection();
            PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            pstmt.setString(1, student.getFullName());
            pstmt.setString(2, student.getPhoneNumber());
            pstmt.setString(3, student.getStatus().name());
            pstmt.setString(4, student.getRegistrationDate().toString());
            pstmt.setString(5, student.getArchivedAt() != null ? student.getArchivedAt().toString() : null);
            if (student.getArchivedBy() != null) {
                pstmt.setInt(6, student.getArchivedBy());
            } else {
                pstmt.setNull(6, Types.INTEGER);
            }
            
            int affectedRows = pstmt.executeUpdate();
            if (affectedRows == 0) {
                throw new DAOException("Insert student failed, no rows affected");
            }
            
            ResultSet generatedKeys = pstmt.getGeneratedKeys();
            if (generatedKeys.next()) {
                return generatedKeys.getInt(1);
            } else {
                throw new DAOException("Insert student failed, no ID obtained");
            }
        } catch (SQLException e) {
            throw new DAOException("Failed to insert student: " + student.getFullName(), e);
        } finally {
            dbConnection.closeConnection(conn);
        }
    }
    
    @Override
    public boolean update(Student student) {
        String sql = "UPDATE students SET student_name = ?, phone_number = ?, status = ?, " +
                     "archived_at = ?, archived_by = ? WHERE student_id = ?";
        Connection conn = null;
        try {
            conn = dbConnection.getConnection();
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, student.getFullName());
            pstmt.setString(2, student.getPhoneNumber());
            pstmt.setString(3, student.getStatus().name());
            pstmt.setString(4, student.getArchivedAt() != null ? student.getArchivedAt().toString() : null);
            if (student.getArchivedBy() != null) {
                pstmt.setInt(5, student.getArchivedBy());
            } else {
                pstmt.setNull(5, Types.INTEGER);
            }
            pstmt.setInt(6, student.getStudentId());
            
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new DAOException("Failed to update student ID: " + student.getStudentId(), e);
        } finally {
            dbConnection.closeConnection(conn);
        }
    }
    
    @Override
    public boolean delete(int studentId) {
        String sql = "DELETE FROM students WHERE student_id = ?";
        Connection conn = null;
        try {
            conn = dbConnection.getConnection();
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, studentId);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new DAOException("Failed to delete student ID: " + studentId, e);
        } finally {
            dbConnection.closeConnection(conn);
        }
    }
    
    @Override
    public Student findById(int studentId) {
        String sql = "SELECT * FROM students WHERE student_id = ?";
        Connection conn = null;
        try {
            conn = dbConnection.getConnection();
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, studentId);
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                return mapResultSetToStudent(rs);
            }
            return null;
        } catch (SQLException e) {
            throw new DAOException("Failed to find student by ID: " + studentId, e);
        } finally {
            dbConnection.closeConnection(conn);
        }
    }
    
    @Override
    public List<Student> findAll() {
        String sql = "SELECT * FROM students ORDER BY student_name";
        Connection conn = null;
        List<Student> students = new ArrayList<>();
        try {
            conn = dbConnection.getConnection();
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(sql);
            
            while (rs.next()) {
                students.add(mapResultSetToStudent(rs));
            }
            return students;
        } catch (SQLException e) {
            throw new DAOException("Failed to retrieve all students", e);
        } finally {
            dbConnection.closeConnection(conn);
        }
    }
    
    @Override
    public List<Student> findByStatus(Student.StudentStatus status) {
        String sql = "SELECT * FROM students WHERE status = ? ORDER BY student_name";
        Connection conn = null;
        List<Student> students = new ArrayList<>();
        try {
            conn = dbConnection.getConnection();
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, status.name());
            ResultSet rs = pstmt.executeQuery();
            
            while (rs.next()) {
                students.add(mapResultSetToStudent(rs));
            }
            return students;
        } catch (SQLException e) {
            throw new DAOException("Failed to find students by status: " + status, e);
        } finally {
            dbConnection.closeConnection(conn);
        }
    }
    
    @Override
    public List<Student> searchByName(String namePart) {
        String sql = "SELECT * FROM students WHERE student_name LIKE ? ORDER BY student_name";
        Connection conn = null;
        List<Student> students = new ArrayList<>();
        try {
            conn = dbConnection.getConnection();
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, "%" + namePart + "%");
            ResultSet rs = pstmt.executeQuery();
            
            while (rs.next()) {
                students.add(mapResultSetToStudent(rs));
            }
            return students;
        } catch (SQLException e) {
            throw new DAOException("Failed to search students by name: " + namePart, e);
        } finally {
            dbConnection.closeConnection(conn);
        }
    }
    
    @Override
    public int countByStatus(Student.StudentStatus status) {
        String sql = "SELECT COUNT(*) FROM students WHERE status = ?";
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
            throw new DAOException("Failed to count students by status: " + status, e);
        } finally {
            dbConnection.closeConnection(conn);
        }
    }
    
    @Override
    public boolean archive(int studentId, int archivedBy) {
        String sql = "UPDATE students SET status = 'ARCHIVED', archived_at = ?, archived_by = ? " +
                     "WHERE student_id = ?";
        Connection conn = null;
        try {
            conn = dbConnection.getConnection();
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, LocalDateTime.now().toString());
            pstmt.setInt(2, archivedBy);
            pstmt.setInt(3, studentId);
            
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new DAOException("Failed to archive student ID: " + studentId, e);
        } finally {
            dbConnection.closeConnection(conn);
        }
    }
    
    @Override
    public boolean restore(int studentId) {
        String sql = "UPDATE students SET status = 'ACTIVE', archived_at = NULL, archived_by = NULL " +
                     "WHERE student_id = ?";
        Connection conn = null;
        try {
            conn = dbConnection.getConnection();
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, studentId);
            
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new DAOException("Failed to restore student ID: " + studentId, e);
        } finally {
            dbConnection.closeConnection(conn);
        }
    }
    
    @Override
    public Student findByPhoneNumber(String phoneNumber) {
        String sql = "SELECT * FROM students WHERE phone_number = ?";
        Connection conn = null;
        try {
            conn = dbConnection.getConnection();
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, phoneNumber);
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                return mapResultSetToStudent(rs);
            }
            return null;
        } catch (SQLException e) {
            throw new DAOException("Failed to find student by phone: " + phoneNumber, e);
        } finally {
            dbConnection.closeConnection(conn);
        }
    }
    
    private Student mapResultSetToStudent(ResultSet rs) throws SQLException {
        Student student = new Student();
        student.setStudentId(rs.getInt("student_id"));
        student.setFullName(rs.getString("student_name"));
        student.setPhoneNumber(rs.getString("phone_number"));
        student.setStatus(Student.StudentStatus.valueOf(rs.getString("status")));
        student.setRegistrationDate(LocalDateTime.parse(rs.getString("created_at")));
        
        String archivedAt = rs.getString("archived_at");
        if (archivedAt != null) {
            student.setArchivedAt(LocalDateTime.parse(archivedAt));
        }
        
        int archivedBy = rs.getInt("archived_by");
        if (!rs.wasNull()) {
            student.setArchivedBy(archivedBy);
        }
        
        return student;
    }
}