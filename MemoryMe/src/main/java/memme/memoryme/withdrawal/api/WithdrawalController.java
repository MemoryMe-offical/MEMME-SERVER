package memme.memoryme.withdrawal.api;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import memme.memoryme.global.util.jwt.CurrentUserProvider;
import memme.memoryme.global.util.response.ResponseWrapper;
import memme.memoryme.withdrawal.application.service.WithdrawalService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Withdrawal API", description = "회원 탈퇴 API")
@ApiResponses({
        @ApiResponse(responseCode = "204", description = "회원 탈퇴 성공"),
        @ApiResponse(responseCode = "401", description = "인증 실패")
})
@RestController
@RequestMapping("/v1/withdrawal")
@RequiredArgsConstructor
public class WithdrawalController {
    private final WithdrawalService withdrawalService;
    private final CurrentUserProvider currentUserProvider;

    @Operation(summary = "회원 탈퇴", description = "현재 로그인된 사용자의 모든 데이터를 삭제하고 계정을 탈퇴 처리합니다.")
    @DeleteMapping
    public ResponseEntity<ResponseWrapper<Void>> withdrawUser() {
        withdrawalService.withdrawUser(currentUserProvider.getUid());
        return ResponseEntity.ok(ResponseWrapper.ok(200, "회원 탈퇴 성공", null));
    }
}