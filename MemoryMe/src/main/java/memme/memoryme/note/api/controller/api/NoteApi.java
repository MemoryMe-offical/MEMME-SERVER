package memme.memoryme.note.api.controller.api;

import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.RequestMapping;

@Tag(name = "Note API", description = "메모 API")
@RequestMapping("/v1")
public interface NoteApi {
}
