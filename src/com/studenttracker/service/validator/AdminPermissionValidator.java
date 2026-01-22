package com.studenttracker.service.validator;

import com.studenttracker.dao.UserDAO;
import com.studenttracker.exception.UnauthorizedException;
import com.studenttracker.model.User;

public class AdminPermissionValidator 
{
    private AdminPermissionValidator() {}

    public static void validateAdminPermission(Integer userId , UserDAO userDAO) {
        User user = userDAO.findById(userId);
        if (user == null || !user.isAdmin()) {
            throw new UnauthorizedException("Only administrators can perform this action");
        }
    }
}
