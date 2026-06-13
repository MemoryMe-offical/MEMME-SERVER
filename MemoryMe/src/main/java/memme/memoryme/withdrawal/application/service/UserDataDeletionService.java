package memme.memoryme.withdrawal.application.service;

import java.util.UUID;

public interface UserDataDeletionService {
    /**
     * 회원 탈퇴 시 user 모듈에서 호출.
     * S3 키 수집 → pending_s3_delete 등록 → DB 데이터 전체 삭제를 단일 트랜잭션으로 처리.
     * 트랜잭션 커밋 후 S3 삭제가 비동기로 실행됨.
     */
    void deleteUserData(UUID userUid);
}
