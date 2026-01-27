package nttdata.personal.julius.common.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.function.Function;

/**
 * Service responsible for JWT token validation and claim extraction.
 * Used by microservices that need to validate tokens but not generate them.
 */
@Service
public class JwtService {

    private static final Logger logger = LoggerFactory.getLogger(JwtService.class);

    @Value("${jwt.secret}")
    private String secretKey;

    /**
     * Extracts the username (subject) from the JWT token.
     *
     * @param token the JWT token
     * @return the username stored in the token
     */
    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    /**
     * Extracts the user ID from the JWT token.
     *
     * @param token the JWT token
     * @return the user ID stored in the token, or null if not present
     */
    public Long extractUserId(String token) {
        Object userIdObj = extractClaim(token, claims -> claims.get("userId"));
        if (userIdObj == null) {
            return null;
        }
        if (userIdObj instanceof Number) {
            return ((Number) userIdObj).longValue();
        }
        return Long.parseLong(userIdObj.toString());
    }

    /**
     * Extracts a specific claim from the JWT token.
     *
     * @param token the JWT token
     * @param claimsResolver function to extract the desired claim
     * @return the extracted claim value
     */
    public <T> T extractClaim(String token, java.util.function.Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    /**
     * Validates if the token is valid (not expired).
     *
     * @param token the JWT token
     * @return true if the token is valid, false otherwise
     */
    public boolean isTokenValid(String token) {
        try {
            return !isTokenExpired(token);
        } catch (Exception e) {
            logger.debug("Token JWT inválido: {}", e.getMessage());
            return false;
        }
    }

    private boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    private Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    private Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSignInKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    private SecretKey getSignInKey() {
        byte[] keyBytes = Decoders.BASE64.decode(secretKey);
        return Keys.hmacShaKeyFor(keyBytes);
    }
}
