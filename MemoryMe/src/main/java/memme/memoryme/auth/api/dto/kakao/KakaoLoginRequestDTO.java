package memme.memoryme.auth.api.dto.kakao;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "카카오 로그인 요청 DTO")
public record KakaoLoginRequestDTO(
        @Schema(description = "카카오 인가코드")
        String code
) {
}