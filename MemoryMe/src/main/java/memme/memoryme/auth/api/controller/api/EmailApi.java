package memme.memoryme.auth.api.controller.api;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import memme.memoryme.auth.api.dto.email.EmailRequestDTO;
import memme.memoryme.auth.api.dto.email.EmailResponseDTO;
import memme.memoryme.auth.api.dto.email.VerifyEmailRequestDTO;
import memme.memoryme.auth.api.dto.email.VerifyEmailResponseDTO;
import memme.memoryme.global.util.response.ResponseWrapper;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

@Tag(name = "Email API", description = "이메일 인증 API")
@RequestMapping("/v1/email")
public interface EmailApi {
    @Operation(summary = "이메일 인증 요청")
    @PostMapping("/request")
    ResponseEntity<ResponseWrapper<EmailResponseDTO>> requestEmailVerification(
            @Parameter(description = "이메일 요청")
            @RequestBody EmailRequestDTO request
    );

    @Operation(summary = "이메일 인증 확인")
    @PostMapping("/verify")
    ResponseEntity<ResponseWrapper<VerifyEmailResponseDTO>> verifyEmail(
            @Parameter(description = "이메일 인증")
            @RequestBody VerifyEmailRequestDTO request
    );
}
