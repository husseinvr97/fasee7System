package com.studenttracker.dao.impl.helpers;

import java.sql.ResultSet;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import com.studenttracker.model.Attendance;
import com.studenttracker.util.ResultSetExtractor;

public class AttendanceDAOImplHelpers
{
    private AttendanceDAOImplHelpers() {}

    public static Map<String, Function<Object, Object>> getTransformers() {
        Map<String, Function<Object, Object>> transformers = new HashMap<>();
        
        
        transformers.put("marked_at",  s ->
            {
                try {
                        return LocalDateTime.parse((String)s);
                } catch (Exception e) {
                    return null;
                }
            }
        );
        
        return transformers;
    }

    public static Attendance extractAttendanceFromResultSet(ResultSet rs,Map<String, Function<Object, Object>> transformers)  {
        if (transformers == null) {
            try {
                return ResultSetExtractor.extractWithTransformers(rs, Attendance.class, null);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
                
        try {
            return ResultSetExtractor.extractWithTransformers(rs, Attendance.class, transformers);
        } catch (Exception e) {
            e.printStackTrace();
        }
                
        return null;
    }
}
