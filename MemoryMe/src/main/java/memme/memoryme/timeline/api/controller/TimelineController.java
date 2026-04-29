package memme.memoryme.timeline.api.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import memme.memoryme.global.util.response.ResponseWrapper;
import memme.memoryme.timeline.api.dto.TimelineResponse;
import memme.memoryme.timeline.application.service.TimelineService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@Tag(name = "Timeline API", description = "메모와 보드를 혼합 조회하는 타임라인 API")
@RestController
@RequestMapping("/v1/timeline")
@RequiredArgsConstructor
public class TimelineController {
    private final TimelineService timelineService;

    @Operation(summary = "타임라인 목록 조회")
    @GetMapping
    public ResponseEntity<ResponseWrapper<TimelineResponse>> getTimeline(
            @RequestParam(required = false) String type,
            @RequestParam(required = false) String tags,
            @RequestParam(required = false) String q,
            @RequestParam(required = false, defaultValue = "createdAt") String sort,
            @RequestParam(required = false, defaultValue = "1") Integer page,
            @RequestParam(required = false, defaultValue = "50") Integer limit,
            @RequestParam(required = false) UUID excludeId
    ) {
        return ResponseEntity.ok(ResponseWrapper.ok(
                200,
                "타임라인 조회 성공",
                timelineService.getTimeline(type, tags, q, sort, page, limit, excludeId)
        ));
    }
}
