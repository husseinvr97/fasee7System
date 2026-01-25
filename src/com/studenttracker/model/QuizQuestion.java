package com.studenttracker.model;


public class QuizQuestion {
    private Integer questionId;
    private Integer quizId;
    private int questionNumber;
    private QuestionType questionType;
    private LessonTopic.TopicCategory category;
    private Double points;
    private String modelAnswer;       // For MCQ: "A", "B", "C", "D"; NULL for Essay
    
    public enum QuestionType {
        MCQ, ESSAY
    }
    
    // Constructors
    public QuizQuestion() {}
    
    public QuizQuestion(Integer quizId, int questionNumber, QuestionType questionType, 
                       LessonTopic.TopicCategory category, Double points, String modelAnswer) {
        this.quizId = quizId;
        this.questionNumber = questionNumber;
        this.questionType = questionType;
        this.category = category;
        this.points = points;
        this.modelAnswer = modelAnswer;
    }
    
    // Getters and Setters
    public Integer getQuestionId() { return questionId; }
    public void setQuestionId(Integer questionId) { this.questionId = questionId; }
    
    public Integer getQuizId() { return quizId; }
    public void setQuizId(Integer quizId) { this.quizId = quizId; }
    
    public int getQuestionNumber() { return questionNumber; }
    public void setQuestionNumber(int questionNumber) { this.questionNumber = questionNumber; }
    
    public QuestionType getQuestionType() { return questionType; }
    public void setQuestionType(QuestionType questionType) { this.questionType = questionType; }
    
    public LessonTopic.TopicCategory getCategory() { return category; }
    public void setCategory(LessonTopic.TopicCategory category) { this.category = category; }
    
    public Double getPoints() { return points; }
    public void setPoints(Double points) { this.points = points; }
    
    public String getModelAnswer() { return modelAnswer; }
    public void setModelAnswer(String modelAnswer) { this.modelAnswer = modelAnswer; }
    
    // Helper methods
    public boolean isMCQ() {
        return questionType == QuestionType.MCQ;
    }
    
    public boolean isEssay() {
        return questionType == QuestionType.ESSAY;
    }
    
    @Override
    public String toString() {
        return "Q" + questionNumber + " (" + questionType + " - " + category.getArabicName() + 
               ", " + points + " pts)";
    }
}