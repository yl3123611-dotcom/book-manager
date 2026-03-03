package com.book.manager.util;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

/**
 * Tiny utility to generate BCrypt hash for a given password.
 */
public class BcryptGen {

    public static void main(String[] args) {
        String raw = (args != null && args.length > 0) ? args[0] : "123456";
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        System.out.println(encoder.encode(raw));
    }
}
