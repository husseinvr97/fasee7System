package com.studenttracker.dao;

import com.studenttracker.model.Fasee7Snapshot;
import java.time.LocalDate;
import java.util.List;

public interface Fasee7SnapshotDAO {
    
    // Standard CRUD methods
    Integer insert(Fasee7Snapshot snapshot);
    boolean update(Fasee7Snapshot snapshot);
    boolean delete(int snapshotId);
    Fasee7Snapshot findById(int snapshotId);
    List<Fasee7Snapshot> findAll();
    
    // Custom methods
    List<Fasee7Snapshot> findByMonth(String month);
    Fasee7Snapshot findByDate(LocalDate date);
    Fasee7Snapshot findLatest();
    List<Fasee7Snapshot> findAllOrderedByDate();
}