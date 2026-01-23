package com.studenttracker.dao.impl.helpers;

import com.studenttracker.model.Mission;
import com.studenttracker.model.Mission.MissionStatus;
import com.studenttracker.model.Mission.MissionType;
import com.studenttracker.util.ResultSetExtractor;

import java.sql.ResultSet;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

public class MissionDAOImplHelpers {
    
    private MissionDAOImplHelpers() {}
    
    /**
     * Get field transformers for Mission entity.
     * Handles conversion of database types to Java types.
     */
    public static Map<String, Function<Object, Object>> getTransformers() {
        Map<String, Function<Object, Object>> transformers = new HashMap<>();
        
        // Transform mission_type from String to MissionType enum
        transformers.put("mission_type", s -> {
            try {
                return MissionType.valueOf((String) s);
            } catch (Exception e) {
                return null;
            }
        });
        
        // Transform status from String to MissionStatus enum
        transformers.put("status", s -> {
            try {
                return MissionStatus.valueOf((String) s);
            } catch (Exception e) {
                return null;
            }
        });
        
        // Transform assigned_at from String to LocalDateTime
        transformers.put("assigned_at", s -> {
            try {
                return LocalDateTime.parse((String) s);
            } catch (Exception e) {
                return null;
            }
        });
        
        // Transform completed_at from String to LocalDateTime
        transformers.put("completed_at", s -> {
            try {
                return s != null ? LocalDateTime.parse((String) s) : null;
            } catch (Exception e) {
                return null;
            }
        });
        
        return transformers;
    }
    
    /**
     * Extract Mission object from ResultSet using transformers.
     * @param rs ResultSet positioned at a valid row
     * @param transformers Map of field transformers
     * @return Mission object extracted from current ResultSet row
     */
    public static Mission extractMissionFromResultSet(ResultSet rs, Map<String, Function<Object, Object>> transformers) {
        if (transformers == null) {
            try {
                return ResultSetExtractor.extractWithTransformers(rs, Mission.class, null);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        
        try {
            return ResultSetExtractor.extractWithTransformers(rs, Mission.class, transformers);
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        return null;
    }
}