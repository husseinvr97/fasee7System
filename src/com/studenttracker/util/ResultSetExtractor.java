package com.studenttracker.util;

import java.lang.reflect.Field;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.util.Map;
import java.util.function.Function;

public class ResultSetExtractor 
{
    private ResultSetExtractor() {}

    public static <T> T extractObjectFromResultSet(ResultSet rs, Class<T> clazz) throws Exception {
        T instance = clazz.getDeclaredConstructor().newInstance();
        ResultSetMetaData metaData = rs.getMetaData();
        int columnCount = metaData.getColumnCount();

        for (int i = 1; i <= columnCount; i++) {
            String columnName = metaData.getColumnLabel(i);
            Object value = rs.getObject(i); // Fetch by index is faster

            if (value != null) {
                // Transform snake_case to camelCase for the setter search
                String fieldName = toCamelCase(columnName);
                try {
                    // Find field to determine the correct type for the setter
                    Field field = clazz.getDeclaredField(fieldName);
                    field.setAccessible(true);
                
                    // Set value directly to field (bypasses setter naming issues)
                    field.set(instance, value);
                } catch (NoSuchFieldException e) {
                    // Log or ignore columns that don't exist in the Java object
                }
            }
        }
        return instance;
    }

    public static <T> T extractWithTransformers(
    ResultSet rs, 
    Class<T> clazz, 
    Map<String, Function<Object, Object>>transformers
) throws Exception {
    
    T instance = clazz.getDeclaredConstructor().newInstance();
    ResultSetMetaData metaData = rs.getMetaData();
    int columnCount = metaData.getColumnCount();

    for (int i = 1; i <= columnCount; i++) {
        String colName = metaData.getColumnLabel(i);
        Object value = rs.getObject(i);

        if (value != null) {
            // 1. Apply user-defined transformation if exists
            if (transformers != null && transformers.containsKey(colName)) {
                value = transformers.get(colName).apply(value);
            }

            // 2. Intelligent Mapping
            smartSet(instance, colName, value);
        }
    }
    return instance;
}

private static void smartSet(Object instance, String colName, Object value) {
    try {
        // Normalize column name (user_id -> userId)
        String fieldName = toCamelCase(colName);
        Field field = instance.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        
        // 3. Automatic Type Narrowing/Conversion
        // If DB gives Long but Field is Integer, this prevents an IllegalArgumentException
        Object convertedValue = convertToFieldType(value, field.getType());
        
        field.set(instance, convertedValue);
    } catch (NoSuchFieldException e) {
        // Skip columns that don't exist in the class
    } catch (Exception e) {
        e.printStackTrace();
    }
}

    private static String toCamelCase(String s) {
        // Basic logic to turn USER_NAME or user_name into userName
        StringBuilder sb = new StringBuilder();
        boolean nextUpper = false;
        for (char c : s.toLowerCase().toCharArray()) {
            if (c == '_') { nextUpper = true; }
            else {
                if (nextUpper) { sb.append(Character.toUpperCase(c)); nextUpper = false; }
                else { sb.append(c); }
            }
        }
        return sb.toString();
    }

    private static Object convertToFieldType(Object value, Class<?> targetType) {
    if (value == null) return null;

    // If the types already match, just return it
    if (targetType.isAssignableFrom(value.getClass())) {
        return value;
    }

    // Handle common SQL -> Java conversions
    if (targetType == Integer.class || targetType == int.class) {
        if (value instanceof Number) return ((Number) value).intValue();
    }
    
    if (targetType == Long.class || targetType == long.class) {
        if (value instanceof Number) return ((Number) value).longValue();
    }

    if (targetType == Double.class || targetType == double.class) {
        if (value instanceof Number) return ((Number) value).doubleValue();
    }

    // Handle Date conversions (The biggest pain point in JDBC)
    if (targetType == java.time.LocalDate.class && value instanceof java.sql.Date) {
        return ((java.sql.Date) value).toLocalDate();
    }
    
    if (targetType == java.time.LocalDateTime.class && value instanceof java.sql.Timestamp) {
        return ((java.sql.Timestamp) value).toLocalDateTime();
    }

    // Add more conversions as you discover them
    return value; 
}
}
