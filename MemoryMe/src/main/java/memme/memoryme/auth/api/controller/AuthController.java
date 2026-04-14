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
public class AuthController {
    private final AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<RegisterResponseDTO> completeRegistration(@RequestBody RegisterRequestDTO request) {
        authService.completeRegistration(request.getEmail(), request.getPassword(), request.getUserName());
        return ResponseEntity.ok(new RegisterResponseDTO("회원가입 완료",
                request.getEmail(), request.getUserName())
        );
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponseDTO> login(@RequestBody LoginRequestDTO request) {
        LoginResponseDTO response = authService.login(request.getEmail(), request.getPassword());
        return ResponseEntity.ok(response);
    }


}
