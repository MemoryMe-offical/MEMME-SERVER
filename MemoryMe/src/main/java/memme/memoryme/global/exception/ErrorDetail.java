package memme.memoryme.global.exception;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
@Schema(description = "에러 상세 정보")
public class ErrorDetail {

    @Schema(description = "에러 코드", example = "AUTH_001")
    private String code;

    @Schema(description = "에러 상세 메시지", example = "유효하지 않은 사용자 UID 형식입니다.")
    private String detail;
}
