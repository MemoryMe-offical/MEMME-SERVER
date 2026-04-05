package memme.memoryme.auth.api.controller;

import lombok.RequiredArgsConstructor;
import memme.memoryme.auth.api.dto.email.*;
import memme.memoryme.auth.application.service.AuthService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/v1/auth")
@RequiredArgsConstructor
public class EmailController {

    private final AuthService authService;

    @PostMapping("/email/request")
    public ResponseEntity<EmailResponseDTO> requestEmailVerification(@RequestBody EmailRequestDTO request) {
        authService.requestEmailVerification(request.getEmail());
        return ResponseEntity.ok(new EmailResponseDTO("인증 코드 이메일로 발송",
                request.getEmail())
        );
    }

    @PostMapping("/email/verify")
    public ResponseEntity<VerifyEmailResponseDTO> verifyEmail(@RequestBody VerifyEmailRequestDTO request) {
        authService.verifyEmail(request.getEmail(), request.getCode());
        return ResponseEntity.ok(new VerifyEmailResponseDTO("이메일 인증 완료",
                        request.getEmail())
        );
    }

    @PostMapping("/register")
    public ResponseEntity<RegisterResponseDTO> completeRegistration(@RequestBody RegisterRequestDTO request) {
        authService.completeRegistration(request.getEmail(), request.getPassword());
        return ResponseEntity.ok(new RegisterResponseDTO("회원가입 완료",
                request.getEmail())
        );
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponseDTO> login(@RequestBody LoginRequestDTO request) {
        LoginResponseDTO response = authService.login(request.getEmail(), request.getPassword());
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/pwchange")
    public ResponseEntity<PwChangeResponseDTO> changePassword(@RequestBody PwChangeRequestDTO request,
                                                              Authentication authentication) {
        String email = authentication.getName();
        authService.changePassword(email,
                request.getCurrentPassword(),
                request.getNewPassword());
        return ResponseEntity.ok(new PwChangeResponseDTO(email, "비밀번호 변경 완료"));
    }
}