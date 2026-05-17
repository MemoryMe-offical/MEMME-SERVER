package memme.memoryme.auth.application.service.impl;

import lombok.RequiredArgsConstructor;
import memme.memoryme.auth.api.dto.email.EmailResponseDTO;
import memme.memoryme.auth.api.dto.email.LoginResponseDTO;
import memme.memoryme.auth.api.dto.email.RegisterResponseDTO;
import memme.memoryme.auth.api.dto.email.VerifyEmailResponseDTO;
import memme.memoryme.auth.application.service.AuthService;
import memme.memoryme.auth.application.service.EmailService;
import memme.memoryme.auth.domain.EmailVerificationEntity;
import memme.memoryme.auth.infra.EmailVerificationRepository;
import memme.memoryme.global.util.jwt.JwtUtil;
import memme.memoryme.user.domain.UserEntity;
import memme.memoryme.user.infra.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
            throw new RuntimeException("이미 가입된 이메일입니다.");
        }
        return emailService.sendVerificationEmail(email);
    }

    @Override
    @Transactional
    public VerifyEmailResponseDTO verifyEmail(String email, String code) {
        return emailService.verifyEmail(email, code);
    }

    @Override
    @Transactional
    public RegisterResponseDTO completeRegistration(String email, String password, String userName) {
        EmailVerificationEntity verification =
                emailVerificationRepository.findByEmail(email)
                        .orElseThrow(() -> new RuntimeException("인증 요청이 존재하지 않습니다."));

        if (!verification.isVerified()) {
            throw new RuntimeException("이메일 인증이 완료되지 않았습니다.");
        }

        if (userRepository.findByEmail(email).isPresent()) {
            throw new RuntimeException("이미 가입된 이메일입니다.");
        }

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
                .orElseThrow(() -> new RuntimeException("사용자가 존재하지 않습니다."));

        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new RuntimeException("비밀번호가 올바르지 않습니다.");
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
            throw new RuntimeException("토큰 오류");
        }

        if (!jwtUtil.getTokenType(refreshToken).equals("refresh")) {
            throw new RuntimeException("refresh 토큰 아님");
        }

        String uid = jwtUtil.getUidFromToken(refreshToken);

        UserEntity user = userRepository.findByUid(UUID.fromString(uid))
                .orElseThrow(() -> new RuntimeException("유저 없음"));

        if (user.getRefreshToken() == null || !refreshToken.equals(user.getRefreshToken())) {
            throw new RuntimeException("토큰 불일치");
        }

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
                        .orElseThrow(() -> new RuntimeException("인증 요청이 존재하지 않습니다."));

        if (!verification.isVerified()) {
            throw new RuntimeException("이메일 인증이 완료되지 않았습니다.");
        }

        UserEntity user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("유저 없음"));

        user.setPassword(passwordEncoder.encode(newPassword));

        // 기존 로그인 무효화
        user.setRefreshToken(null);

        userRepository.save(user);

        // 인증 상태 삭제
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
            throw new RuntimeException("가입되지 않은 이메일입니다.");
        }

        return emailService.sendVerificationEmail(email);
    }
}
