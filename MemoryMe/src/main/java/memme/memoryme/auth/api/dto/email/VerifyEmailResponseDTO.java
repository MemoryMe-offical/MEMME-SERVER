package memme.memoryme.auth.api.dto.email;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "이메일 인증 확인 응답 DTO")
public record VerifyEmailResponseDTO(
        @Schema(description = "이메일", example = "test@example.com")
        String email
) {
}
