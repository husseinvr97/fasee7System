package com.studenttracker.model;

public class LessonTopic {
    private Integer topicId;
    private Integer lessonId;
    private TopicCategory category;
    private String specificTopic;     // Free text
    
    public enum TopicCategory {
        NAHW("نحو"),
        ADAB("أدب"),
        QISSA("قصة"),
        TABEER("تعبير"),
        NUSUS("نصوص"),
        QIRAA("قراءة");
        
        private final String arabicName;
        
        TopicCategory(String arabicName) {
            this.arabicName = arabicName;
        }
        
        public String getArabicName() {
            return arabicName;
        }
    }
    
    // Constructors
    public LessonTopic() {}
    
    public LessonTopic(Integer lessonId, TopicCategory category, String specificTopic) {
        this.lessonId = lessonId;
        this.category = category;
        this.specificTopic = specificTopic;
    }
    
    // Getters and Setters
    public Integer getTopicId() { return topicId; }
    public void setTopicId(Integer topicId) { this.topicId = topicId; }
    
    public Integer getLessonId() { return lessonId; }
    public void setLessonId(Integer lessonId) { this.lessonId = lessonId; }
    
    public TopicCategory getCategory() { return category; }
    public void setCategory(TopicCategory category) { this.category = category; }
    
    public String getSpecificTopic() { return specificTopic; }
    public void setSpecificTopic(String specificTopic) { this.specificTopic = specificTopic; }
    
    @Override
    public String toString() {
        return category.getArabicName() + ": " + specificTopic;
    }
}