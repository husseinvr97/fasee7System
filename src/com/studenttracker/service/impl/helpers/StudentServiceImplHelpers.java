package com.studenttracker.service.impl.helpers;

public class StudentServiceImplHelpers
{
    private StudentServiceImplHelpers() {}

    /**
     * Gets the number of name parts.
     * 
     * @param fullName The full name
     * @return Number of name parts
     */
    public static int getNamePartCount(String fullName) {
        if (fullName == null || fullName.trim().isEmpty()) {
            return 0;
        }
        return fullName.trim().split("\\s+").length;
    }

    /**
     * Normalizes full name by trimming and reducing multiple spaces to single space.
     * 
     * @param fullName The full name to normalize
     * @return Normalized full name
     */
    public static String normalizeFullName(String fullName) {
        if (fullName == null) {
            return null;
        }
        return fullName.trim().replaceAll("\\s+", " ");
    }

}
