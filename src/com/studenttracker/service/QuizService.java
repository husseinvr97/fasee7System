package com.studenttracker.service;

import com.studenttracker.model.Quiz;
import com.studenttracker.model.QuizCategoryTotal;
import com.studenttracker.model.QuizQuestion;
import com.studenttracker.model.QuizScore;
import com.studenttracker.model.QuizStatistics;
import com.studenttracker.model.LessonTopic.TopicCategory;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * Service interface for Quiz operations.
 * Handles business logic for quiz creation, grading, and statistics.
 */
public interface QuizService {
    
    // ========== Quiz Creation (Admin only) ==========
    
    /**
     * Creates a new quiz for a lesson.
     * 
     * @param lessonId ID of the lesson
     * @param pdfData PDF file data
     * @param questions List of quiz questions
     * @param createdBy ID of the user creating the quiz (must be Admin)
     * @return quiz ID if successful
     * @throws UnauthorizedException if createdBy is not Admin
     * @throws ValidationException if questions list is empty
     */
    Integer createQuiz(Integer lessonId, byte[] pdfData, List<QuizQuestion> questions, 
                      Integer createdBy);
    
    /**
     * Updates an existing quiz.
     * Can only update if no scores have been entered yet.
     * 
     * @param quizId ID of the quiz
     * @param pdfData New PDF file data
     * @param questions New list of questions
     * @param updatedBy ID of the user updating (must be Admin)
     * @return true if successful, false otherwise
     * @throws UnauthorizedException if updatedBy is not Admin
     * @throws ValidationException if scores already exist
     */
    boolean updateQuiz(Integer quizId, byte[] pdfData, List<QuizQuestion> questions, 
                      Integer updatedBy);
    
    /**
     * Deletes a quiz (CASCADE deletes questions and scores).
     * 
     * @param quizId ID of the quiz
     * @param deletedBy ID of the user deleting (must be Admin)
     * @return true if successful, false otherwise
     * @throws UnauthorizedException if deletedBy is not Admin
     */
    boolean deleteQuiz(Integer quizId, Integer deletedBy);
    
    
    // ========== Quiz Grading ==========
    
    /**
     * Grades a single student's quiz.
     * 
     * @param quizId ID of the quiz
     * @param studentId ID of the student
     * @param scores List of scores for each question
     * @param gradedBy ID of the user grading
     * @return true if successful, false otherwise
     * @throws ValidationException if student didn't attend or is in first lesson
     */
    boolean gradeStudent(Integer quizId, Integer studentId, 
                        List<QuizScore> scores, Integer gradedBy);
    
    /**
     * Bulk grades multiple students for a quiz.
     * 
     * @param quizId ID of the quiz
     * @param studentScores Map of studentId to their scores
     * @param gradedBy ID of the user grading
     * @return true if successful, false otherwise
     */
    boolean bulkGradeQuiz(Integer quizId, Map<Integer, List<QuizScore>> studentScores, 
                         Integer gradedBy);
    
    /**
     * Updates a specific quiz score.
     * 
     * @param scoreId ID of the score record
     * @param newPoints New points earned
     * @return true if successful, false otherwise
     * @throws ValidationException if newPoints exceeds question max points
     */
    boolean updateQuizScore(Integer scoreId, BigDecimal newPoints);
    
    
    // ========== Retrieval Operations ==========
    
    /**
     * Gets a quiz by ID.
     * 
     * @param quizId ID of the quiz
     * @return Quiz object or null if not found
     */
    Quiz getQuizById(Integer quizId);
    
    /**
     * Gets quiz for a specific lesson.
     * 
     * @param lessonId ID of the lesson
     * @return Quiz object or null if not found
     */
    Quiz getQuizByLesson(Integer lessonId);
    
    /**
     * Gets all questions for a quiz.
     * 
     * @param quizId ID of the quiz
     * @return List of questions ordered by question number
     */
    List<QuizQuestion> getQuizQuestions(Integer quizId);
    
    /**
     * Gets the quiz PDF data.
     * 
     * @param quizId ID of the quiz
     * @return PDF byte array
     */
    byte[] getQuizPdf(Integer quizId);
    
    /**
     * Gets a student's scores for a quiz.
     * 
     * @param quizId ID of the quiz
     * @param studentId ID of the student
     * @return List of quiz scores
     */
    List<QuizScore> getStudentQuizScores(Integer quizId, Integer studentId);
    
    /**
     * Gets a student's category totals for a quiz.
     * 
     * @param quizId ID of the quiz
     * @param studentId ID of the student
     * @return List of category totals
     */
    List<QuizCategoryTotal> getStudentCategoryTotals(Integer quizId, Integer studentId);
    
    
    // ========== Helper Methods ==========
    
    /**
     * Auto-grades an MCQ question.
     * 
     * @param studentAnswer Student's answer
     * @param modelAnswer Correct answer
     * @param maxPoints Maximum points for the question
     * @return Points earned (maxPoints if correct, 0 if wrong)
     */
    BigDecimal autoGradeMCQ(String studentAnswer, String modelAnswer, BigDecimal maxPoints);
    
    
    // ========== Statistics ==========
    
    /**
     * Gets total score for a student in a quiz.
     * 
     * @param quizId ID of the quiz
     * @param studentId ID of the student
     * @return Total score
     */
    BigDecimal getStudentQuizTotal(Integer quizId, Integer studentId);
    
    /**
     * Gets statistics for a quiz.
     * 
     * @param quizId ID of the quiz
     * @return Quiz statistics (average, highest, lowest, pass rate)
     */
    QuizStatistics getQuizStatistics(Integer quizId);
    
    /**
     * Gets quiz points breakdown by category.
     * 
     * @param quizId ID of the quiz
     * @return Map of category to total points
     */
    Map<TopicCategory, BigDecimal> getQuizCategoryBreakdown(Integer quizId);
    
    /**
     * Calculates total quiz points for a student across all quizzes.
     * Used for Fasee7 table calculations.
     * 
     * @param studentId ID of the student
     * @return Total quiz points
     */
    BigDecimal calculateQuizPoints(Integer studentId);
}