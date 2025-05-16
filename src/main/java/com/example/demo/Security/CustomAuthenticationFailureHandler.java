package com.example.demo.Security;

import org.springframework.security.authentication.*;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.stereotype.Component;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

@Component
public class CustomAuthenticationFailureHandler implements AuthenticationFailureHandler {

    @Override
    public void onAuthenticationFailure(HttpServletRequest request, 
                                        HttpServletResponse response,
                                        AuthenticationException exception) 
                                        throws IOException {
        String redirectUrl = "/login?error=true";
    
        if (exception instanceof BadCredentialsException) {
            redirectUrl = "/login?bad=true";
        } else if (exception instanceof DisabledException) {
            redirectUrl = "/login?disabled=true";
        } else if (exception instanceof AccountExpiredException) {
            redirectUrl = "/login?expired=true";
        } else if (exception instanceof LockedException) {
            redirectUrl = "/login?locked=true";
        } else if (exception instanceof CredentialsExpiredException) {
            redirectUrl = "/login?credentialsExpired=true";
        } else if (exception instanceof org.springframework.security.core.userdetails.UsernameNotFoundException) {
            redirectUrl = "/login?notfound=true";
        }
    
        response.sendRedirect(redirectUrl);
    }
}