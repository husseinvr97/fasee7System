package com.studenttracker.dao.impl.helpers;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;


import static com.studenttracker.model.LessonTopic.TopicCategory;

public class PerformanceIndicatorDAOImplHelpers {
    
    private PerformanceIndicatorDAOImplHelpers() {}
    
    /**
     * Get transformers for ResultSet extraction.
     * Handles conversion of database types to Java types.
     * @return Map of column names to transformer functions
     */
    public static Map<String, Function<Object, Object>> getTransformers() {
        Map<String, Function<Object, Object>> transformers = new HashMap<>();
        
        // Transform calculated_at from String to LocalDateTime
        transformers.put("calculated_at", s -> {
            try {
                return LocalDateTime.parse((String) s);
            } catch (Exception e) {
                return null;
            }
        });
        
        // Transform category from String to TopicCategory enum
        transformers.put("category", s -> {
            try {
                return TopicCategory.valueOf((String) s);
            } catch (Exception e) {
                return null;
            }
        });
        
        return transformers;
    }
}