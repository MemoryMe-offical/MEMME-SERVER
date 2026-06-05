package memme.memoryme.auth.api.dto.kakao;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "카카오 사용자 정보 응답 DTO")
public record KakaoUserInfoResponseDTO(

        @Schema(description = "카카오 사용자 ID")
        Long id,

        @Schema(description = "카카오 계정 정보")
        @JsonProperty("kakao_account")
        KakaoAccount kakaoAccount
) {
    public record KakaoAccount(
            String email
) {}
}
