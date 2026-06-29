package memme.memoryme.auth.api.dto.apple;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "애플 로그인 요청 DTO")
public record AppleLoginRequest (
    @Schema(description = "애플 인가코드")
    String code
) {}
