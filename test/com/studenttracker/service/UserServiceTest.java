package com.studenttracker.service;

import com.studenttracker.dao.UserDAO;
import com.studenttracker.exception.*;
import com.studenttracker.model.User;
import com.studenttracker.model.User.UserRole;
import com.studenttracker.service.impl.UserServiceImpl;
import com.studenttracker.util.PasswordHashProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@DisplayName("UserService Tests")
public class UserServiceTest {

    @Mock
    private UserDAO userDAO;

    private UserService userService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        userService = new UserServiceImpl(userDAO);
    }

    // ==================== Test 2.1: Login - Valid Credentials ====================
    @Test
    @DisplayName("Test 2.1: Login with valid credentials should return User object")
    void testLogin_ValidCredentials_ReturnsUser() {
        // Arrange
        String username = "admin1";
        String plainPassword = "Password123!";
        String hashedPassword = PasswordHashProvider.hashPassword(plainPassword);
        
        User mockUser = new User(username, hashedPassword, "Admin User", UserRole.ADMIN);
        mockUser.setUserId(1);
        mockUser.setActive(true);
        
        when(userDAO.findByUsername(username)).thenReturn(mockUser);
        
        // Act
        User result = userService.login(username, plainPassword);
        
        // Assert
        assertNotNull(result);
        assertEquals(username, result.getUsername());
        assertEquals(UserRole.ADMIN, result.getRole());
        verify(userDAO, times(1)).findByUsername(username);
    }

    // ==================== Test 2.2: Login - Invalid Username ====================
    @Test
    @DisplayName("Test 2.2: Login with invalid username should throw InvalidCredentialsException")
    void testLogin_InvalidUsername_ThrowsException() {
        // Arrange
        String username = "nonexistent";
        String plainPassword = "Password123!";
        
        when(userDAO.findByUsername(username)).thenReturn(null);
        
        // Act & Assert
        InvalidCredentialsException exception = assertThrows(
            InvalidCredentialsException.class,
            () -> userService.login(username, plainPassword)
        );
        
        assertEquals("Invalid username or password", exception.getMessage());
        verify(userDAO, times(1)).findByUsername(username);
    }

    // ==================== Test 2.3: Login - Invalid Password ====================
    @Test
    @DisplayName("Test 2.3: Login with invalid password should throw InvalidCredentialsException")
    void testLogin_InvalidPassword_ThrowsException() {
        // Arrange
        String username = "admin1";
        String correctPassword = "Password123!";
        String wrongPassword = "WrongPass456!";
        String hashedPassword = PasswordHashProvider.hashPassword(correctPassword);
        
        User mockUser = new User(username, hashedPassword, "Admin User", UserRole.ADMIN);
        mockUser.setUserId(1);
        mockUser.setActive(true);
        
        when(userDAO.findByUsername(username)).thenReturn(mockUser);
        
        // Act & Assert
        InvalidCredentialsException exception = assertThrows(
            InvalidCredentialsException.class,
            () -> userService.login(username, wrongPassword)
        );
        
        assertEquals("Invalid username or password", exception.getMessage());
        verify(userDAO, times(1)).findByUsername(username);
    }

    // ==================== Test 2.4: Create User - Admin Creates Assistant ====================
    @Test
    @DisplayName("Test 2.4: Admin creates assistant user successfully")
    void testCreateUser_AdminCreatesAssistant_Success() {
        // Arrange
        String username = "assistant1";
        String plainPassword = "Pass123!";
        String fullName = "Ahmed Ali";
        UserRole role = UserRole.ASSISTANT;
        Integer createdBy = 1;
        
        User adminUser = new User("admin1", "hash", "Admin", UserRole.ADMIN);
        adminUser.setUserId(1);
        
        when(userDAO.findById(createdBy)).thenReturn(adminUser);
        when(userDAO.usernameExists(username)).thenReturn(false);
        when(userDAO.insert(any(User.class))).thenReturn(5);
        
        // Act
        Integer userId = userService.createUser(username, plainPassword, fullName, role, createdBy);
        
        // Assert
        assertNotNull(userId);
        assertEquals(5, userId);
        verify(userDAO, times(1)).findById(createdBy);
        verify(userDAO, times(1)).usernameExists(username);
        verify(userDAO, times(1)).insert(any(User.class));
    }

    // ==================== Test 2.5: Create User - Non-Admin Tries ====================
    @Test
    @DisplayName("Test 2.5: Non-admin trying to create user should throw UnauthorizedException")
    void testCreateUser_NonAdmin_ThrowsUnauthorizedException() {
        // Arrange
        Integer createdBy = 5;
        User assistantUser = new User("assistant", "hash", "Assistant", UserRole.ASSISTANT);
        assistantUser.setUserId(5);
        
        when(userDAO.findById(createdBy)).thenReturn(assistantUser);
        
        // Act & Assert
        UnauthorizedException exception = assertThrows(
            UnauthorizedException.class,
            () -> userService.createUser("newuser", "Pass123!", "New User", UserRole.ASSISTANT, createdBy)
        );
        
        assertEquals("Only administrators can perform this action", exception.getMessage());
        verify(userDAO, times(1)).findById(createdBy);
        verify(userDAO, never()).usernameExists(anyString());
        verify(userDAO, never()).insert(any(User.class));
    }

    // ==================== Test 2.6: Create User - Duplicate Username ====================
    @Test
    @DisplayName("Test 2.6: Creating user with duplicate username should throw DuplicateUsernameException")
    void testCreateUser_DuplicateUsername_ThrowsException() {
        // Arrange
        String username = "admin1";
        Integer createdBy = 1;
        
        User adminUser = new User("admin1", "hash", "Admin", UserRole.ADMIN);
        adminUser.setUserId(1);
        
        when(userDAO.findById(createdBy)).thenReturn(adminUser);
        when(userDAO.usernameExists(username)).thenReturn(true);
        
        // Act & Assert
        DuplicateUsernameException exception = assertThrows(
            DuplicateUsernameException.class,
            () -> userService.createUser(username, "Pass123!", "Test User", UserRole.ASSISTANT, createdBy)
        );
        
        assertTrue(exception.getMessage().contains("already exists"));
        verify(userDAO, times(1)).findById(createdBy);
        verify(userDAO, times(1)).usernameExists(username);
        verify(userDAO, never()).insert(any(User.class));
    }

    // ==================== Test 2.7: Create User - Weak Password ====================
    @Test
    @DisplayName("Test 2.7: Creating user with weak password should throw WeakPasswordException")
    void testCreateUser_WeakPassword_ThrowsException() {
        // Arrange
        String weakPassword = "123";
        Integer createdBy = 1;
        
        User adminUser = new User("admin1", "hash", "Admin", UserRole.ADMIN);
        adminUser.setUserId(1);
        
        when(userDAO.findById(createdBy)).thenReturn(adminUser);
        
        // Act & Assert
        WeakPasswordException exception = assertThrows(
            WeakPasswordException.class,
            () -> userService.createUser("newuser", weakPassword, "Test User", UserRole.ASSISTANT, createdBy)
        );
        
        assertTrue(exception.getMessage().contains("at least 8 characters"));
        verify(userDAO, times(1)).findById(createdBy);
        verify(userDAO, never()).usernameExists(anyString());
        verify(userDAO, never()).insert(any(User.class));
    }

    // ==================== Test 2.8: Change Password - Success ====================
    @Test
    @DisplayName("Test 2.8: Change password with correct old password should succeed")
    void testChangePassword_Success() {
        // Arrange
        Integer userId = 1;
        String oldPassword = "OldPass123!";
        String newPassword = "NewPass456!";
        String oldHash = PasswordHashProvider.hashPassword(oldPassword);
        
        User user = new User("admin1", oldHash, "Admin", UserRole.ADMIN);
        user.setUserId(userId);
        
        when(userDAO.findById(userId)).thenReturn(user);
        when(userDAO.update(any(User.class))).thenReturn(true);
        
        // Act
        boolean result = userService.changePassword(userId, oldPassword, newPassword);
        
        // Assert
        assertTrue(result);
        verify(userDAO, times(1)).findById(userId);
        verify(userDAO, times(1)).update(any(User.class));
    }

    // ==================== Test 2.9: Change Password - Wrong Old Password ====================
    @Test
    @DisplayName("Test 2.9: Change password with wrong old password should throw InvalidCredentialsException")
    void testChangePassword_WrongOldPassword_ThrowsException() {
        // Arrange
        Integer userId = 1;
        String correctOldPassword = "OldPass123!";
        String wrongOldPassword = "WrongPass123!";
        String newPassword = "NewPass456!";
        String oldHash = PasswordHashProvider.hashPassword(correctOldPassword);
        
        User user = new User("admin1", oldHash, "Admin", UserRole.ADMIN);
        user.setUserId(userId);
        
        when(userDAO.findById(userId)).thenReturn(user);
        
        // Act & Assert
        InvalidCredentialsException exception = assertThrows(
            InvalidCredentialsException.class,
            () -> userService.changePassword(userId, wrongOldPassword, newPassword)
        );
        
        assertEquals("Current password is incorrect", exception.getMessage());
        verify(userDAO, times(1)).findById(userId);
        verify(userDAO, never()).update(any(User.class));
    }

    // ==================== Test 2.10: Is Admin - User is Admin ====================
    @Test
    @DisplayName("Test 2.10: isAdmin should return true for admin user")
    void testIsAdmin_UserIsAdmin_ReturnsTrue() {
        // Arrange
        Integer userId = 1;
        User adminUser = new User("admin1", "hash", "Admin", UserRole.ADMIN);
        adminUser.setUserId(userId);
        
        when(userDAO.findById(userId)).thenReturn(adminUser);
        
        // Act
        boolean result = userService.isAdmin(userId);
        
        // Assert
        assertTrue(result);
        verify(userDAO, times(1)).findById(userId);
    }

    // ==================== Test 2.11: Is Admin - User is Assistant ====================
    @Test
    @DisplayName("Test 2.11: isAdmin should return false for assistant user")
    void testIsAdmin_UserIsAssistant_ReturnsFalse() {
        // Arrange
        Integer userId = 5;
        User assistantUser = new User("assistant1", "hash", "Assistant", UserRole.ASSISTANT);
        assistantUser.setUserId(userId);
        
        when(userDAO.findById(userId)).thenReturn(assistantUser);
        
        // Act
        boolean result = userService.isAdmin(userId);
        
        // Assert
        assertFalse(result);
        verify(userDAO, times(1)).findById(userId);
    }

    // ==================== Additional Edge Case Tests ====================
    
    @Test
    @DisplayName("Login with inactive user should throw InvalidCredentialsException")
    void testLogin_InactiveUser_ThrowsException() {
        // Arrange
        String username = "admin1";
        String plainPassword = "Password123!";
        String hashedPassword = PasswordHashProvider.hashPassword(plainPassword);
        
        User mockUser = new User(username, hashedPassword, "Admin User", UserRole.ADMIN);
        mockUser.setUserId(1);
        mockUser.setActive(false);
        
        when(userDAO.findByUsername(username)).thenReturn(mockUser);
        
        // Act & Assert
        assertThrows(InvalidCredentialsException.class, 
            () -> userService.login(username, plainPassword)
        );
    }

    @Test
    @DisplayName("Change password for non-existent user should throw UserNotFoundException")
    void testChangePassword_UserNotFound_ThrowsException() {
        // Arrange
        Integer userId = 999;
        when(userDAO.findById(userId)).thenReturn(null);
        
        // Act & Assert
        assertThrows(UserNotFoundException.class,
            () -> userService.changePassword(userId, "OldPass123!", "NewPass456!")
        );
    }

    @Test
    @DisplayName("Change password with weak new password should throw WeakPasswordException")
    void testChangePassword_WeakNewPassword_ThrowsException() {
        // Arrange
        Integer userId = 1;
        String oldPassword = "OldPass123!";
        String weakNewPassword = "weak";
        String oldHash = PasswordHashProvider.hashPassword(oldPassword);
        
        User user = new User("admin1", oldHash, "Admin", UserRole.ADMIN);
        user.setUserId(userId);
        
        when(userDAO.findById(userId)).thenReturn(user);
        
        // Act & Assert
        assertThrows(WeakPasswordException.class,
            () -> userService.changePassword(userId, oldPassword, weakNewPassword)
        );
    }
}