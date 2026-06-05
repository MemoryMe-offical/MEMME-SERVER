package memme.memoryme.auth.api.controller;

import lombok.RequiredArgsConstructor;
import memme.memoryme.auth.api.controller.api.AuthApi;
import memme.memoryme.auth.api.dto.email.*;
import memme.memoryme.auth.api.dto.kakao.KakaoLoginRequestDTO;
import memme.memoryme.auth.api.dto.pw.PwResetRequestDTO;
import memme.memoryme.auth.application.service.AuthService;
import memme.memoryme.global.util.response.ResponseWrapper;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
public class AuthController implements AuthApi {
    private final AuthService authService;

    @Override
    public ResponseEntity<ResponseWrapper<RegisterResponseDTO>> completeRegistration(@RequestBody RegisterRequestDTO request) {
        RegisterResponseDTO response =
                authService.completeRegistration(
                        request.email(),
                        request.password(),
                        request.userName()
                );
        return ResponseEntity.status(HttpStatus.CREATED).body(
                ResponseWrapper.ok(201,"회원가입 완료", response)
        );
    }

    @Override
    public ResponseEntity<ResponseWrapper<LoginResponseDTO>> login(@RequestBody LoginRequestDTO request) {
        LoginResponseDTO response = authService.login(request.email(), request.password());
        return ResponseEntity.ok(
                ResponseWrapper.ok(200, "로그인 성공", response)
        );
    }

    @Override
    public ResponseEntity<ResponseWrapper<LoginResponseDTO>> refresh(@RequestBody RefreshRequestDTO request) {
        LoginResponseDTO response = authService.refresh(request.refreshToken());
        return ResponseEntity.ok(ResponseWrapper.ok(200, "토큰 재발급 성공", response)
        );
    }

    @Override
    public ResponseEntity<ResponseWrapper<Void>> resetPassword(@RequestBody PwResetRequestDTO request) {
        authService.resetPassword(request.email(), request.newPassword());
        return ResponseEntity.ok(ResponseWrapper.ok(200, "비밀번호 변경 완료", null)
        );
    }
    @Override
    public ResponseEntity<ResponseWrapper<EmailResponseDTO>> requestPasswordResetEmail(@RequestParam String  email) {
        EmailResponseDTO response = authService.requestPasswordResetEmail(email);
        return ResponseEntity.ok(
                ResponseWrapper.ok(200, "비밀번호 재설정 인증 메일 발송 완료", response)
        );
    }
    @Override
    public ResponseEntity<ResponseWrapper<VerifyEmailResponseDTO>> verifyPasswordResetEmail(@RequestParam String email, @RequestParam String code) {
        VerifyEmailResponseDTO response = authService.verifyPasswordResetEmail(email, code);
        return ResponseEntity.ok(
                ResponseWrapper.ok(200, "비밀번호 재설정 이메일 인증 완료", response)
        );
    }

    @Override
    public ResponseEntity<ResponseWrapper<LoginResponseDTO>> kakaoLogin(@RequestBody KakaoLoginRequestDTO request) {
        LoginResponseDTO response = authService.kakaoLogin(request);
        return ResponseEntity.ok(
                ResponseWrapper.ok(200, "카카오 로그인 성공", response)
        );
    }
}