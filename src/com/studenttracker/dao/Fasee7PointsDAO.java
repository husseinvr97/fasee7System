package com.studenttracker.dao;

import com.studenttracker.model.Fasee7Points;
import java.math.BigDecimal;
import java.util.List;

public interface Fasee7PointsDAO {
    
    // Standard CRUD operations
    Integer insert(Fasee7Points points);
    boolean update(Fasee7Points points);
    boolean delete(int pointsId);
    Fasee7Points findById(int pointsId);
    List<Fasee7Points> findAll();
    
    // Custom methods
    Fasee7Points findByStudentId(int studentId);
    boolean upsert(Fasee7Points points);
    List<Fasee7Points> findAllOrderedByTotal();
    int getRankByStudentId(int studentId);
    List<Fasee7Points> getTopN(int limit);
    List<Fasee7Points> findByMinPoints(BigDecimal minPoints);
}