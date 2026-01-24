package com.studenttracker.dao.impl;

import com.studenttracker.dao.Fasee7SnapshotDAO;
import com.studenttracker.dao.impl.helpers.Fasee7SnapshotDAOImplHelpers;
import com.studenttracker.exception.DAOException;
import com.studenttracker.model.Fasee7Snapshot;
import com.studenttracker.util.DatabaseConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

public class Fasee7SnapshotDAOImpl implements Fasee7SnapshotDAO {
    
    private final DatabaseConnection dbConn = DatabaseConnection.getInstance();
    private static final Map<String, Function<Object, Object>> transformers = Fasee7SnapshotDAOImplHelpers.getTransformers();
    
    @Override
    public Integer insert(Fasee7Snapshot snapshot) {
        String sql = "INSERT INTO fasee7_snapshots (snapshot_date, snapshot_data) VALUES (?, ?)";
        
        Connection conn = null;
        try {
            conn = dbConn.getConnection();
            PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            
            pstmt.setString(1, snapshot.getSnapshotDate().toString());
            pstmt.setString(2, snapshot.getSnapshotData());
            
            int affectedRows = pstmt.executeUpdate();
            if (affectedRows == 0) {
                throw new DAOException("Insert snapshot failed, no rows affected");
            }
            
            ResultSet rs = pstmt.getGeneratedKeys();
            if (rs.next()) {
                return rs.getInt(1);
            } else {
                throw new DAOException("Insert snapshot failed, no ID obtained");
            }
            
        } catch (SQLException e) {
            throw new DAOException("Failed to insert snapshot", e);
        } finally {
            dbConn.closeConnection(conn);
        }
    }
    
    @Override
    public boolean update(Fasee7Snapshot snapshot) {
        String sql = "UPDATE fasee7_snapshots SET snapshot_date = ?, snapshot_data = ? WHERE snapshot_id = ?";
        
        Connection conn = null;
        try {
            conn = dbConn.getConnection();
            PreparedStatement pstmt = conn.prepareStatement(sql);
            
            pstmt.setString(1, snapshot.getSnapshotDate().toString());
            pstmt.setString(2, snapshot.getSnapshotData());
            pstmt.setInt(3, snapshot.getSnapshotId());
            
            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;
            
        } catch (SQLException e) {
            throw new DAOException("Failed to update snapshot", e);
        } finally {
            dbConn.closeConnection(conn);
        }
    }
    
    @Override
    public boolean delete(int snapshotId) {
        String sql = "DELETE FROM fasee7_snapshots WHERE snapshot_id = ?";
        
        Connection conn = null;
        try {
            conn = dbConn.getConnection();
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, snapshotId);
            
            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;
            
        } catch (SQLException e) {
            throw new DAOException("Failed to delete snapshot", e);
        } finally {
            dbConn.closeConnection(conn);
        }
    }
    
    @Override
    public Fasee7Snapshot findById(int snapshotId) {
        String sql = "SELECT * FROM fasee7_snapshots WHERE snapshot_id = ?";
        
        Connection conn = null;
        try {
            conn = dbConn.getConnection();
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, snapshotId);
            
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return Fasee7SnapshotDAOImplHelpers.extractFasee7SnapshotFromResultSet(rs, transformers);
            }
            return null;
            
        } catch (SQLException e) {
            throw new DAOException("Failed to find snapshot by ID", e);
        } finally {
            dbConn.closeConnection(conn);
        }
    }
    
    @Override
    public List<Fasee7Snapshot> findAll() {
        String sql = "SELECT * FROM fasee7_snapshots ORDER BY snapshot_date DESC";
        
        Connection conn = null;
        try {
            conn = dbConn.getConnection();
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(sql);
            
            List<Fasee7Snapshot> snapshots = new ArrayList<>();
            while (rs.next()) {
                snapshots.add(Fasee7SnapshotDAOImplHelpers.extractFasee7SnapshotFromResultSet(rs, transformers));
            }
            return snapshots;
            
        } catch (SQLException e) {
            throw new DAOException("Failed to find all snapshots", e);
        } finally {
            dbConn.closeConnection(conn);
        }
    }
    
    @Override
    public List<Fasee7Snapshot> findByMonth(String month) {
        String sql = "SELECT * FROM fasee7_snapshots WHERE strftime('%Y-%m', snapshot_date) = ? ORDER BY snapshot_date DESC";
        
        Connection conn = null;
        try {
            conn = dbConn.getConnection();
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, month);
            
            ResultSet rs = pstmt.executeQuery();
            List<Fasee7Snapshot> snapshots = new ArrayList<>();
            while (rs.next()) {
                snapshots.add(Fasee7SnapshotDAOImplHelpers.extractFasee7SnapshotFromResultSet(rs, transformers));
            }
            return snapshots;
            
        } catch (SQLException e) {
            throw new DAOException("Failed to find snapshots by month", e);
        } finally {
            dbConn.closeConnection(conn);
        }
    }
    
    @Override
    public Fasee7Snapshot findByDate(LocalDate date) {
        String sql = "SELECT * FROM fasee7_snapshots WHERE snapshot_date = ?";
        
        Connection conn = null;
        try {
            conn = dbConn.getConnection();
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, date.toString());
            
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return Fasee7SnapshotDAOImplHelpers.extractFasee7SnapshotFromResultSet(rs, transformers);
            }
            return null;
            
        } catch (SQLException e) {
            throw new DAOException("Failed to find snapshot by date", e);
        } finally {
            dbConn.closeConnection(conn);
        }
    }
    
    @Override
    public Fasee7Snapshot findLatest() {
        String sql = "SELECT * FROM fasee7_snapshots ORDER BY snapshot_date DESC LIMIT 1";
        
        Connection conn = null;
        try {
            conn = dbConn.getConnection();
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(sql);
            
            if (rs.next()) {
                return Fasee7SnapshotDAOImplHelpers.extractFasee7SnapshotFromResultSet(rs, transformers);
            }
            return null;
            
        } catch (SQLException e) {
            throw new DAOException("Failed to find latest snapshot", e);
        } finally {
            dbConn.closeConnection(conn);
        }
    }
    
    @Override
    public List<Fasee7Snapshot> findAllOrderedByDate() {
        String sql = "SELECT * FROM fasee7_snapshots ORDER BY snapshot_date DESC";
        
        Connection conn = null;
        try {
            conn = dbConn.getConnection();
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(sql);
            
            List<Fasee7Snapshot> snapshots = new ArrayList<>();
            while (rs.next()) {
                snapshots.add(Fasee7SnapshotDAOImplHelpers.extractFasee7SnapshotFromResultSet(rs, transformers));
            }
            return snapshots;
            
        } catch (SQLException e) {
            throw new DAOException("Failed to find all snapshots ordered by date", e);
        } finally {
            dbConn.closeConnection(conn);
        }
    }
}