package com.bitlord.authservice.service;

import com.bitlord.authservice.model.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;


/**
 * Service for handling JWT token generation and validation.
 */
@Service
public class JwtService {

    // Secret key string loaded from application.properties/yml — used to sign and verify JWT tokens
    @Value("${app.jwt.secret}")
    private String secretKey;

    // Access token expiry duration in seconds — loaded from application.properties/yml
    @Value("${app.jwt.access-token-expiry}")
    private long jwtExpiration; // in seconds

    /**
     * Extracts the email from the token.
     */
    public String extractUsername(String token) {
        // The subject field in the JWT holds the user's email
        return extractClaim(token, Claims::getSubject);
    }

    // Generic method to extract any single claim from the token using a claims resolver function
    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    /**
     * Generates an access token for the given user, adding custom claims like role and userId.
     */
    public String generateToken(User user) {
        // Build extra claims map to embed role and userId inside the token payload
        Map<String, Object> extraClaims = new HashMap<>();
        extraClaims.put("role", user.getRole().name());
        extraClaims.put("userId", user.getId());
        // Convert expiry from seconds to milliseconds before passing to buildToken
        return buildToken(extraClaims, user.getEmail(), jwtExpiration * 1000);
    }

    // Builds and signs the JWT token with claims, subject, issued time, expiry, and the signing key
    private String buildToken(
            Map<String, Object> extraClaims,
            String subject,
            long expiration
    ) {
        return Jwts.builder()
                .claims(extraClaims)                                          // Set custom claims (role, userId)
                .subject(subject)                                             // Set subject as the user's email
                .issuedAt(new Date(System.currentTimeMillis()))               // Token creation timestamp
                .expiration(new Date(System.currentTimeMillis() + expiration))// Token expiry timestamp
                .signWith(getSignInKey())                                      // Sign with HMAC secret key
                .compact();                                                   // Serialize to a compact JWT string
    }

    /**
     * Checks if the token is valid for the user and not expired.
     */
    public boolean isTokenValid(String token, User user) {
        final String username = extractUsername(token);
        // Token is valid only if the email matches the user and the token is not expired
        return (username.equals(user.getEmail())) && !isTokenExpired(token);
    }

    // Returns true if the token's expiration date is before the current time
    private boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    // Extracts the expiration date claim from the token
    private Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    // Parses and returns all claims from the token after verifying the signature
    private Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSignInKey()) // Use the secret key to verify the token signature
                .build()
                .parseSignedClaims(token)  // Parse and validate the signed JWT
                .getPayload();             // Return only the claims payload
    }

    // Decodes the Base64 secret key string and builds an HMAC-SHA signing key from it
    private SecretKey getSignInKey() {
        byte[] keyBytes = Decoders.BASE64.decode(secretKey);
        return Keys.hmacShaKeyFor(keyBytes);
    }
}
