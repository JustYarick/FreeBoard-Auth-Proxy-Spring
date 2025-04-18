package com.FreeBoard.auth_proxy.service;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;


public abstract class SecurityContextService {

    public static String getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && !authentication.getName().equals("anonymousUser")) {
            return authentication.getName();
        }
        return null;
    }


    private SecurityContextService() {
    }
}
