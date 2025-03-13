package com.eazybytes.security.jwt;

import com.eazybytes.security.services.UserDetailsServiceImpl;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

// This class is used to intercept and validate JSON Web Tokens (JWTs) in incoming HTTP requests.
// extends OncePerRequestFilter, meaning it runs once per HTTP request to check if a JWT token is present and valid.
// It retrieves the JWT from the request header using jwtUtils.getJwtFromHeader(request)
// Validate the JWT (jwtUtils.validateJwtToken(jwt))
// Extract User Details if valid (roles, permission) ->(userDetailsService.loadUserByUsername(username))
// Creates an UsernamePasswordAuthenticationToken with the user's roles and permissions, allowing Spring
// Security to recognize the user as authenticated.  (SecurityContextHolder.getContext().setAuthentication(authentication))
// If authentication is successful, the request proceeds; otherwise, unauthorized requests will be blocked.((filterChain.doFilter(request, response))


/* Why OncePerRequestFilter
1. Avoids Multiple Executions
If the request goes through multiple filters (e.g., CORS filters, logging filters),
OncePerRequestFilter ensures that authentication is checked only once in the request lifecycle.
2. Ensures JWT Validation Happens Early
The filter runs before the request reaches secured endpoints, allowing only authenticated users to proceed.
3 .Integrates with Spring Security
It works seamlessly within Spring Security, allowing authentication to be set in the SecurityContextHolder.

* */
@Component
public class AuthTokenFilter extends OncePerRequestFilter {
    @Autowired
    private JwtUtils jwtUtils;

    @Autowired
    private UserDetailsServiceImpl userDetailsService;

    private static final Logger logger = LoggerFactory.getLogger(AuthTokenFilter.class);

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {
        logger.debug("AuthTokenFilter called for URI: {}", request.getRequestURI());
        try {
            String jwt = parseJwt(request);
            if (jwt != null && jwtUtils.validateJwtToken(jwt)) {
                String username = jwtUtils.getUserNameFromJwtToken(jwt);

                UserDetails userDetails = userDetailsService.loadUserByUsername(username);

                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(userDetails,
                                null,
                                userDetails.getAuthorities());
                logger.debug("Roles from JWT: {}", userDetails.getAuthorities());

                authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                SecurityContextHolder.getContext().setAuthentication(authentication);
            }
        } catch (Exception e) {
            logger.error("Cannot set user authentication: {}", e);
        }

        filterChain.doFilter(request, response);
    }

    // parseJwt method is responsible for extracting the JWT token from the incoming HTTP request.
    // Extract JWT from Request Header

    private String parseJwt(HttpServletRequest request) {
        String jwt = jwtUtils.getJwtFromHeader(request);
        logger.debug("AuthTokenFilter.java: {}", jwt);
        return jwt;
    }
}
