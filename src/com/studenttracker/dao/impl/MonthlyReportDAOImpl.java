package com.studenttracker.dao.impl;

import com.studenttracker.dao.MonthlyReportDAO;
import com.studenttracker.dao.impl.helpers.MonthlyReportDAOImplHelpers;
import com.studenttracker.exception.DAOException;
import com.studenttracker.model.MonthlyReport;
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

public class MonthlyReportDAOImpl implements MonthlyReportDAO {
    
    private final DatabaseConnection dbConn = DatabaseConnection.getInstance();
    private static final Map<String, Function<Object, Object>> transformers = MonthlyReportDAOImplHelpers.getTransformers();
    
    @Override
    public Integer insert(MonthlyReport report) {
        String sql = "INSERT INTO monthly_reports (report_month, report_data, generated_by) " +
                    "VALUES (?, ?, ?)";
        
        Connection conn = null;
        try {
            conn = dbConn.getConnection();
            PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            
            pstmt.setString(1, report.getReportMonth());
            pstmt.setString(2, report.getReportData());
            pstmt.setInt(3, report.getGeneratedBy());
            
            int affectedRows = pstmt.executeUpdate();
            if (affectedRows == 0) {
                throw new DAOException("Insert monthly report failed, no rows affected");
            }
            
            ResultSet rs = pstmt.getGeneratedKeys();
            if (rs.next()) {
                return rs.getInt(1);
            } else {
                throw new DAOException("Insert monthly report failed, no ID obtained");
            }
            
        } catch (SQLException e) {
            throw new DAOException("Failed to insert monthly report", e);
        } finally {
            dbConn.closeConnection(conn);
        }
    }
    
    @Override
    public boolean update(MonthlyReport report) {
        String sql = "UPDATE monthly_reports SET report_month = ?, report_data = ?, " +
                    "generated_by = ? WHERE report_id = ?";
        
        Connection conn = null;
        try {
            conn = dbConn.getConnection();
            PreparedStatement pstmt = conn.prepareStatement(sql);
            
            pstmt.setString(1, report.getReportMonth());
            pstmt.setString(2, report.getReportData());
            pstmt.setInt(3, report.getGeneratedBy());
            pstmt.setInt(4, report.getReportId());
            
            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;
            
        } catch (SQLException e) {
            throw new DAOException("Failed to update monthly report", e);
        } finally {
            dbConn.closeConnection(conn);
        }
    }
    
    @Override
    public boolean delete(int reportId) {
        String sql = "DELETE FROM monthly_reports WHERE report_id = ?";
        
        Connection conn = null;
        try {
            conn = dbConn.getConnection();
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, reportId);
            
            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;
            
        } catch (SQLException e) {
            throw new DAOException("Failed to delete monthly report", e);
        } finally {
            dbConn.closeConnection(conn);
        }
    }
    
    @Override
    public MonthlyReport findById(int reportId) {
        String sql = "SELECT * FROM monthly_reports WHERE report_id = ?";
        
        Connection conn = null;
        try {
            conn = dbConn.getConnection();
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, reportId);
            
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return MonthlyReportDAOImplHelpers.extractMonthlyReportFromResultSet(rs, transformers);
            }
            return null;
            
        } catch (SQLException e) {
            throw new DAOException("Failed to find monthly report by ID", e);
        } finally {
            dbConn.closeConnection(conn);
        }
    }
    
    @Override
    public List<MonthlyReport> findAll() {
        String sql = "SELECT * FROM monthly_reports ORDER BY report_month DESC";
        
        Connection conn = null;
        try {
            conn = dbConn.getConnection();
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(sql);
            
            List<MonthlyReport> reports = new ArrayList<>();
            while (rs.next()) {
                reports.add(MonthlyReportDAOImplHelpers.extractMonthlyReportFromResultSet(rs, transformers));
            }
            return reports;
            
        } catch (SQLException e) {
            throw new DAOException("Failed to find all monthly reports", e);
        } finally {
            dbConn.closeConnection(conn);
        }
    }
    
    @Override
    public MonthlyReport findByMonth(String month) {
        String sql = "SELECT * FROM monthly_reports WHERE report_month = ?";
        
        Connection conn = null;
        try {
            conn = dbConn.getConnection();
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, month);
            
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return MonthlyReportDAOImplHelpers.extractMonthlyReportFromResultSet(rs, transformers);
            }
            return null;
            
        } catch (SQLException e) {
            throw new DAOException("Failed to find monthly report by month", e);
        } finally {
            dbConn.closeConnection(conn);
        }
    }
    
    @Override
    public List<MonthlyReport> findAllOrderedByMonth() {
        String sql = "SELECT * FROM monthly_reports ORDER BY report_month DESC";
        
        Connection conn = null;
        try {
            conn = dbConn.getConnection();
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(sql);
            
            List<MonthlyReport> reports = new ArrayList<>();
            while (rs.next()) {
                reports.add(MonthlyReportDAOImplHelpers.extractMonthlyReportFromResultSet(rs, transformers));
            }
            return reports;
            
        } catch (SQLException e) {
            throw new DAOException("Failed to find all monthly reports ordered by month", e);
        } finally {
            dbConn.closeConnection(conn);
        }
    }
    
    @Override
    public MonthlyReport findLatest() {
        String sql = "SELECT * FROM monthly_reports ORDER BY report_month DESC LIMIT 1";
        
        Connection conn = null;
        try {
            conn = dbConn.getConnection();
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(sql);
            
            if (rs.next()) {
                return MonthlyReportDAOImplHelpers.extractMonthlyReportFromResultSet(rs, transformers);
            }
            return null;
            
        } catch (SQLException e) {
            throw new DAOException("Failed to find latest monthly report", e);
        } finally {
            dbConn.closeConnection(conn);
        }
    }
    
    @Override
    public boolean existsForMonth(String month) {
        String sql = "SELECT COUNT(*) FROM monthly_reports WHERE report_month = ?";
        
        Connection conn = null;
        try {
            conn = dbConn.getConnection();
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, month);
            
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
            return false;
            
        } catch (SQLException e) {
            throw new DAOException("Failed to check if monthly report exists", e);
        } finally {
            dbConn.closeConnection(conn);
        }
    }
}