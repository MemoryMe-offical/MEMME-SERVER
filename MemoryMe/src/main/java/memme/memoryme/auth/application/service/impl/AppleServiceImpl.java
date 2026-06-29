package memme.memoryme.auth.application.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import memme.memoryme.auth.api.dto.apple.AppleLoginRequest;
import memme.memoryme.auth.api.dto.apple.AppleTokenResponse;
import memme.memoryme.auth.api.dto.email.LoginResponseDTO;
import memme.memoryme.auth.application.service.AppleService;
import memme.memoryme.auth.exception.AuthErrorCode;
import memme.memoryme.global.exception.BusinessException;
import memme.memoryme.global.util.apple.AppleClientSecretProvider;
import memme.memoryme.global.util.apple.AppleJwtProvider;
import memme.memoryme.global.util.jwt.JwtUtil;
import memme.memoryme.user.domain.UserEntity;
import memme.memoryme.user.infra.UserRepository;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import java.util.UUID;
@Slf4j
@Service
@RequiredArgsConstructor
public class AppleServiceImpl implements AppleService {
    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;
    private final PasswordEncoder passwordEncoder;
    private final RestTemplate restTemplate;
    private final AppleClientSecretProvider appleClientSecretProvider;
    private final AppleJwtProvider appleJwtProvider;
    private static final String APPLE_TOKEN_URL = "https://appleid.apple.com/auth/token";

    @Override
    @Transactional
    public LoginResponseDTO login(AppleLoginRequest request) {
        log.info("1. Apple login start");
        AppleTokenResponse tokenResponse = requestAppleToken(request.code());

        log.info("2. Apple token received");
        if (tokenResponse == null || tokenResponse.idToken() == null) {
            log.error("idToken is null");
            throw new BusinessException(AuthErrorCode.APPLE_TOKEN_EXCHANGE_FAILED);
        }

        String appleId = appleJwtProvider.verifyAndGetAppleId(tokenResponse.idToken());
        log.info("3. Apple ID = {}", appleId);
        String email = appleJwtProvider.getEmail(tokenResponse.idToken());
        log.info("4. Email = {}", email);

        UserEntity user = userRepository
                .findByProviderAndProviderId("APPLE", appleId)
                .orElseGet(() -> createNewAppleUser(appleId, email));
        log.info("5. User uid = {}", user.getUid());

        if (email != null && (user.getEmail() == null || user.getEmail().isEmpty())) {
            user.setEmail(email);
        }

        String accessToken = jwtUtil.createAccessToken(
                user.getEmail() == null ? "" : user.getEmail(),
                user.getUid().toString()
        );
        log.info("6. AccessToken created");

        String refreshToken = jwtUtil.createRefreshToken(
                user.getUid().toString()
        );
        log.info("7. RefreshToken created");

        user.setRefreshToken(refreshToken);
        userRepository.save(user);
        log.info("8. User saved");

        return new LoginResponseDTO(
                accessToken,
                refreshToken,
                jwtUtil.getAccessTokenExpirationSeconds()
        );
    }

    private AppleTokenResponse requestAppleToken(String code) {

        try {
            MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
            params.add("client_id", appleClientSecretProvider.getClientId());
            params.add("client_secret", appleClientSecretProvider.createClientSecret());
            params.add("code", code);
            params.add("grant_type", "authorization_code");

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

            HttpEntity<MultiValueMap<String, String>> entity = new HttpEntity<>(params, headers);

            ResponseEntity<AppleTokenResponse> response =
                    restTemplate.postForEntity(
                            APPLE_TOKEN_URL,
                            entity,
                            AppleTokenResponse.class
                    );
            log.info("Apple token response = {}", response.getBody());

            if (!response.getStatusCode().is2xxSuccessful()) {
                throw new BusinessException(AuthErrorCode.APPLE_TOKEN_EXCHANGE_FAILED);
            }

            return response.getBody();

        } catch (Exception e) {
            log.error("Apple token request failed", e);
            throw new BusinessException(AuthErrorCode.APPLE_TOKEN_EXCHANGE_FAILED);
        }
    }

    private UserEntity createNewAppleUser(String appleId, String email) {

        UserEntity newUser = UserEntity.builder()
                .uid(UUID.randomUUID())
                .email(email)
                .userName("Apple_" + UUID.randomUUID().toString().substring(0, 8))
                .password(passwordEncoder.encode(UUID.randomUUID().toString()))
                .provider("APPLE")
                .providerId(appleId)
                .emailVerified(true)
                .build();

        return userRepository.save(newUser);
    }
}