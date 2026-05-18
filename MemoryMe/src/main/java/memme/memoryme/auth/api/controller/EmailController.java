package memme.memoryme.auth.api.controller;

import lombok.RequiredArgsConstructor;
import memme.memoryme.auth.api.controller.api.EmailApi;
import memme.memoryme.auth.api.dto.email.*;
import memme.memoryme.auth.application.service.AuthService;
import memme.memoryme.global.util.response.ResponseWrapper;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
public class EmailController implements EmailApi {

    private final AuthService authService;

    @Override
    public ResponseEntity<ResponseWrapper<EmailResponseDTO>> requestEmailVerification(@RequestBody EmailRequestDTO request) {
        EmailResponseDTO response =
                authService.requestEmailVerification(request.email());

        return ResponseEntity.ok(
                ResponseWrapper.ok(200, "이메일 인증 요청 성공", response)
        );
    }

    @Override
    public ResponseEntity<ResponseWrapper<VerifyEmailResponseDTO>> verifyEmail(@RequestBody VerifyEmailRequestDTO request) {
        VerifyEmailResponseDTO response =
                authService.verifyEmail(request.email(), request.code());

        return ResponseEntity.ok(
                ResponseWrapper.ok(200, "이메일 인증 성공", response)
        );

    }
}