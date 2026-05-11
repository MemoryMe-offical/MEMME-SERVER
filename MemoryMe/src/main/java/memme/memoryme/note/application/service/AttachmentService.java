package memme.memoryme.note.application.service;

import memme.memoryme.note.api.dto.AttachmentDto;
import memme.memoryme.note.api.dto.AttachmentListResponse;

import java.util.UUID;

public interface AttachmentService {
    AttachmentListResponse getAttachments(String type, Integer page, Integer limit);
    AttachmentDto getAttachment(UUID attachmentUid);
    void deleteAttachment(UUID attachmentUid);
}
