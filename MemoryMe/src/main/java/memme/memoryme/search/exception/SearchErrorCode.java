package memme.memoryme.search.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import memme.memoryme.global.exception.BaseErrorCode;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum SearchErrorCode implements BaseErrorCode {
    SEARCH_DISABLED(HttpStatus.SERVICE_UNAVAILABLE, "SEARCH_001", "검색 기능이 비활성화되어 있습니다."),
    SEARCH_INDEX_FAILED(HttpStatus.SERVICE_UNAVAILABLE, "SEARCH_002", "검색 색인 처리 중 오류가 발생했습니다."),
    SEARCH_QUERY_FAILED(HttpStatus.SERVICE_UNAVAILABLE, "SEARCH_003", "검색 조회 중 오류가 발생했습니다."),
    INVALID_SEARCH_REQUEST(HttpStatus.BAD_REQUEST, "SEARCH_004", "유효하지 않은 검색 요청입니다.");

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;
}
