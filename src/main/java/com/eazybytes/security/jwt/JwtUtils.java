package com.eazybytes.security.jwt;

import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.security.Key;
import java.util.Date;

@Component // spring managed bean
public class JwtUtils {
    // A logger is created for this class to facilitate logging messages
    // (debugging, error reporting, etc.) related to JWT processing.
    private static final Logger logger = LoggerFactory.getLogger(JwtUtils.class);

    // two variables jwtSecret and jwtExpirationMS are initialized from application.properties
    // jwtSecret is a  random secret key used to sign and verify JWTs.
    // The jwtSecret should be kept private and not exposed publicly.
    // When a user sends a JWT in a request, The jwtSecret is used to verify that the
    // token was issued by your server and hasn't been altered.
    // They are typically generated using cryptographic libraries (OpenSSL, Java, Python, or Linux ) and stored in secure locations like
    // environment variables, key vaults, or configuration management systems.


    // @Value injects values from application properties to these fields
    @Value("${spring.app.jwtSecret}")
    private String jwtSecret;

    @Value("${spring.app.jwtExpirationMs}")
    private int jwtExpirationMs;

    // Key is an interface has three things in common  - algorithm, encoded and format
    // This method is responsible for creating a cryptographic key used to sign and verify JWT tokens.
    // jwtSecret is stored as a BASE64 string.
    // Decoders.BASE64.decode(jwtSecret) converts it back into its original binary form,
    // so it can be used as a cryptographic key.
    // Keys.hmacShaKeyFor()  method decodes the BASE64-encoded secret key.
    // It converts the decoded binary data into a valid HMAC key.
    // The key is used to sign JWTs using the HMAC SHA algorithm.
    private Key key() {
        return Keys.hmacShaKeyFor(Decoders.BASE64.decode(jwtSecret));
    }

    //  Generates a JWT for a given user based on their username.
    //Retrieves the username from the UserDetails object.
    //Uses the Jwts.builder() method to create a JWT: Jwts is utility class from JJWT (Java Jwt) library.
    //Sets the subject (the username).
    //Sets the issued date (current date/time).
    //Sets the expiration date based on jwtExpirationMs.
    //Signs the token using the secret key obtained from the key() method.
    //Building and returning the JWT as a compact string url-safe string
    // compact()->Converts the JWT into a single string that contains the header, payload, and signature,
    // all encoded and separated by periods (.).
    public String generateTokenFromUsername(UserDetails userDetails) {
        String username = userDetails.getUsername();
        return Jwts.builder()
                .subject(username)
                .issuedAt(new Date())
                .expiration(new Date((new Date()).getTime() + jwtExpirationMs))
                .signWith(key())
                .compact();
    }

    //  Extracts the JWT from the Authorization header of the incoming HTTP request.
    // give me the header with the name "Authorization".
    // Logs the header for debugging purposes.
    // Checks if the header starts with Bearer (the standard prefix for JWT).
    // If valid, it returns the token by removing the Bearer prefix; otherwise, it returns null.

    public String getJwtFromHeader(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        logger.debug("Authorization Header: {}", bearerToken);
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7); // Remove Bearer prefix
        }
        return null;
    }



    // parser()-> open the token, process for decoding and verifying JWT
    // The SecretKey is the key used to sign the JWT when it was created,
    // Uses the JWT parser to verify the token with the secret key.
    // build() method finalizes the configuration to open token and read its information.
    // extract the claims.
    // Claims are pieces of information inside the JWT, like the username, roles, expiration time, etc.
    // The payload is the actual content inside the envelope. Username (subject), roles, expiration, etc.
    public String getUserNameFromJwtToken(String token) {
        return Jwts.parser()
                .verifyWith((SecretKey) key())
                .build().parseSignedClaims(token)
                .getPayload().getSubject();
    }


    //  Validates a JWT token and checks its integrity and expiration.
    //Attempts to parse and verify the token using the secret key.
    //If successful, returns true, indicating that the token is valid.
    //If any exceptions occur (e.g., malformed, expired, unsupported, or empty token),
    //it logs an error message and returns false.
    public boolean validateJwtToken(String authToken) {
        try {
            System.out.println("Validate");
            Jwts.parser().verifyWith((SecretKey) key()).build().parseSignedClaims(authToken);
            return true;
        } catch (MalformedJwtException e) {
            logger.error("Invalid JWT token: {}", e.getMessage());
        } catch (ExpiredJwtException e) {
            logger.error("JWT token is expired: {}", e.getMessage());
        } catch (UnsupportedJwtException e) {
            logger.error("JWT token is unsupported: {}", e.getMessage());
        } catch (IllegalArgumentException e) {
            logger.error("JWT claims string is empty: {}", e.getMessage());
        }
        return false;
    }
}
