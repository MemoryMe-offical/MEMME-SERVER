package memme.memoryme.auth.api.dto.email;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "refresh 토큰 요청 DTO")
public record RefreshRequestDTO(
    @Schema(description = "refresh 토큰", example = "eyJhbGciOiJIUzI1NiJ9...")
    String refreshToken
) {
}
