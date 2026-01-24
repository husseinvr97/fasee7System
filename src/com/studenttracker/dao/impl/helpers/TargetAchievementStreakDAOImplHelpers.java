package com.studenttracker.dao.impl.helpers;

import java.sql.ResultSet;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import com.studenttracker.model.TargetAchievementStreak;
import com.studenttracker.util.ResultSetExtractor;

public class TargetAchievementStreakDAOImplHelpers {
    
    private TargetAchievementStreakDAOImplHelpers() {}

    public static Map<String, Function<Object, Object>> getTransformers() {
        Map<String, Function<Object, Object>> transformers = new HashMap<>();
        
        transformers.put("last_achievement_at", s -> {
            try {
                return s != null ? LocalDateTime.parse((String) s) : null;
            } catch (Exception e) {
                return null;
            }
        });
        
        return transformers;
    }

    public static TargetAchievementStreak extractStreakFromResultSet(
            ResultSet rs, 
            Map<String, Function<Object, Object>> transformers) {
        if (transformers == null) {
            try {
                return ResultSetExtractor.extractWithTransformers(rs, TargetAchievementStreak.class, null);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        
        try {
            return ResultSetExtractor.extractWithTransformers(rs, TargetAchievementStreak.class, transformers);
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        return null;
    }
}