package memme.memoryme.auth.api.dto.email;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "이메일 인증 확인 요청 DTO")
public record VerifyEmailRequestDTO(
    @Schema(description = "이메일", example = "test@example.com")
    String email,

    @Schema(description = "인증코드", example = "123456")
    String code
) {
}