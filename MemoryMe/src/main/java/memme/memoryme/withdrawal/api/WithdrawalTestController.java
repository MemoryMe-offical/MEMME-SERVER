package memme.memoryme.withdrawal.api;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import memme.memoryme.global.util.jwt.CurrentUserProvider;
import memme.memoryme.global.util.response.ResponseWrapper;
import memme.memoryme.withdrawal.application.service.UserDataDeletionService;
import org.springframework.context.annotation.Profile;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "[TEST] Withdrawal API", description = "회원 탈퇴 데이터 삭제 테스트용 (dev 환경 전용)")
@ApiResponses({
        @ApiResponse(responseCode = "200", description = "삭제 성공"),
        @ApiResponse(responseCode = "401", description = "인증 실패")
})
@RestController
@RequestMapping("/v1/test/withdrawal")
@RequiredArgsConstructor
@Profile({"dev", "local"})
public class WithdrawalTestController {

    private final UserDataDeletionService userDataDeletionService;
    private final CurrentUserProvider currentUserProvider;

    @Operation(
            summary = "[TEST] 현재 로그인 유저 데이터 전체 삭제",
            description = """
                    현재 JWT로 인증된 유저의 모든 데이터를 삭제합니다. (memo, board, pendinglink, ES 인덱스)
                    S3 파일은 트랜잭션 커밋 후 비동기로 삭제되며, 실패 시 pending_s3_delete 테이블에 남아 스케줄러가 재시도합니다.
                    **dev 환경 전용 — prod에서는 활성화되지 않습니다.**
                    """
    )
    @DeleteMapping("/my-data")
    public ResponseEntity<ResponseWrapper<Void>> deleteMyData() {
        userDataDeletionService.deleteUserData(currentUserProvider.getUid());
        return ResponseEntity.ok(ResponseWrapper.ok(200, "유저 데이터 삭제 성공 (S3는 비동기 처리)", null));
    }
}
