package memme.memoryme.auth.api.controller;

import lombok.RequiredArgsConstructor;
import memme.memoryme.auth.api.dto.email.*;
import memme.memoryme.auth.application.service.AuthService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/v1/email")
@RequiredArgsConstructor
public class EmailController {

    private final AuthService authService;

    @PostMapping("/request")
    public ResponseEntity<EmailResponseDTO> requestEmailVerification(@RequestBody EmailRequestDTO request) {
        authService.requestEmailVerification(request.getEmail());
        return ResponseEntity.ok(new EmailResponseDTO("인증 코드 이메일로 발송",
                request.getEmail())
        );
    }

    @PostMapping("/verify")
    public ResponseEntity<VerifyEmailResponseDTO> verifyEmail(@RequestBody VerifyEmailRequestDTO request) {
        authService.verifyEmail(request.getEmail(), request.getCode());
        return ResponseEntity.ok(new VerifyEmailResponseDTO("이메일 인증 완료",
                        request.getEmail())
        );
    }

}