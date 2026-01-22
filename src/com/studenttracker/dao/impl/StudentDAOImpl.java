package com.studenttracker.dao.impl;

import com.studenttracker.dao.StudentDAO;
import com.studenttracker.exception.DAOException;
import com.studenttracker.model.Student;
import com.studenttracker.model.Student.StudentStatus;
import com.studenttracker.util.DatabaseConnection;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class StudentDAOImpl implements StudentDAO {
    
    private final DatabaseConnection dbConn = DatabaseConnection.getInstance();
    
    @Override
    public Integer insert(Student student) {
        String sql = "INSERT INTO students (full_name, phone_number, whatsapp_number, " +
                    "parent_phone_number, parent_whatsapp_number, registration_date, status) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?)";
        
        Connection conn = null;
        try {
            conn = dbConn.getConnection();
            PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            
            pstmt.setString(1, student.getFullName());
            pstmt.setString(2, student.getPhoneNumber());
            pstmt.setString(3, student.getWhatsappNumber());
            pstmt.setString(4, student.getParentPhoneNumber());
            pstmt.setString(5, student.getParentWhatsappNumber());
            pstmt.setString(6, student.getRegistrationDate().toString());
            pstmt.setString(7, student.getStatus().name());
            
            int affectedRows = pstmt.executeUpdate();
            if (affectedRows == 0) {
                throw new DAOException("Insert student failed, no rows affected");
            }
            
            ResultSet rs = pstmt.getGeneratedKeys();
            if (rs.next()) {
                return rs.getInt(1);
            } else {
                throw new DAOException("Insert student failed, no ID obtained");
            }
            
        } catch (SQLException e) {
            throw new DAOException("Failed to insert student", e);
        } finally {
            dbConn.closeConnection(conn);
        }
    }
    
    @Override
    public boolean update(Student student) {
        String sql = "UPDATE students SET full_name = ?, phone_number = ?, whatsapp_number = ?, " +
                    "parent_phone_number = ?, parent_whatsapp_number = ?, status = ?, " +
                    "archived_at = ?, archived_by = ? WHERE student_id = ?";
        
        Connection conn = null;
        try {
            conn = dbConn.getConnection();
            PreparedStatement pstmt = conn.prepareStatement(sql);
            
            pstmt.setString(1, student.getFullName());
            pstmt.setString(2, student.getPhoneNumber());
            pstmt.setString(3, student.getWhatsappNumber());
            pstmt.setString(4, student.getParentPhoneNumber());
            pstmt.setString(5, student.getParentWhatsappNumber());
            pstmt.setString(6, student.getStatus().name());
            pstmt.setString(7, student.getArchivedAt() != null ? student.getArchivedAt().toString() : null);
            pstmt.setObject(8, student.getArchivedBy());
            pstmt.setInt(9, student.getStudentId());
            
            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;
            
        } catch (SQLException e) {
            throw new DAOException("Failed to update student", e);
        } finally {
            dbConn.closeConnection(conn);
        }
    }
    
    @Override
    public boolean delete(int studentId) {
        String sql = "DELETE FROM students WHERE student_id = ?";
        
        Connection conn = null;
        try {
            conn = dbConn.getConnection();
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, studentId);
            
            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;
            
        } catch (SQLException e) {
            throw new DAOException("Failed to delete student", e);
        } finally {
            dbConn.closeConnection(conn);
        }
    }
    
    @Override
    public Student findById(int studentId) {
        String sql = "SELECT * FROM students WHERE student_id = ?";
        
        Connection conn = null;
        try {
            conn = dbConn.getConnection();
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, studentId);
            
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return extractStudentFromResultSet(rs);
            }
            return null;
            
        } catch (SQLException e) {
            throw new DAOException("Failed to find student by ID", e);
        } finally {
            dbConn.closeConnection(conn);
        }
    }
    
    @Override
    public List<Student> findAll() {
        String sql = "SELECT * FROM students ORDER BY full_name";
        
        Connection conn = null;
        try {
            conn = dbConn.getConnection();
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(sql);
            
            List<Student> students = new ArrayList<>();
            while (rs.next()) {
                students.add(extractStudentFromResultSet(rs));
            }
            return students;
            
        } catch (SQLException e) {
            throw new DAOException("Failed to find all students", e);
        } finally {
            dbConn.closeConnection(conn);
        }
    }
    
    @Override
    public List<Student> findByStatus(StudentStatus status) {
        String sql = "SELECT * FROM students WHERE status = ? ORDER BY full_name";
        
        Connection conn = null;
        try {
            conn = dbConn.getConnection();
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, status.name());
            
            ResultSet rs = pstmt.executeQuery();
            List<Student> students = new ArrayList<>();
            while (rs.next()) {
                students.add(extractStudentFromResultSet(rs));
            }
            return students;
            
        } catch (SQLException e) {
            throw new DAOException("Failed to find students by status", e);
        } finally {
            dbConn.closeConnection(conn);
        }
    }
    
    @Override
    public List<Student> searchByName(String namePart) {
        String sql = "SELECT * FROM students WHERE full_name LIKE ? ORDER BY full_name";
        
        Connection conn = null;
        try {
            conn = dbConn.getConnection();
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, "%" + namePart + "%");
            
            ResultSet rs = pstmt.executeQuery();
            List<Student> students = new ArrayList<>();
            while (rs.next()) {
                students.add(extractStudentFromResultSet(rs));
            }
            return students;
            
        } catch (SQLException e) {
            throw new DAOException("Failed to search students by name", e);
        } finally {
            dbConn.closeConnection(conn);
        }
    }
    
    @Override
    public int countByStatus(StudentStatus status) {
        String sql = "SELECT COUNT(*) FROM students WHERE status = ?";
        
        Connection conn = null;
        try {
            conn = dbConn.getConnection();
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, status.name());
            
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1);
            }
            return 0;
            
        } catch (SQLException e) {
            throw new DAOException("Failed to count students by status", e);
        } finally {
            dbConn.closeConnection(conn);
        }
    }
    
    @Override
    public boolean archive(int studentId, int archivedBy) {
        String sql = "UPDATE students SET status = ?, archived_at = ?, archived_by = ? WHERE student_id = ?";
        
        Connection conn = null;
        try {
            conn = dbConn.getConnection();
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, StudentStatus.ARCHIVED.name());
            pstmt.setString(2, LocalDateTime.now().toString());
            pstmt.setInt(3, archivedBy);
            pstmt.setInt(4, studentId);
            
            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;
            
        } catch (SQLException e) {
            throw new DAOException("Failed to archive student", e);
        } finally {
            dbConn.closeConnection(conn);
        }
    }
    
    @Override
    public boolean restore(int studentId) {
        String sql = "UPDATE students SET status = ?, archived_at = NULL, archived_by = NULL WHERE student_id = ?";
        
        Connection conn = null;
        try {
            conn = dbConn.getConnection();
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, StudentStatus.ACTIVE.name());
            pstmt.setInt(2, studentId);
            
            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;
            
        } catch (SQLException e) {
            throw new DAOException("Failed to restore student", e);
        } finally {
            dbConn.closeConnection(conn);
        }
    }
    
    @Override
    public Student findByPhoneNumber(String phoneNumber) {
        String sql = "SELECT * FROM students WHERE phone_number = ?";
        
        Connection conn = null;
        try {
            conn = dbConn.getConnection();
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, phoneNumber);
            
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return extractStudentFromResultSet(rs);
            }
            return null;
            
        } catch (SQLException e) {
            throw new DAOException("Failed to find student by phone number", e);
        } finally {
            dbConn.closeConnection(conn);
        }
    }
    
    // Helper method to extract Student object from ResultSet
    private Student extractStudentFromResultSet(ResultSet rs) throws SQLException {
        Student student = new Student();
        student.setStudentId(rs.getInt("student_id"));
        student.setFullName(rs.getString("full_name"));
        student.setPhoneNumber(rs.getString("phone_number"));
        student.setWhatsappNumber(rs.getString("whatsapp_number"));
        student.setParentPhoneNumber(rs.getString("parent_phone_number"));
        student.setParentWhatsappNumber(rs.getString("parent_whatsapp_number"));
        
        String regDate = rs.getString("registration_date");
        student.setRegistrationDate(regDate != null ? LocalDateTime.parse(regDate) : null);
        
        student.setStatus(StudentStatus.valueOf(rs.getString("status")));
        
        String archDate = rs.getString("archived_at");
        student.setArchivedAt(archDate != null ? LocalDateTime.parse(archDate) : null);
        
        Integer archBy = (Integer) rs.getObject("archived_by");
        student.setArchivedBy(archBy);
        
        return student;
    }
}