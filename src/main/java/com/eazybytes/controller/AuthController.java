package com.eazybytes.controller;

import com.eazybytes.security.jwt.JwtUtils;
import com.eazybytes.security.request.LoginRequest;
import com.eazybytes.security.response.LoginResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

// AuthController handles user authentication, verifies credentials,
// and issues a JWT token upon successful login.
@RestController
@RequestMapping("/api/auth")
public class AuthController {
    @Autowired
    JwtUtils jwtUtils;

    @Autowired
    AuthenticationManager authenticationManager;

    // @RequestBody LoginRequest loginRequest → Takes JSON input containing the username and password.
    //We create an Authentication object to store authentication information.
    // authenticationManager.authenticate(...) is called to verify the user's credentials:
    //  Uses UsernamePasswordAuthenticationToken, which holds username and password.
    //   authenticationManager delegates the authentication process to the authentication provider (e.g., DaoAuthenticationProvider).
    //    It verifies credentials against the database or user store.
    // ✅ If authentication succeeds: ->The authentication object will contain the authenticated user details.
    // ❌ If authentication fails:->An AuthenticationException is thrown, leading to the catch block.
    @PostMapping("/public/signin")
    public ResponseEntity<?> authenticateUser(@RequestBody LoginRequest loginRequest) {
        Authentication authentication;
        try {
            authentication = authenticationManager
                    .authenticate(new UsernamePasswordAuthenticationToken(loginRequest.getUsername(), loginRequest.getPassword()));
        } catch (AuthenticationException exception) {
            Map<String, Object> map = new HashMap<>();
            map.put("message", "Bad credentials");
            map.put("status", false);
            return new ResponseEntity<Object>(map, HttpStatus.NOT_FOUND);
        }

//      set the authentication in Security Context
        // SecurityContextHolder → Stores security-related information for the current request.
        // Stores the authentication object in the Spring Security Context.
        //This allows authorization mechanisms (e.g., role-based access control) to function properly for the authenticated user.
        SecurityContextHolder.getContext().setAuthentication(authentication);

        // returns the Authenticated user object.
        //Since authentication.getPrincipal() returns the authenticated user Object, we cast it to UserDetails.
        //UserDetails contains essential user information, such as:
        //Username
        //Roles/Authorities
        //Password (but not returned in the response for security reasons)
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();

        // This calls a utility class (jwtUtils) to generate a JWT (JSON Web Token) for the authenticated user.
        // The token encapsulates the user’s identity and is used for stateless authentication.
        // The token allows the client (frontend) to authenticate future requests without re-entering credentials.
        String jwtToken = jwtUtils.generateTokenFromUsername(userDetails);

        // Extracting roles from the UserDetails
        // stream() processes each authority object.
        // map(item -> item.getAuthority()) extracts the role name (e.g., "ROLE_ADMIN", "ROLE_USER").
        // collect(Collectors.toList()) converts the stream into a List of role names.
        List<String> roles = userDetails.getAuthorities().stream()
                .map(item -> item.getAuthority())
                .collect(Collectors.toList());

        // Prepare the response body, now including the JWT token directly in the body
        LoginResponse response = new LoginResponse(userDetails.getUsername(), roles, jwtToken);

        // Return the response entity with the JWT token included in the response body
        return ResponseEntity.ok(response);
    }





}
