package memme.memoryme.auth.api.dto.email;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "로그인 요청 DTO")
public record LoginRequestDTO(
    @Schema(description = "이메일", example = "test@example.com")
    String email,

    @Schema(description = "비밀번호", example = "1234")
    String password
) {
}