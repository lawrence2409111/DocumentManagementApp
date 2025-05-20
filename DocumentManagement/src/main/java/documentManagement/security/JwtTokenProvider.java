package documentManagement.security;

import documentManagement.service.CustomUserDetailsService;
import io.jsonwebtoken.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.annotation.PostConstruct;
import javax.crypto.spec.SecretKeySpec;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.security.Key;
import java.util.Base64;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

// JWT Authentication Filter

// JWT Token Provider
@Component
@Slf4j
public class JwtTokenProvider {

    private Key secretKey;
    private final long validityInMilliseconds = 3600000; // 1 hour

    @Autowired
    private CustomUserDetailsService userDetailsService;

    @Autowired
    private SecretKeyInitializer secretKeyInitializer;

    // Use PostConstruct to initialize the secretKey
    @PostConstruct
    public void init() {
        this.secretKey = secretKeyInitializer.getSecretKey();
        if (this.secretKey != null) {
            byte[] encodedKey = this.secretKey.getEncoded();
            String base64EncodedKey = Base64.getEncoder().encodeToString(encodedKey);
            log.info("JWT Secret Key from Initializer: {}", base64EncodedKey);
        } else {
            log.warn("JWT Secret Key is null in JwtTokenProvider!");
        }
    }

    public String createToken(String username) {
        UserDetails userDetails = userDetailsService.loadUserByUsername(username);
        Claims claims = Jwts.claims().setSubject(username);

        // Add roles to the token's claims
        List<String> roles = userDetails.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toList());
        claims.put("roles", roles); // Store roles in the "roles" claim

        Date now = new Date();
        Date validity = new Date(now.getTime() + validityInMilliseconds);

        String token = Jwts.builder()
                .setClaims(claims)
                .setIssuedAt(now)
                .setExpiration(validity)
                .signWith(SignatureAlgorithm.HS256, secretKey) // Use the initialized secretKey
                .compact();
        log.info("Generated JWT Token: {}", token);
        return token;
    }


    public String getUsername(String token) {
        return Jwts.parser().setSigningKey(secretKey).parseClaimsJws(token).getBody().getSubject();
    }

    public String resolveToken(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }

    public boolean validateToken(String token) {
        try {
            Jws<Claims> claims = Jwts.parser().setSigningKey(secretKey).parseClaimsJws(token);
            log.info("Parsed Claims: {}", claims.getBody()); // Log the claims
            return !claims.getBody().getExpiration().before(new Date()); // Check expiration
        } catch (ExpiredJwtException e) {
            log.error("Token expired: {}", e.getMessage());
            return false;
        } catch (MalformedJwtException e) {
            log.error("Malformed token: {}", e.getMessage());
            return false;
        } catch (SignatureException e) {
            log.error("Invalid signature: {}", e.getMessage());
            return false;
        } catch (IllegalArgumentException e) {
            log.error("Invalid token: {}", e.getMessage());
            return false;
        }
    }

    public List<String> getRolesFromToken(String token) {
        Claims claims = Jwts.parser().setSigningKey(secretKey).parseClaimsJws(token).getBody();
        //The roles are stored as a List<String>, so we need to cast the value.
        List<String> roles = (List<String>) claims.get("roles");
        return roles;
    }
}
