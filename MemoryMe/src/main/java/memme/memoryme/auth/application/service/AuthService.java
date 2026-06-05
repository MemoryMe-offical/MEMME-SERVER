package memme.memoryme.auth.application.service;

import memme.memoryme.auth.api.dto.email.EmailResponseDTO;
import memme.memoryme.auth.api.dto.email.LoginResponseDTO;
import memme.memoryme.auth.api.dto.email.RegisterResponseDTO;
import memme.memoryme.auth.api.dto.email.VerifyEmailResponseDTO;
import memme.memoryme.auth.api.dto.kakao.KakaoLoginRequestDTO;

public interface AuthService {

    EmailResponseDTO requestEmailVerification(String email);
    VerifyEmailResponseDTO verifyEmail(String email, String code);
    RegisterResponseDTO completeRegistration(String email, String password, String userName);
    LoginResponseDTO login(String email, String password);
    LoginResponseDTO refresh(String refreshToken);
    void resetPassword(String email, String newPassword);
    EmailResponseDTO requestPasswordResetEmail(String email);
    VerifyEmailResponseDTO verifyPasswordResetEmail(String email, String code);
    LoginResponseDTO kakaoLogin(KakaoLoginRequestDTO request);
}