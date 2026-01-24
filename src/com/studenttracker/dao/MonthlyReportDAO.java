package com.studenttracker.dao;

import com.studenttracker.model.MonthlyReport;
import java.util.List;

public interface MonthlyReportDAO {
    
    // Standard CRUD methods
    Integer insert(MonthlyReport report);
    boolean update(MonthlyReport report);
    boolean delete(int reportId);
    MonthlyReport findById(int reportId);
    List<MonthlyReport> findAll();
    
    // Custom methods
    MonthlyReport findByMonth(String month);
    List<MonthlyReport> findAllOrderedByMonth();
    MonthlyReport findLatest();
    boolean existsForMonth(String month);
}