package com.studenttracker.model;

import java.time.LocalDateTime;

public class User {
    private Integer userId;
    private String username;
    private String passwordHash;
    private String fullName;
    private UserRole role;
    private LocalDateTime createdAt;
    private boolean isActive;
    
    public enum UserRole {
        ADMIN, ASSISTANT
    }
    
    // Constructors
    public User() {}
    
    public User(String username, String passwordHash, String fullName, UserRole role) {
        this.username = username;
        this.passwordHash = passwordHash;
        this.fullName = fullName;
        this.role = role;
        this.createdAt = LocalDateTime.now();
        this.isActive = true;
    }
    
    // Getters and Setters
    public Integer getUserId() { return userId; }
    public void setUserId(Integer userId) { this.userId = userId; }
    
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    
    public String getPasswordHash() { return passwordHash; }
    public void setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; }
    
    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }
    
    public UserRole getRole() { return role; }
    public void setRole(UserRole role) { this.role = role; }
    
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    
    public boolean isActive() { return isActive; }
    public void setActive(boolean active) { isActive = active; }
    
    // Helper methods
    public boolean isAdmin() {
        return role == UserRole.ADMIN;
    }
    
    public boolean isAssistant() {
        return role == UserRole.ASSISTANT;
    }
    
    @Override
    public String toString() {
        return "User{id=" + userId + ", username='" + username + "', role=" + role + "}";
    }
}