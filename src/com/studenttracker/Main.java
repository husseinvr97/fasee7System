package com.studenttracker;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

// Import service classes
import com.studenttracker.util.ServiceLocator;
import com.studenttracker.service.impl.UserServiceImpl;
import com.studenttracker.dao.impl.UserDAOImpl;
// TODO: Import other services as needed

public class Main extends Application {

    private static final ServiceLocator serviceLocator = ServiceLocator.getInstance();
    @Override
    public void start(Stage primaryStage) throws Exception {
        // Initialize services BEFORE loading any FXML
        initializeServices();
        
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/studenttracker/view/fxml/auth/Login.fxml"));
        Parent root = loader.load();
        
        Scene scene = new Scene(root);
        
        primaryStage.setTitle("Fasee7 System");
        primaryStage.setScene(scene);
        primaryStage.setMinWidth(800);
        primaryStage.setMinHeight(600);
        primaryStage.show();
    }

    /**
     * Initialize all services and register them with ServiceLocator.
     */
    private void initializeServices() {
        // TODO: Initialize DAOs
        UserDAOImpl userDAO = new UserDAOImpl();
        
        // TODO: Initialize Services
        UserServiceImpl userService = new UserServiceImpl(userDAO);
        
        // Register services with ServiceLocator
        serviceLocator.setUserService(userService);
        
        // TODO: Initialize and register other services as needed:
        // ServiceLocator.setStudentService(studentService);
        // ServiceLocator.setLessonService(lessonService);
        // etc.
    }

    public static void main(String[] args) {
        launch(args);
    }
}