package memme.memoryme.auth.application.service.impl;

import lombok.RequiredArgsConstructor;
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
        AppleTokenResponse tokenResponse = requestAppleToken(request.code());

        if (tokenResponse == null || tokenResponse.idToken() == null) {
            throw new BusinessException(AuthErrorCode.APPLE_TOKEN_EXCHANGE_FAILED);
        }

        String appleId = appleJwtProvider.verifyAndGetAppleId(tokenResponse.idToken());
        String email = appleJwtProvider.getEmail(tokenResponse.idToken());

        UserEntity user = userRepository
                .findByProviderAndProviderId("APPLE", appleId)
                .orElseGet(() -> createNewAppleUser(appleId, email));

        if (email != null && (user.getEmail() == null || user.getEmail().isEmpty())) {
            user.setEmail(email);
        }

        String accessToken = jwtUtil.createAccessToken(
                user.getEmail() == null ? "" : user.getEmail(),
                user.getUid().toString()
        );

        String refreshToken = jwtUtil.createRefreshToken(
                user.getUid().toString()
        );

        user.setRefreshToken(refreshToken);
        userRepository.save(user);

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

            if (!response.getStatusCode().is2xxSuccessful()) {
                throw new BusinessException(AuthErrorCode.APPLE_TOKEN_EXCHANGE_FAILED);
            }

            return response.getBody();

        } catch (Exception e) {
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