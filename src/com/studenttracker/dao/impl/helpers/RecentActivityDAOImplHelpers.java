package com.studenttracker.dao.impl.helpers;

import com.studenttracker.model.RecentActivity;
import com.studenttracker.util.ResultSetExtractor;

import java.sql.ResultSet;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

/**
 * Helper class for RecentActivityDAOImpl.
 * Provides transformers for ResultSetExtractor if we want to use it instead of manual mapping.
 * 
 * <p><b>Purpose:</b> Centralize field transformations for RecentActivity entity.</p>
 * 
 * <p><b>Why Optional?</b> The DAO currently uses manual mapping in mapResultSetToActivity().
 * This helper is provided as an alternative approach using ResultSetExtractor.</p>
 * 
 * <p><b>Usage Example - In DAO:</b></p>
 * <pre>
 * // Instead of manual mapping:
 * private RecentActivity mapResultSetToActivity(ResultSet rs) throws SQLException {
 *     // ... manual field setting ...
 * }
 * 
 * // Use ResultSetExtractor:
 * private RecentActivity mapResultSetToActivity(ResultSet rs) {
 *     return RecentActivityDAOImplHelpers.extractFromResultSet(rs);
 * }
 * </pre>
 * 
 * @author fasee7System
 * @version 1.0.0
 * @since 2026-01-28
 */
public class RecentActivityDAOImplHelpers {
    
    /**
     * Private constructor to prevent instantiation.
     * This is a utility class with only static methods.
     */
    private RecentActivityDAOImplHelpers() {
        throw new AssertionError("Cannot instantiate utility class");
    }
    
    /**
     * Gets field transformers for RecentActivity entity.
     * Handles conversion of database types to Java types.
     * 
     * <p><b>Transformers:</b></p>
     * <ul>
     *   <li>created_at: String → LocalDateTime</li>
     * </ul>
     * 
     * <p><b>Note:</b> Other fields (Integer, String) don't need transformers
     * as ResultSetExtractor handles them automatically.</p>
     * 
     * @return Map of field name to transformer function
     */
    public static Map<String, Function<Object, Object>> getTransformers() {
        Map<String, Function<Object, Object>> transformers = new HashMap<>();
        
        // Transform created_at from String (SQLite format) to LocalDateTime
        transformers.put("created_at", value -> {
            if (value == null) {
                return null;
            }
            
            try {
                String timestamp = (String) value;
                // SQLite stores as "YYYY-MM-DD HH:MM:SS" or "YYYY-MM-DDTHH:MM:SS"
                // Convert to ISO format if needed (replace space with 'T')
                timestamp = timestamp.replace(" ", "T");
                return LocalDateTime.parse(timestamp);
            } catch (Exception e) {
                System.err.println("Failed to parse created_at: " + value);
                e.printStackTrace();
                return null;
            }
        });
        
        // Note: We don't need transformers for:
        // - activity_id (Integer) - handled by ResultSetExtractor
        // - activity_type (String) - handled by ResultSetExtractor
        // - activity_description (String) - handled by ResultSetExtractor
        // - entity_type (String) - handled by ResultSetExtractor
        // - entity_id (Integer) - handled by ResultSetExtractor
        // - performed_by (Integer) - handled by ResultSetExtractor
        
        return transformers;
    }
    
    /**
     * Extracts RecentActivity object from ResultSet using ResultSetExtractor.
     * This is an alternative to manual mapping.
     * 
     * <p><b>Advantages:</b></p>
     * <ul>
     *   <li>Less boilerplate code</li>
     *   <li>Automatic snake_case to camelCase conversion</li>
     *   <li>Automatic type conversion (Long → Integer, etc.)</li>
     * </ul>
     * 
     * <p><b>Usage in DAO:</b></p>
     * <pre>
     * private RecentActivity mapResultSetToActivity(ResultSet rs) {
     *     return RecentActivityDAOImplHelpers.extractFromResultSet(rs);
     * }
     * </pre>
     * 
     * @param rs ResultSet positioned at a valid row
     * @return RecentActivity object extracted from current ResultSet row
     */
    public static RecentActivity extractFromResultSet(ResultSet rs) {
        Map<String, Function<Object, Object>> transformers = getTransformers();
        
        try {
            return ResultSetExtractor.extractWithTransformers(
                rs, 
                RecentActivity.class, 
                transformers
            );
        } catch (Exception e) {
            System.err.println("Failed to extract RecentActivity from ResultSet");
            e.printStackTrace();
            return null;
        }
    }
    
    /**
     * Extracts RecentActivity without transformers (for testing).
     * Uses default ResultSetExtractor behavior.
     * 
     * <p><b>Note:</b> This won't handle created_at properly - use extractFromResultSet() instead.</p>
     * 
     * @param rs ResultSet positioned at a valid row
     * @return RecentActivity object (created_at may be null)
     */
    public static RecentActivity extractWithoutTransformers(ResultSet rs) {
        try {
            return ResultSetExtractor.extractObjectFromResultSet(rs, RecentActivity.class);
        } catch (Exception e) {
            System.err.println("Failed to extract RecentActivity from ResultSet");
            e.printStackTrace();
            return null;
        }
    }
}