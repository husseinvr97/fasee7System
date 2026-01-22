package com.studenttracker.model;

import java.time.LocalDateTime;

public class Student {
    // Fields
    private Integer studentId;
    private String fullName;          // الاسم الرباعي
    private String phoneNumber;
    private String whatsappNumber;
    private String parentPhoneNumber;
    private String parentWhatsappNumber;
    private LocalDateTime registrationDate;
    private StudentStatus status;     // ACTIVE or ARCHIVED
    private LocalDateTime archivedAt;
    private Integer archivedBy;       // user_id who archived
    
    // Enums
    public enum StudentStatus {
        ACTIVE, ARCHIVED
    }
    
    // Constructors
    public Student() {}
    
    public Student(String fullName, String phoneNumber, String parentPhoneNumber) {
        this.fullName = fullName;
        this.phoneNumber = phoneNumber;
        this.parentPhoneNumber = parentPhoneNumber;
        this.registrationDate = LocalDateTime.now();
        this.status = StudentStatus.ACTIVE;
    }
    
    // Getters and Setters
    public Integer getStudentId() { return studentId; }
    public void setStudentId(Integer studentId) { this.studentId = studentId; }
    
    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }
    
    public String getPhoneNumber() { return phoneNumber; }
    public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }
    
    public String getWhatsappNumber() { return whatsappNumber; }
    public void setWhatsappNumber(String whatsappNumber) { this.whatsappNumber = whatsappNumber; }
    
    public String getParentPhoneNumber() { return parentPhoneNumber; }
    public void setParentPhoneNumber(String parentPhoneNumber) { this.parentPhoneNumber = parentPhoneNumber; }
    
    public String getParentWhatsappNumber() { return parentWhatsappNumber; }
    public void setParentWhatsappNumber(String parentWhatsappNumber) { this.parentWhatsappNumber = parentWhatsappNumber; }
    
    public LocalDateTime getRegistrationDate() { return registrationDate; }
    public void setRegistrationDate(LocalDateTime registrationDate) { this.registrationDate = registrationDate; }
    
    public StudentStatus getStatus() { return status; }
    public void setStatus(StudentStatus status) { this.status = status; }
    
    public LocalDateTime getArchivedAt() { return archivedAt; }
    public void setArchivedAt(LocalDateTime archivedAt) { this.archivedAt = archivedAt; }
    
    public Integer getArchivedBy() { return archivedBy; }
    public void setArchivedBy(Integer archivedBy) { this.archivedBy = archivedBy; }
    
    // Helper methods
    public boolean isArchived() {
        return status == StudentStatus.ARCHIVED;
    }
    
    public void archive(Integer userId) {
        this.status = StudentStatus.ARCHIVED;
        this.archivedAt = LocalDateTime.now();
        this.archivedBy = userId;
    }
    
    public void restore() {
        this.status = StudentStatus.ACTIVE;
        this.archivedAt = null;
        this.archivedBy = null;
    }
    
    @Override
    public String toString() {
        return "Student{id=" + studentId + ", name='" + fullName + "', status=" + status + "}";
    }
}