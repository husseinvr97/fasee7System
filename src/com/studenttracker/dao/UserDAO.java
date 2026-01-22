package com.studenttracker.dao;

import com.studenttracker.model.User;
import com.studenttracker.model.User.UserRole;
import java.util.List;

/**
 * Data Access Object interface for User entity operations.
 */
public interface UserDAO {
    
    // Standard CRUD operations
    Integer insert(User user);
    boolean update(User user);
    boolean delete(int userId);
    User findById(int userId);
    List<User> findAll();
    
    // Custom query methods
    User findByUsername(String username);
    List<User> findByRole(UserRole role);
    boolean usernameExists(String username);
    int countByRole(UserRole role);
}