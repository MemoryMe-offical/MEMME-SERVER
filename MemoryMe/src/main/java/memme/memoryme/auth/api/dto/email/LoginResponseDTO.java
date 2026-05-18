package memme.memoryme.auth.api.dto.email;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "로그인 응답 DTO")
public record LoginResponseDTO(
        @Schema(description = "JWT 요청용 토큰", example = "qwerasdfzxcv...")
        String accessToken,

        @Schema(description = "JWT 재발급용 토큰", example = "qwerasdfzxcv...")
        String refreshToken,

        @Schema(description = "AccessToken 만료 시간(초)", example = "3600")
        long expiresIn
){
}