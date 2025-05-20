package documentManagement.security;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import javax.annotation.PostConstruct;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Base64;
import java.security.SecureRandom;

@Slf4j
@Component
@Scope("singleton")
public class SecretKeyInitializer {

    private Key secretKey;
    private String jwtSecret;

    public Key getSecretKey() {
        return secretKey;
    }

    @PostConstruct
    public void init() {
        if (jwtSecret == null) {
            jwtSecret = generateSecretKey();
        }
        byte[] keyBytes = Base64.getDecoder().decode(jwtSecret);
        secretKey = new SecretKeySpec(keyBytes, "HmacSHA256");
    }

    private String generateSecretKey() {
        SecureRandom random = new SecureRandom();
        byte[] keyBytes = new byte[32];
        random.nextBytes(keyBytes);
        return Base64.getEncoder().encodeToString(keyBytes);
    }

    public String getJwtSecret() {
        return jwtSecret;
    }
}