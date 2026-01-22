package com.studenttracker.service;

import com.studenttracker.model.User;
import com.studenttracker.model.User.UserRole;
import java.util.List;

public interface UserService {
    
    // Authentication
    User login(String username, String plainPassword);
    boolean changePassword(Integer userId, String oldPassword, String newPassword);
    
    // User Management (Admin only)
    Integer createUser(String username, String plainPassword, String fullName, 
                      UserRole role, Integer createdBy);
    boolean updateUser(Integer userId, String fullName, UserRole role, Integer updatedBy);
    boolean deleteUser(Integer userId, Integer deletedBy);
    
    // Query methods
    User getUserById(Integer userId);
    List<User> getAllUsers();
    List<User> getAdmins();
    List<User> getAssistants();
    
    // Validation
    boolean isUsernameAvailable(String username);
    
    // Authorization Helpers
    boolean isAdmin(Integer userId);
    boolean isAssistant(Integer userId);
    boolean canPerformAction(Integer userId, String action);
}