package memme.memoryme.auth.application.service;

import lombok.RequiredArgsConstructor;
import memme.memoryme.auth.domain.EmailVerificationEntity;
import memme.memoryme.auth.infra.EmailVerificationRepository;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.Random;

@Service
@RequiredArgsConstructor
public class EmailService {

    private final EmailVerificationRepository emailVerificationRepository;
    private final JavaMailSender mailSender;

    @Transactional
    public void sendVerificationEmail(String email) {
        String verificationCode = generateVerificationCode();
        LocalDateTime expiryDate = LocalDateTime.now().plusMinutes(5);

        Optional<EmailVerificationEntity> existingEmail = emailVerificationRepository.findByEmail(email);
        EmailVerificationEntity emailVerificationEntity;

        if (existingEmail.isPresent()) {
            emailVerificationEntity = existingEmail.get();
            if (!emailVerificationEntity.isVerified()) {
                emailVerificationEntity.setVerificationCode(verificationCode);
                emailVerificationEntity.setExpiryDate(expiryDate);
                emailVerificationEntity.setVerified(false);
            }
        }
        else {
            emailVerificationEntity = EmailVerificationEntity.builder()
                    .email(email)
                    .verificationCode(verificationCode)
                    .expiryDate(expiryDate)
                    .verified(false)
                    .build();
        }
        emailVerificationRepository.save(emailVerificationEntity);

        sendEmail(email, verificationCode);
    }

    @Transactional
    public boolean verifyEmail(String email, String code) {
        Optional<EmailVerificationEntity> emailVerificationOptional = emailVerificationRepository.findByEmail(email);

        if (emailVerificationOptional.isEmpty()) {
            return false;
        }

        EmailVerificationEntity emailVerificationEntity = emailVerificationOptional.get();

        if (emailVerificationEntity.getExpiryDate().isBefore(LocalDateTime.now())) {
            return false;
        }

        if (emailVerificationEntity.getVerificationCode().equals(code)) {
            emailVerificationEntity.setVerified(true);
            emailVerificationRepository.save(emailVerificationEntity);
            return true;
        }

        return false;
    }

    private String generateVerificationCode() {
        Random random = new Random();
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