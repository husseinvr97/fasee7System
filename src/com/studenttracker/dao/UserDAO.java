package com.studenttracker.dao;

import com.studenttracker.model.User;
import java.util.List;

public interface UserDAO {
    
    // Standard CRUD operations
    Integer insert(User user);
    boolean update(User user);
    boolean delete(int userId);
    User findById(int userId);
    List<User> findAll();
    
    // Custom methods
    User findByUsername(String username);
    List<User> findByRole(User.UserRole role);
    boolean usernameExists(String username);
    int countByRole(User.UserRole role);
}