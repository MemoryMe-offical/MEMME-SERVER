package memme.memoryme.auth.api.dto.kakao;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "카카오 토큰 응답 DTO")
public record KakaoTokenResponseDTO(

        @Schema(description = "카카오 Access Token")
        @JsonProperty("access_token")
        String accessToken,

        @Schema(description = "카카오 Refresh Token")
        @JsonProperty("refresh_token")
        String refreshToken,

        @Schema(description = "토큰 만료 시간")
        @JsonProperty("expires_in")
        Integer expiresIn
) {
}