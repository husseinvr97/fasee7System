package com.studenttracker.dao.impl.helpers;

import java.sql.ResultSet;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import com.studenttracker.model.Target;
import com.studenttracker.model.Target.TopicCategory;
import com.studenttracker.util.ResultSetExtractor;

public class TargetDAOImplHelpers {
    private TargetDAOImplHelpers() {}

    public static Map<String, Function<Object, Object>> getTransformers() {
        Map<String, Function<Object, Object>> transformers = new HashMap<>();
        
        transformers.put("created_at", s -> {
            try {
                return LocalDateTime.parse((String) s);
            } catch (Exception e) {
                return null;
            }
        });
        
        transformers.put("achieved_at", s -> {
            try {
                return s != null ? LocalDateTime.parse((String) s) : null;
            } catch (Exception e) {
                return null;
            }
        });
        
        transformers.put("category", s -> {
            try {
                return TopicCategory.valueOf((String) s);
            } catch (Exception e) {
                return null;
            }
        });
        
        transformers.put("is_achieved", s -> {
            try {
                return ((Number) s).intValue() == 1;
            } catch (Exception e) {
                return false;
            }
        });
        
        return transformers;
    }

    public static Target extractTargetFromResultSet(ResultSet rs, Map<String, Function<Object, Object>> transformers) {
        if (transformers == null) {
            try {
                return ResultSetExtractor.extractWithTransformers(rs, Target.class, null);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        
        try {
            return ResultSetExtractor.extractWithTransformers(rs, Target.class, transformers);
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        return null;
    }
}