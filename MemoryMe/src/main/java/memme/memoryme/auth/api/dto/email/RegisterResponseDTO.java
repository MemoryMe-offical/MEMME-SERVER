package memme.memoryme.auth.api.dto.email;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "회원가입 응답 DTO")
public record RegisterResponseDTO(
        @Schema(description = "이메일", example = "test@example.com")
        String email,
    
        @Schema(description = "사용자 이름", example = "가나다")
        String userName
){
}