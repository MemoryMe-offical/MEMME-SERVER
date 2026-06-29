package memme.memoryme.global.util.apple;

import com.fasterxml.jackson.databind.JsonNode;
import io.jsonwebtoken.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.math.BigInteger;
import java.security.Key;
import java.security.KeyFactory;
import java.security.spec.RSAPublicKeySpec;
import java.util.Base64;

@Component
public class AppleJwtProvider {

    private static final String APPLE_KEYS_URL =
            "https://appleid.apple.com/auth/keys";

    private final RestTemplate restTemplate;
    private final AppleClientSecretProvider appleClientSecretProvider;

    private JsonNode cachedKeys;
    private long lastUpdatedTime = 0;
    private static final long CACHE_TTL = 60 * 60 * 1000; // 1시간

    public AppleJwtProvider(RestTemplate restTemplate, AppleClientSecretProvider appleClientSecretProvider) {
        this.restTemplate = restTemplate;
        this.appleClientSecretProvider = appleClientSecretProvider;
    }

    public String verifyAndGetAppleId(String idToken) {
        try {
            Jws<Claims> claims = Jwts.parserBuilder()
                    .setSigningKeyResolver(new AppleSigningKeyResolver(getKeys()))
                    .build()
                    .parseClaimsJws(idToken);

            Claims body = claims.getBody();

            if (!"https://appleid.apple.com".equals(body.getIssuer())) {
                throw new RuntimeException("Invalid issuer");
            }

            String clientId = appleClientSecretProvider.getClientId();
            if (!clientId.equals(body.getAudience())) {
                throw new RuntimeException("Invalid audience");
            }

            return body.getSubject();

        } catch (Exception e) {
            throw new RuntimeException("Invalid Apple id token", e);
        }
    }

    private JsonNode getKeys() {
        long now = System.currentTimeMillis();

        if (cachedKeys == null || now - lastUpdatedTime > CACHE_TTL) {
            cachedKeys = restTemplate.getForObject(APPLE_KEYS_URL, JsonNode.class);
            lastUpdatedTime = now;
        }
        return cachedKeys;
    }

    private class AppleSigningKeyResolver extends SigningKeyResolverAdapter {
        private final JsonNode jwks;

        public AppleSigningKeyResolver(JsonNode jwks) {
            this.jwks = jwks;
        }

        @Override
        public Key resolveSigningKey(JwsHeader header, Claims claims) {

            try {
                String kid = header.getKeyId();

                for (JsonNode key : jwks.get("keys")) {

                    if (kid.equals(key.get("kid").asText())) {

                        String n = key.get("n").asText();
                        String e = key.get("e").asText();

                        byte[] modulusBytes = Base64.getUrlDecoder().decode(n);
                        byte[] exponentBytes = Base64.getUrlDecoder().decode(e);

                        BigInteger modulus = new BigInteger(1, modulusBytes);
                        BigInteger exponent = new BigInteger(1, exponentBytes);

                        RSAPublicKeySpec spec = new RSAPublicKeySpec(modulus, exponent);
                        KeyFactory factory = KeyFactory.getInstance("RSA");

                        return factory.generatePublic(spec);
                    }
                }
                throw new RuntimeException("Apple public key not found");

            } catch (Exception e) {
                throw new RuntimeException("Failed to resolve Apple public key", e);
            }
        }
    }
    public String getEmail(String idToken) {
        try {
            Jws<Claims> claims = Jwts.parserBuilder()
                    .setSigningKeyResolver(new AppleSigningKeyResolver(restTemplate.getForObject(APPLE_KEYS_URL, JsonNode.class)))
                    .build()
                    .parseClaimsJws(idToken);

            return claims.getBody().get("email", String.class);

        } catch (Exception e) {
            return null;
        }
    }
}