package com.studenttracker.dao.impl.helpers;

import java.sql.ResultSet;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import com.studenttracker.model.MonthlyReport;
import com.studenttracker.util.ResultSetExtractor;

public class MonthlyReportDAOImplHelpers {
    
    private MonthlyReportDAOImplHelpers() {}

    public static Map<String, Function<Object, Object>> getTransformers() {
        Map<String, Function<Object, Object>> transformers = new HashMap<>();
        
        transformers.put("generated_at", s -> {
            try {
                return LocalDateTime.parse((String) s);
            } catch (Exception e) {
                return null;
            }
        });
        
        return transformers;
    }

    public static MonthlyReport extractMonthlyReportFromResultSet(ResultSet rs, Map<String, Function<Object, Object>> transformers) {
        if (transformers == null) {
            try {
                return ResultSetExtractor.extractWithTransformers(rs, MonthlyReport.class, null);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        
        try {
            return ResultSetExtractor.extractWithTransformers(rs, MonthlyReport.class, transformers);
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        return null;
    }
}