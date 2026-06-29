package memme.memoryme.auth.api.dto.apple;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "애플 토큰 응답 DTO")
public record AppleTokenResponse(

        @JsonProperty("access_token")
        @Schema(description = "애플 Access Token")
        String accessToken,

        @JsonProperty("id_token")
        @Schema(description = "애플 ID Token")
        String idToken,

        @JsonProperty("refresh_token")
        @Schema(description = "애플 Refresh Token")
        String refreshToken,

        @JsonProperty("expires_in")
        @Schema(description = "토큰 만료 시간")
        Long expiresIn,

        @JsonProperty("token_type")
        @Schema(description = "토큰 타입")
        String tokenType

) {
}