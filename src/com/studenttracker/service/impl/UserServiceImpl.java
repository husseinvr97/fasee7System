package com.studenttracker.service.impl;

import com.studenttracker.dao.UserDAO;
import com.studenttracker.exception.DuplicateUsernameException;
import com.studenttracker.exception.InvalidCredentialsException;
import com.studenttracker.exception.OperationException;

import com.studenttracker.exception.UserNotFoundException;


import com.studenttracker.model.User;
import com.studenttracker.model.User.UserRole;
import com.studenttracker.service.UserService;
import com.studenttracker.service.validator.AdminPermissionValidator;
import com.studenttracker.service.validator.PasswordValidator;
import com.studenttracker.util.PasswordHashProvider;


import java.util.List;

public class UserServiceImpl implements UserService {
    
    private final UserDAO userDAO;
    
    public UserServiceImpl(UserDAO userDAO) {
        this.userDAO = userDAO;
    }
    
    @Override
    public User login(String username, String plainPassword) {
        User user = userDAO.findByUsername(username);
        
        if (user == null || !user.isActive()) {
            throw new InvalidCredentialsException("Invalid username or password");
        }
        
        if (!PasswordValidator.verifyPassword(plainPassword, user.getPasswordHash())) {
            throw new InvalidCredentialsException("Invalid username or password");
        }
        
        return user;
    }
    
    @Override
    public boolean changePassword(Integer userId, String oldPassword, String newPassword) {
        User user = userDAO.findById(userId);
        
        if (user == null) {
            throw new UserNotFoundException("User not found");
        }
        
        if (!PasswordValidator.verifyPassword(oldPassword, user.getPasswordHash())) {
            throw new InvalidCredentialsException("Current password is incorrect");
        }
        
        PasswordValidator.validatePassword(newPassword);
        
        String newPasswordHash = PasswordHashProvider.hashPassword(newPassword);
        user.setPasswordHash(newPasswordHash);
        
        return userDAO.update(user);
    }
    
    @Override
    public Integer createUser(String username, String plainPassword, String fullName, 
                             UserRole role, Integer createdBy) {
        AdminPermissionValidator.validateAdminPermission(createdBy , userDAO);
        PasswordValidator.validatePassword(plainPassword);
        
        if (userDAO.usernameExists(username)) {
            throw new DuplicateUsernameException("Username '" + username + "' already exists");
        }
        
        String passwordHash = PasswordHashProvider.hashPassword(plainPassword);
        User user = new User(username, passwordHash, fullName, role);
        
        return userDAO.insert(user);
    }
    
    @Override
    public boolean updateUser(Integer userId, String fullName, UserRole role, Integer updatedBy) {
        AdminPermissionValidator.validateAdminPermission(updatedBy , userDAO);
        
        User user = userDAO.findById(userId);
        if (user == null) {
            throw new UserNotFoundException("User not found");
        }
        
        user.setFullName(fullName);
        user.setRole(role);
        
        return userDAO.update(user);
    }
    
    @Override
    public boolean deleteUser(Integer userId, Integer deletedBy) {
        AdminPermissionValidator.validateAdminPermission(deletedBy , userDAO);
        
        if (userId.equals(deletedBy)) {
            throw new OperationException("Cannot delete yourself");
        }
        
        User userToDelete = userDAO.findById(userId);
        if (userToDelete == null) {
            throw new UserNotFoundException("User not found");
        }
        
        if (isLastAdmin(userId)) {
            throw new OperationException("Cannot delete the last admin user");
        }
        
        userToDelete.setActive(false);
        return userDAO.update(userToDelete);
    }
    
    @Override
    public User getUserById(Integer userId) {
        return userDAO.findById(userId);
    }
    
    @Override
    public List<User> getAllUsers() {
        return userDAO.findAll();
    }
    
    @Override
    public List<User> getAdmins() {
        return userDAO.findByRole(UserRole.ADMIN);
    }
    
    @Override
    public List<User> getAssistants() {
        return userDAO.findByRole(UserRole.ASSISTANT);
    }
    
    @Override
    public boolean isUsernameAvailable(String username) {
        return !userDAO.usernameExists(username);
    }
    
    @Override
    public boolean isAdmin(Integer userId) {
        User user = userDAO.findById(userId);
        return user != null && user.isAdmin();
    }
    
    @Override
    public boolean isAssistant(Integer userId) {
        User user = userDAO.findById(userId);
        return user != null && user.isAssistant();
    }
    
    @Override
    public boolean canPerformAction(Integer userId, String action) {
        User user = userDAO.findById(userId);
        if (user == null || !user.isActive()) {
            return false;
        }
        
        // Admin can perform all actions
        if (user.isAdmin()) {
            return true;
        }
        
        // Define assistant permissions
        switch (action) {
            case "VIEW_USERS":
            case "VIEW_STUDENTS":
            case "EDIT_STUDENTS":
            case "TAKE_ATTENDANCE":
            case "GRADE_HOMEWORK":
            case "MANAGE_QUIZZES":
                return true;
            case "CREATE_USER":
            case "DELETE_USER":
            case "EDIT_USER":
            case "DELETE_STUDENT":
                return false;
            default:
                return false;
        }
    }
    
    
    
    
    
    
    
    
    
    private boolean isLastAdmin(Integer userId) {
        User user = userDAO.findById(userId);
        if (user != null && user.isAdmin()) {
            int activeAdminCount = (int) userDAO.findByRole(UserRole.ADMIN)
                .stream()
                .filter(User::isActive)
                .count();
            return activeAdminCount == 1;
        }
        return false;
    }
}