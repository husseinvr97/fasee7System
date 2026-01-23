package com.studenttracker.dao.impl.helpers;

import com.studenttracker.model.Warning;
import com.studenttracker.model.Warning.WarningType;
import com.studenttracker.util.ResultSetExtractor;

import java.sql.ResultSet;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

public class WarningDAOImplHelpers {
    
    private WarningDAOImplHelpers() {}
    
    /**
     * Get field transformers for Warning entity.
     * Handles conversion of database types to Java types.
     */
    public static Map<String, Function<Object, Object>> getTransformers() {
        Map<String, Function<Object, Object>> transformers = new HashMap<>();
        
        // Transform warning_type from String to WarningType enum
        transformers.put("warning_type", s -> {
            try {
                return WarningType.valueOf((String) s);
            } catch (Exception e) {
                return null;
            }
        });
        
        // Transform created_at from String to LocalDateTime
        transformers.put("created_at", s -> {
            try {
                return LocalDateTime.parse((String) s);
            } catch (Exception e) {
                return null;
            }
        });
        
        // Transform resolved_at from String to LocalDateTime
        transformers.put("resolved_at", s -> {
            try {
                return s != null ? LocalDateTime.parse((String) s) : null;
            } catch (Exception e) {
                return null;
            }
        });
        
        return transformers;
    }
    
    /**
     * Extract Warning object from ResultSet using transformers.
     * @param rs ResultSet positioned at a valid row
     * @param transformers Map of field transformers
     * @return Warning object extracted from current ResultSet row
     */
    public static Warning extractWarningFromResultSet(ResultSet rs, Map<String, Function<Object, Object>> transformers) {
        if (transformers == null) {
            try {
                return ResultSetExtractor.extractWithTransformers(rs, Warning.class, null);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        
        try {
            return ResultSetExtractor.extractWithTransformers(rs, Warning.class, transformers);
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        return null;
    }
}