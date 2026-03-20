package memme.memoryme.global.docs.response;

import io.swagger.v3.oas.annotations.media.Schema;
import memme.memoryme.global.util.response.ResponseWrapper;
import memme.memoryme.note.api.dto.note.NoteDto;

import java.time.LocalDateTime;

// Swagger에 표시할 반환 스키마
@Schema(
        name = "NoteResponse",
        description = "메모 단건 응답",
        example = """
                {
                  "success": true,
                  "status": 200,
                  "message": "메모 생성 성공",
                  "timestamp": "2026-03-20T16:10:00",
                  "data": {
                    "uid": "550e8400-e29b-41d4-a716-446655440000",
                    "title": "오늘 회의 정리",
                    "post": {
                      "content": "오늘은 회의 내용을 정리하고 다음 작업 일정을 정했다.",
                      "images": [
                        "https://cdn.example.com/images/note-1.png"
                      ],
                      "files": [
                        "https://cdn.example.com/files/meeting-notes.pdf"
                      ]
                    },
                    "created": "2026-03-20T14:30:00",
                    "updated": "2026-03-20T15:10:00"
                  }
                }
                """
)
public class NoteResponse extends ResponseWrapper<NoteDto> {

    public NoteResponse(boolean success, int status, String message, LocalDateTime timestamp, NoteDto data) {
        super(success, status, message, timestamp, data);
    }
}