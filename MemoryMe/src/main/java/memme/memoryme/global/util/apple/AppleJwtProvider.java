package memme.memoryme.global.util.apple;

import io.jsonwebtoken.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.math.BigInteger;
import java.security.Key;
import java.security.KeyFactory;
import java.security.spec.RSAPublicKeySpec;
import java.util.Base64;

@Slf4j
@Component
public class AppleJwtProvider {

    private static final String APPLE_KEYS_URL =
            "https://appleid.apple.com/auth/keys";

    private final RestTemplate restTemplate;
    private final AppleClientSecretProvider appleClientSecretProvider;

    private ApplePublicKeyResponse cachedKeys;
    private long lastUpdatedTime = 0;
    private static final long CACHE_TTL = 60 * 60 * 1000; // 1시간

    public AppleJwtProvider(RestTemplate restTemplate, AppleClientSecretProvider appleClientSecretProvider) {
        this.restTemplate = restTemplate;
        this.appleClientSecretProvider = appleClientSecretProvider;
    }

    public String verifyAndGetAppleId(String idToken) {
        try {
            Jws<Claims> claims = parseClaims(idToken, getKeys());

            Claims body = claims.getBody();

            if (!"https://appleid.apple.com".equals(body.getIssuer())) {
                throw new RuntimeException("Invalid issuer: " + body.getIssuer());
            }

            String clientId = appleClientSecretProvider.getClientId();
            if (!clientId.equals(body.getAudience())) {
                throw new RuntimeException(
                        "Invalid audience. expected=" + clientId + ", actual=" + body.getAudience()
                );
            }

            return body.getSubject();

        } catch (Exception e) {
            if (isApplePublicKeyNotFound(e)) {
                return verifyAndGetAppleIdAfterRefreshingKeys(idToken, e);
            }
            log.error("Apple id token validation failed: {}", e.getMessage(), e);
            throw new RuntimeException("Invalid Apple id token", e);
        }
    }

    private String verifyAndGetAppleIdAfterRefreshingKeys(String idToken, Exception firstException) {
        try {
            log.warn("Apple public key not found in cache. Refreshing Apple JWKS and retrying token validation.");
            Jws<Claims> claims = parseClaims(idToken, refreshKeys());
            Claims body = claims.getBody();

            if (!"https://appleid.apple.com".equals(body.getIssuer())) {
                throw new RuntimeException("Invalid issuer: " + body.getIssuer());
            }

            String clientId = appleClientSecretProvider.getClientId();
            if (!clientId.equals(body.getAudience())) {
                throw new RuntimeException(
                        "Invalid audience. expected=" + clientId + ", actual=" + body.getAudience()
                );
            }

            return body.getSubject();
        } catch (Exception retryException) {
            log.error("Apple id token validation failed after refreshing JWKS: {}", retryException.getMessage(), retryException);
            retryException.addSuppressed(firstException);
            throw new RuntimeException("Invalid Apple id token", retryException);
        }
    }

    private Jws<Claims> parseClaims(String idToken, ApplePublicKeyResponse keys) {
        return Jwts.parserBuilder()
                .setSigningKeyResolver(new AppleSigningKeyResolver(keys))
                .build()
                .parseClaimsJws(idToken);
    }

    private synchronized ApplePublicKeyResponse getKeys() {
        long now = System.currentTimeMillis();

        if (cachedKeys == null || now - lastUpdatedTime > CACHE_TTL) {
            refreshKeys();
        }
        return cachedKeys;
    }

    private synchronized ApplePublicKeyResponse refreshKeys() {
        cachedKeys = restTemplate.getForObject(APPLE_KEYS_URL, ApplePublicKeyResponse.class);
        lastUpdatedTime = System.currentTimeMillis();

        if (cachedKeys == null || cachedKeys.keys() == null) {
            throw new RuntimeException("Failed to load Apple public keys");
        }

        return cachedKeys;
    }

    private boolean isApplePublicKeyNotFound(Throwable throwable) {
        Throwable current = throwable;
        while (current != null) {
            if (current.getMessage() != null && current.getMessage().contains("Apple public key not found")) {
                return true;
            }
            current = current.getCause();
        }
        return false;
    }

    private class AppleSigningKeyResolver extends SigningKeyResolverAdapter {
        private final ApplePublicKeyResponse jwks;

        public AppleSigningKeyResolver(ApplePublicKeyResponse jwks) {
            this.jwks = jwks;
        }

        @Override
        public Key resolveSigningKey(JwsHeader header, Claims claims) {

            try {
                String kid = header.getKeyId();

                for (ApplePublicKey key : jwks.keys()) {

                    if (kid.equals(key.kid())) {

                        String n = key.n();
                        String e = key.e();

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
            Jws<Claims> claims = parseClaims(idToken, getKeys());

            return claims.getBody().get("email", String.class);

        } catch (Exception e) {
            log.warn("Failed to extract email from Apple id token: {}", e.getMessage());
            return null;
        }
    }
}