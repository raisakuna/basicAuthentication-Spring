package com.eazybytes.security.jwt;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

//  This class is used to handle unauthorized access in a Spring Security JWT authentication system.
//  It acts as an entry point that gets triggered when an unauthenticated user tries to access a secured resource.
@Component
public class AuthEntryPointJwt implements AuthenticationEntryPoint {

    private static final Logger logger = LoggerFactory.getLogger(AuthEntryPointJwt.class);

    // commence() method is responsible for handling authentication errors,
    // typically by returning an HTTP 401 Unauthorized response.
    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException authException)
            throws IOException, ServletException {
        // logs the unauthorized access attempt with the error message
        logger.error("Unauthorized error: {}", authException.getMessage());
        System.out.println(authException);

        // Ensures that the response will be returned as JSON.
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        // Sets the HTTP status to 401 Unauthorized.
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);

        // This creates a JSON response body using a HashMap:
        //"status" → 401 (Unauthorized).
        //"error" → A string indicating "Unauthorized".
        //"message" → The error message from the exception.
        //"path" → The requested endpoint that triggered the error.

        final Map<String, Object> body = new HashMap<>();
        body.put("status", HttpServletResponse.SC_UNAUTHORIZED);
        body.put("error", "Unauthorized");
        body.put("message", authException.getMessage());
        body.put("path", request.getServletPath());

        // ObjectMapper from the Jackson library to convert the body map into a JSON object.
        // Writes this JSON object to the response output stream.
        final ObjectMapper mapper = new ObjectMapper();
        mapper.writeValue(response.getOutputStream(), body);
    }

}
