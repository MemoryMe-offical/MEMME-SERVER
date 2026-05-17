package memme.memoryme.auth.application.service;


import memme.memoryme.auth.api.dto.email.EmailResponseDTO;
import memme.memoryme.auth.api.dto.email.VerifyEmailResponseDTO;

public interface EmailService {

    EmailResponseDTO sendVerificationEmail(String email);
    VerifyEmailResponseDTO verifyEmail(String email, String code);
}