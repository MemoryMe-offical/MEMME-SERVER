package memme.memoryme.auth.api.dto.pw;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "비밀번호 재설정 요청 DTO")
public record PwResetRequestDTO(
        @Schema(description = "이메일", example = "test@example.com")
        String email,
        String newPassword
) {
}