package com.studenttracker.dao.impl.helpers;

import java.sql.ResultSet;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import com.studenttracker.model.Notification;
import com.studenttracker.util.ResultSetExtractor;

public class NotificationDAOImplHelpers {
    
    private NotificationDAOImplHelpers() {}

    public static Map<String, Function<Object, Object>> getTransformers() {
        Map<String, Function<Object, Object>> transformers = new HashMap<>();
        
        transformers.put("is_read", s -> {
            try {
                if (s instanceof Integer) {
                    return ((Integer) s) == 1;
                }
                if (s instanceof Boolean) {
                    return (Boolean) s;
                }
                return Boolean.parseBoolean(s.toString());
            } catch (Exception e) {
                return false;
            }
        });
        
        transformers.put("created_at", s -> {
            try {
                return LocalDateTime.parse((String) s);
            } catch (Exception e) {
                return null;
            }
        });
        
        return transformers;
    }

    public static Notification extractNotificationFromResultSet(ResultSet rs, Map<String, Function<Object, Object>> transformers) {
        if (transformers == null) {
            try {
                return ResultSetExtractor.extractWithTransformers(rs, Notification.class, null);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        
        try {
            return ResultSetExtractor.extractWithTransformers(rs, Notification.class, transformers);
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        return null;
    }
}