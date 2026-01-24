package com.studenttracker.dao.impl.helpers;

import java.sql.ResultSet;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import com.studenttracker.model.Fasee7Snapshot;
import com.studenttracker.util.ResultSetExtractor;

public class Fasee7SnapshotDAOImplHelpers {
    
    private Fasee7SnapshotDAOImplHelpers() {}

    public static Map<String, Function<Object, Object>> getTransformers() {
        Map<String, Function<Object, Object>> transformers = new HashMap<>();
        
        transformers.put("snapshot_date", s -> {
            try {
                return LocalDate.parse((String) s);
            } catch (Exception e) {
                return null;
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

    public static Fasee7Snapshot extractFasee7SnapshotFromResultSet(ResultSet rs, Map<String, Function<Object, Object>> transformers) {
        if (transformers == null) {
            try {
                return ResultSetExtractor.extractWithTransformers(rs, Fasee7Snapshot.class, null);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        
        try {
            return ResultSetExtractor.extractWithTransformers(rs, Fasee7Snapshot.class, transformers);
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        return null;
    }
}