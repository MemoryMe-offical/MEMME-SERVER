package memme.memoryme.auth.application.service.impl;

import lombok.RequiredArgsConstructor;
import memme.memoryme.auth.api.dto.email.EmailResponseDTO;
import memme.memoryme.auth.api.dto.email.VerifyEmailResponseDTO;
import memme.memoryme.auth.application.service.EmailService;
import memme.memoryme.auth.domain.EmailVerificationEntity;
import memme.memoryme.auth.infra.EmailVerificationRepository;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class EmailServiceImpl implements EmailService {

    private final EmailVerificationRepository emailVerificationRepository;
    private final JavaMailSender mailSender;

    @Override
    @Transactional
    public EmailResponseDTO sendVerificationEmail(String email) {

        String verificationCode = generateVerificationCode();
        LocalDateTime expiryDate = LocalDateTime.now().plusMinutes(5);

        EmailVerificationEntity entity = emailVerificationRepository.findByEmail(email)
                .map(existing -> {
                    if (!existing.isVerified()) {
                        existing.setVerificationCode(verificationCode);
                        existing.setExpiryDate(expiryDate);
                        existing.setVerified(false);
                    }
                    return existing;
                })
                .orElseGet(() -> EmailVerificationEntity.builder()
                        .email(email)
                        .verificationCode(verificationCode)
                        .expiryDate(expiryDate)
                        .verified(false)
                        .build());

        emailVerificationRepository.save(entity);
        sendEmail(email, verificationCode);

        return new EmailResponseDTO(email);
    }

    @Override
    @Transactional
    public VerifyEmailResponseDTO verifyEmail(String email, String code) {

        EmailVerificationEntity entity = emailVerificationRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("이메일 인증 요청이 존재하지 않습니다."));

        if (entity.getExpiryDate().isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("인증 코드가 만료되었습니다.");
        }

        if (!entity.getVerificationCode().equals(code)) {
            throw new IllegalArgumentException("인증 코드가 일치하지 않습니다.");
        }

        entity.setVerified(true);
        emailVerificationRepository.save(entity);

        return new VerifyEmailResponseDTO(email);
    }

    private String generateVerificationCode() {
        SecureRandom random = new SecureRandom();
        int code = 100000 + random.nextInt(900000);
        return String.valueOf(code);
    }

    private void sendEmail(String to, String code) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(to);
        message.setSubject("이메일 인증 코드");
        message.setText("인증 코드: " + code);
        mailSender.send(message);
    }
}