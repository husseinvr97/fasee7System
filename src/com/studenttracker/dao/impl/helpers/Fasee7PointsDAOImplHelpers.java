package com.studenttracker.dao.impl.helpers;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import com.studenttracker.model.Fasee7Points;
import com.studenttracker.util.ResultSetExtractor;

public class Fasee7PointsDAOImplHelpers {
    
    private Fasee7PointsDAOImplHelpers() {}

    public static Map<String, Function<Object, Object>> getTransformers() {
        Map<String, Function<Object, Object>> transformers = new HashMap<>();
        
        transformers.put("quiz_points", s -> {
            try {
                if (s == null) return BigDecimal.ZERO;
                if (s instanceof BigDecimal) return s;
                if (s instanceof Number) return BigDecimal.valueOf(((Number) s).doubleValue());
                return new BigDecimal(s.toString());
            } catch (Exception e) {
                return BigDecimal.ZERO;
            }
        });
        
        transformers.put("total_points", s -> {
            try {
                if (s == null) return BigDecimal.ZERO;
                if (s instanceof BigDecimal) return s;
                if (s instanceof Number) return BigDecimal.valueOf(((Number) s).doubleValue());
                return new BigDecimal(s.toString());
            } catch (Exception e) {
                return BigDecimal.ZERO;
            }
        });
        
        transformers.put("last_updated", s -> {
            try {
                return s != null ? LocalDateTime.parse((String) s) : null;
            } catch (Exception e) {
                return null;
            }
        });
        
        return transformers;
    }

    public static Fasee7Points extractPointsFromResultSet(
            ResultSet rs, 
            Map<String, Function<Object, Object>> transformers) {
        if (transformers == null) {
            try {
                return ResultSetExtractor.extractWithTransformers(rs, Fasee7Points.class, null);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        
        try {
            return ResultSetExtractor.extractWithTransformers(rs, Fasee7Points.class, transformers);
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        return null;
    }
}