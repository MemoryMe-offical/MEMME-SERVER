package memme.memoryme.pendinglink.api.controller;

import lombok.RequiredArgsConstructor;
import memme.memoryme.global.util.response.ResponseWrapper;
import memme.memoryme.pendinglink.api.controller.api.PendingLinkApi;
import memme.memoryme.pendinglink.api.dto.CreatePendingLinkRequest;
import memme.memoryme.pendinglink.api.dto.CreatePendingLinkResponse;
import memme.memoryme.pendinglink.api.dto.PendingLinkListResponse;
import memme.memoryme.pendinglink.application.service.PendingLinkService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
public class PendingLinkController implements PendingLinkApi {
    private final PendingLinkService pendingLinkService;

    @Override
    public ResponseEntity<ResponseWrapper<CreatePendingLinkResponse>> create(CreatePendingLinkRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(
                ResponseWrapper.ok(201, "공유 링크 임시 저장 성공", pendingLinkService.create(request))
        );
    }

    @Override
    public ResponseEntity<ResponseWrapper<PendingLinkListResponse>> getPendingLinks() {
        return ResponseEntity.ok(ResponseWrapper.ok(200, "공유 링크 임시 목록 조회 성공", pendingLinkService.getPendingLinks()));
    }

    @Override
    public ResponseEntity<ResponseWrapper<Void>> delete(UUID pendingLinkUid) {
        pendingLinkService.delete(pendingLinkUid);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).body(ResponseWrapper.ok(204, "공유 링크 임시 항목 삭제 성공", null));
    }
}
