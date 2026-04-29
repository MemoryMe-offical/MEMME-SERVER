package memme.memoryme.auth.application.service;

import lombok.RequiredArgsConstructor;
import memme.memoryme.auth.api.dto.email.LoginResponseDTO;
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
public class AuthService {

    private final EmailVerificationRepository emailVerificationRepository;
    private final UserRepository userRepository;
    private final EmailService emailService;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    @Transactional
    public void requestEmailVerification(String email) {
        if (userRepository.findByEmail(email).isPresent()) {
            throw new RuntimeException("이미 가입된 이메일입니다.");
        }
        emailService.sendVerificationEmail(email);
    }

    @Transactional
    public void verifyEmail(String email, String code) {
        boolean success = emailService.verifyEmail(email, code);
        if (!success) {
            throw new RuntimeException("인증 코드가 올바르지 않거나 만료되었습니다.");
        }
    }

    @Transactional
    public void completeRegistration(String email, String password, String userName) {
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
    }

    @Transactional(readOnly = true)
    public LoginResponseDTO login(String email, String password) {
        UserEntity user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("사용자가 존재하지 않습니다."));

        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new RuntimeException("비밀번호가 올바르지 않습니다.");
        }
        String token = jwtUtil.createToken(user.getEmail(), user.getUid().toString());
        return new LoginResponseDTO(token);
    }
}