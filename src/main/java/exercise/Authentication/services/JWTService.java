package exercise.Authentication.services;

import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;

@Component
public class JWTService {

    // Replace this with a secure key in a real application, ideally fetched from
    // environment variables
    @Value("${jwt.secret.key}")
    private String SECRET;

    public String generateAccessToken(String username) {
        return generateToken(username, "access", 1000 * 60 * 60);
    }

    public String generateRefreshToken(String username) {
        return generateToken(username, "refresh", 1000 * 60 * 60 * 24 * 7);
    }

    // Generate token with given user name
    public String generateToken(String username, String tokenType, Integer milliseconds) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("tokenType", tokenType);
        return createToken(claims, username, milliseconds);
    }

    // Create a JWT token with specified claims and subject (user name)
    private String createToken(Map<String, Object> claims, String username, Integer milliseconds) {
        return Jwts.builder()
                .setClaims(claims)
                .setSubject(username)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + milliseconds))
                .signWith(getSignKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    // Get the signing key for JWT token
    private Key getSignKey() {
        byte[] keyBytes = Decoders.BASE64.decode(SECRET);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    // Extract the username from the token
    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    // Extract the expiration date from the token
    public Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    // Extract a claim from the token
    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    // Extract all claims from the token
    private Claims extractAllClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSignKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    // Check if the token is expired
    private Boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    // Validate the token against user details and expiration
    public Boolean validateToken(String token, UserDetails userDetails, String desiredTokenType) {
        final String username = extractUsername(token);
        String tokenType = extractClaim(token, claims -> claims.get("tokenType", String.class));
        return (username.equals(userDetails.getUsername()) && !isTokenExpired(token)
                && tokenType.equals(desiredTokenType));
    }
}
