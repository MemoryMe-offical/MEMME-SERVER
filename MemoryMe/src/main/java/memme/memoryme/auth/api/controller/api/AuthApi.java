package memme.memoryme.auth.api.controller.api;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import memme.memoryme.auth.api.dto.apple.AppleLoginRequest;
import memme.memoryme.auth.api.dto.email.*;
import memme.memoryme.auth.api.dto.kakao.KakaoLoginRequestDTO;
import memme.memoryme.auth.api.dto.pw.PwResetRequestDTO;
import memme.memoryme.global.util.response.ResponseWrapper;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.security.core.userdetails.UserDetails;

@Tag(name = "Auth API", description = "회원가입 API")
@RequestMapping("/v1/auth")
public interface AuthApi {
    @Operation(summary = "회원가입")
    @PostMapping("/register")
    ResponseEntity<ResponseWrapper<RegisterResponseDTO>> completeRegistration(
            @Parameter(description = "회원가입 요청")
            @RequestBody RegisterRequestDTO request
    );

    @Operation(summary = "로그인")
    @PostMapping("/login")
    ResponseEntity<ResponseWrapper<LoginResponseDTO>> login(
            @Parameter(description = "로그인 요청")
            @RequestBody LoginRequestDTO request
    );

    @Operation(summary = "토큰 재발급")
    @PostMapping("/refresh")
    ResponseEntity<ResponseWrapper<LoginResponseDTO>> refresh(
            @Parameter(description = "리프레시 토큰 요청")
            @RequestBody RefreshRequestDTO request
    );

    @Operation(summary = "비밀번호 재설정 이메일 인증 요청")
    @PostMapping("/pw/email/request")
    ResponseEntity<ResponseWrapper<EmailResponseDTO>> requestPasswordResetEmail(
            @RequestParam String email
    );

    @Operation(summary = "비밀번호 재설정 이메일 인증 확인")
    @PostMapping("/pw/email/verify")
    ResponseEntity<ResponseWrapper<VerifyEmailResponseDTO>> verifyPasswordResetEmail(
            @RequestParam String email,
            @RequestParam String code
    );

    @Operation(summary = "비밀번호 재설정")
    @PostMapping("/pw/reset")
    ResponseEntity<ResponseWrapper<Void>> resetPassword(
            @Parameter(description = "비밀번호 재설정 요청")
            @RequestBody PwResetRequestDTO request
    );

    @Operation(summary = "로그아웃")
    @PostMapping("/logout")
    ResponseEntity<ResponseWrapper<Void>> logout(
            @Parameter(description = "인증된 사용자의 고유 식별자(UID)")
            @AuthenticationPrincipal String uid
    );

    @Operation(summary = "카카오 로그인")
    @PostMapping("/kakao")
    ResponseEntity<ResponseWrapper<LoginResponseDTO>> kakaoLogin(
            @Parameter(description = "카카오 로그인 요청")
            @RequestBody KakaoLoginRequestDTO request
    );

    @Operation(summary = "애플 로그인")
    @PostMapping("/apple")
    ResponseEntity<ResponseWrapper<LoginResponseDTO>> appleLogin(
            @RequestBody AppleLoginRequest request
    );
}
