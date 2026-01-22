package com.studenttracker.util;

import org.mindrot.jbcrypt.BCrypt;

public class PasswordHashProvider 
{
    private PasswordHashProvider(){}

    public static String hashPassword(String password)
    {
        return BCrypt.hashpw(password, BCrypt.gensalt());
    }
}
