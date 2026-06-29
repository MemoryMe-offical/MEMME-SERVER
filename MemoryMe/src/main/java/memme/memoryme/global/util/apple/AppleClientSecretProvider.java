package memme.memoryme.global.util.apple;

import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.JWSSigner;
import com.nimbusds.jose.crypto.ECDSASigner;
import org.springframework.core.io.Resource;import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import java.nio.charset.StandardCharsets;
import java.security.KeyFactory;
import java.security.interfaces.ECPrivateKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.Base64;
import java.util.Date;

@Getter
@Component
@RequiredArgsConstructor
public class AppleClientSecretProvider {
    @Value("${apple.team-id}")
    private String teamId;

    @Value("${apple.client-id}")
    private String clientId;

    @Value("${apple.key-id}")
    private String keyId;

    @Value("${apple.private-key-path}")
    private Resource privateKey;

    public String createClientSecret() {
        try {
            String privateKeyPem = new String(
                    privateKey.getInputStream().readAllBytes(),
                    StandardCharsets.UTF_8
            );

            privateKeyPem = privateKeyPem
                    .replace("-----BEGIN PRIVATE KEY-----", "")
                    .replace("-----END PRIVATE KEY-----", "")
                    .replaceAll("\\s", "");

            byte[] decoded = Base64.getDecoder().decode(privateKeyPem);

            PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(decoded);
            KeyFactory kf = KeyFactory.getInstance("EC");

            ECPrivateKey privateKey = (ECPrivateKey) kf.generatePrivate(keySpec);

            long now = System.currentTimeMillis();

            JWTClaimsSet claims = new JWTClaimsSet.Builder()
                    .issuer(teamId)
                    .issueTime(new Date(now))
                    .expirationTime(new Date(now + 1000 * 60 * 5))
                    .audience("https://appleid.apple.com")
                    .subject(clientId)
                    .build();

            JWSSigner signer = new ECDSASigner(privateKey);

            SignedJWT signedJWT = new SignedJWT(
                    new JWSHeader.Builder(JWSAlgorithm.ES256)
                            .keyID(keyId)
                            .build(),
                    claims
            );

            signedJWT.sign(signer);

            return signedJWT.serialize();

        } catch (Exception e) {
            throw new RuntimeException("Failed to create Apple client secret", e);
        }
    }
}
