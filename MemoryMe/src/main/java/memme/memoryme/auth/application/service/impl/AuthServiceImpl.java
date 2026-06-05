package memme.memoryme.auth.application.service.impl;

import lombok.RequiredArgsConstructor;
import memme.memoryme.auth.api.dto.email.EmailResponseDTO;
import memme.memoryme.auth.api.dto.email.LoginResponseDTO;
import memme.memoryme.auth.api.dto.email.RegisterResponseDTO;
import memme.memoryme.auth.api.dto.email.VerifyEmailResponseDTO;
import memme.memoryme.auth.api.dto.kakao.KakaoLoginRequestDTO;
import memme.memoryme.auth.api.dto.kakao.KakaoTokenResponseDTO;
import memme.memoryme.auth.api.dto.kakao.KakaoUserInfoResponseDTO;
import memme.memoryme.auth.application.service.AuthService;
import memme.memoryme.auth.application.service.EmailService;
import memme.memoryme.auth.domain.EmailVerificationEntity;
import memme.memoryme.auth.exception.AuthErrorCode;
import memme.memoryme.auth.infra.EmailVerificationRepository;
import memme.memoryme.global.exception.BusinessException;
import memme.memoryme.global.exception.CommonErrorCode;
import memme.memoryme.global.util.jwt.JwtUtil;
import memme.memoryme.user.domain.UserEntity;
import memme.memoryme.user.infra.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {
    private final EmailVerificationRepository emailVerificationRepository;
    private final UserRepository userRepository;
    private final EmailService emailService;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    @Override
    @Transactional
    public EmailResponseDTO requestEmailVerification(String email) {
        if (userRepository.findByEmail(email).isPresent()) {
            throw new BusinessException(AuthErrorCode.DUPLICATE_EMAIL);}
        return emailService.sendVerificationEmail(email);
    }

    @Override
    @Transactional
    public VerifyEmailResponseDTO verifyEmail(String email, String code) {
        return emailService.verifyEmail(email, code);}

    @Override
    @Transactional
    public RegisterResponseDTO completeRegistration(String email, String password, String userName) {
        EmailVerificationEntity verification =
                emailVerificationRepository.findByEmail(email)
                        .orElseThrow(() -> new BusinessException(AuthErrorCode.VERIFICATION_NOT_FOUND));

        if (!verification.isVerified()) {
            throw new BusinessException(AuthErrorCode.EMAIL_NOT_VERIFIED);        }

        if (userRepository.findByEmail(email).isPresent()) {
            throw new BusinessException(AuthErrorCode.DUPLICATE_EMAIL);        }

        UserEntity user = UserEntity.builder()
                .email(email)
                .password(passwordEncoder.encode(password))
                .uid(UUID.randomUUID())
                .emailVerified(true)
                .userName(userName)
                .build();
        userRepository.save(user);
        emailVerificationRepository.delete(verification);

        return new RegisterResponseDTO(
                user.getEmail(),
                user.getUserName()
        );}

    @Override
    @Transactional
    public LoginResponseDTO login(String email, String password) {
        UserEntity user = userRepository.findByEmail(email)
                .orElseThrow(() -> new BusinessException(AuthErrorCode.USER_NOT_FOUND));

        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new BusinessException(AuthErrorCode.PASSWORD_MISMATCH);
        }
        String accessToken = jwtUtil.createAccessToken(user.getEmail(), user.getUid().toString());
        String refreshToken = jwtUtil.createRefreshToken(user.getUid().toString());

        user.setRefreshToken(refreshToken);
        userRepository.save(user);

        return new LoginResponseDTO(
                accessToken,
                refreshToken,
                jwtUtil.getAccessTokenExpirationSeconds()
        );
    }

    @Override
    @Transactional
    public LoginResponseDTO refresh(String refreshToken) {

        if (!jwtUtil.validateToken(refreshToken)) {
            throw new BusinessException(AuthErrorCode.INVALID_TOKEN);}

        if (!jwtUtil.getTokenType(refreshToken).equals("refresh")) {
            throw new BusinessException(AuthErrorCode.NOT_A_REFRESH_TOKEN);}

        String uid = jwtUtil.getUidFromToken(refreshToken);

        UserEntity user = userRepository.findByUid(UUID.fromString(uid))
                .orElseThrow(() -> new BusinessException(AuthErrorCode.USER_NOT_FOUND));

        if (user.getRefreshToken() == null || !refreshToken.equals(user.getRefreshToken())) {
            throw new BusinessException(AuthErrorCode.TOKEN_MISMATCH);}

        String newAccessToken = jwtUtil.createAccessToken(
                user.getEmail(),
                user.getUid().toString()
        );

        return new LoginResponseDTO(
                newAccessToken,
                refreshToken,
                jwtUtil.getAccessTokenExpirationSeconds()
        );
    }

    @Override
    @Transactional
    public void resetPassword(String email, String newPassword) {

        EmailVerificationEntity verification =
                emailVerificationRepository.findByEmail(email)
                        .orElseThrow(() -> new BusinessException(AuthErrorCode.VERIFICATION_NOT_FOUND));

        if (!verification.isVerified()) {
            throw new BusinessException(AuthErrorCode.EMAIL_NOT_VERIFIED);}

        UserEntity user = userRepository.findByEmail(email)
                .orElseThrow(() -> new BusinessException(AuthErrorCode.USER_NOT_FOUND));

        user.setPassword(passwordEncoder.encode(newPassword));
        user.setRefreshToken(null);
        userRepository.save(user);
        emailVerificationRepository.delete(verification);
    }

    @Override
    @Transactional
    public VerifyEmailResponseDTO verifyPasswordResetEmail(
            String email,
            String code
    ) {
        return emailService.verifyEmail(email, code);
    }

    @Override
    @Transactional
    public EmailResponseDTO requestPasswordResetEmail(String email) {

        if (userRepository.findByEmail(email).isEmpty()) {
            throw new BusinessException(AuthErrorCode.USER_NOT_FOUND);}
        return emailService.sendVerificationEmail(email);
    }

    private final JwtUtil tokenProvider;
    private final RestTemplate restTemplate;

    @Value("${kakao.client-id}")
    private String clientId;

    @Value("${kakao.redirect-uri}")
    private String redirectUri;

    @Value("${kakao.client-secret}")
    private String clientSecret;

    @Override
    @Transactional
    public LoginResponseDTO kakaoLogin(KakaoLoginRequestDTO request) {

        MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();

        formData.add("grant_type", "authorization_code");
        formData.add("client_id", clientId);
        formData.add("redirect_uri", redirectUri);
        formData.add("code", request.code());
        formData.add("client_secret", clientSecret);

        HttpHeaders tokenHeaders = new HttpHeaders();
        tokenHeaders.setContentType(
                MediaType.APPLICATION_FORM_URLENCODED
        );

        HttpEntity<MultiValueMap<String, String>> tokenRequest = new HttpEntity<>(formData, tokenHeaders);

        try {
            ResponseEntity<KakaoTokenResponseDTO> tokenResponse =
                    restTemplate.postForEntity(
                            "https://kauth.kakao.com/oauth/token",
                            tokenRequest,
                            KakaoTokenResponseDTO.class
                    );

            KakaoTokenResponseDTO tokenBody = tokenResponse.getBody();

            if (tokenBody == null || tokenBody.accessToken() == null) {
                throw new BusinessException(AuthErrorCode.KAKAO_TOKEN_EXCHANGE_FAILED);            }

            HttpHeaders userHeaders = new HttpHeaders();
            userHeaders.setBearerAuth(tokenBody.accessToken());
            HttpEntity<Void> userRequest = new HttpEntity<>(userHeaders);

            ResponseEntity<KakaoUserInfoResponseDTO> userInfoResponse =
                    restTemplate.exchange(
                            "https://kapi.kakao.com/v2/user/me",
                            HttpMethod.GET,
                            userRequest,
                            KakaoUserInfoResponseDTO.class
                    );

            KakaoUserInfoResponseDTO userInfo = userInfoResponse.getBody();

            if (userInfo == null || userInfo.kakaoAccount() == null || userInfo.kakaoAccount().email() == null) {
                throw new BusinessException(AuthErrorCode.KAKAO_USER_PROFILE_FAILED);}

            String providerId = String.valueOf(userInfo.id());
            String email = userInfo.kakaoAccount().email();
            String nickname = email.split("@")[0];

            Optional<UserEntity> existingUser =
                    userRepository.findByProviderAndProviderId("KAKAO", providerId);

            UserEntity user = existingUser.orElseGet(() -> {
                UserEntity newUser = UserEntity.builder()
                        .uid(UUID.randomUUID())
                        .email(email)
                        .userName(nickname)
                        .password(passwordEncoder.encode(UUID.randomUUID().toString()))
                        .provider("KAKAO")
                        .providerId(providerId)
                        .emailVerified(true)
                        .build();

                UserEntity savedUser = userRepository.save(newUser);
                return savedUser;
            });

            String accessToken = tokenProvider.createAccessToken(user.getEmail(), user.getUid().toString());
            String refreshToken = tokenProvider.createRefreshToken(user.getUid().toString());

            user.setRefreshToken(refreshToken);
            userRepository.save(user);

            Long expiresIn = tokenProvider.getAccessTokenExpirationSeconds();
            return new LoginResponseDTO(accessToken, refreshToken, expiresIn);
        } catch (HttpClientErrorException e) {
            throw new BusinessException(AuthErrorCode.KAKAO_API_SERVER_ERROR);
        } catch (Exception e) {
            e.printStackTrace();
            throw new BusinessException(CommonErrorCode.INTERNAL_SERVER_ERROR);        }
    }
}
