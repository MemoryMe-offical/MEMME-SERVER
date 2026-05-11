package memme.memoryme.note.application.service;

import lombok.RequiredArgsConstructor;
import memme.memoryme.global.exception.BusinessException;
import memme.memoryme.global.util.jwt.CurrentUserProvider;
import memme.memoryme.note.api.dto.AttachmentDto;
import memme.memoryme.note.api.dto.AttachmentListResponse;
import memme.memoryme.note.domain.AttachmentStatus;
import memme.memoryme.note.domain.AttachmentType;
import memme.memoryme.note.domain.Note;
import memme.memoryme.note.domain.NoteAttachment;
import memme.memoryme.note.exception.AttachmentErrorCode;
import memme.memoryme.note.infra.repository.NoteAttachmentRepository;
import memme.memoryme.upload.application.service.UploadService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Locale;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AttachmentServiceImpl implements AttachmentService {
    private final NoteAttachmentRepository noteAttachmentRepository;
    private final CurrentUserProvider currentUserProvider;
    private final UploadService uploadService;

    @Override
    @Transactional(readOnly = true)
    public AttachmentListResponse getAttachments(String type, Integer page, Integer limit) {
        UUID userUid = currentUserProvider.getUid();
        int normalizedPage = page == null || page < 1 ? 1 : page;
        int normalizedLimit = limit == null || limit < 1 ? 50 : Math.min(limit, 100);
        AttachmentType attachmentType = parseType(type);

        Page<NoteAttachment> attachments = noteAttachmentRepository.findPage(
                userUid,
                attachmentType,
                AttachmentStatus.ATTACHED,
                PageRequest.of(normalizedPage - 1, normalizedLimit, Sort.by(Sort.Direction.DESC, "createdAt"))
        );

        return new AttachmentListResponse(
                attachments.getContent().stream().map(AttachmentDto::from).toList(),
                attachments.getTotalElements(),
                normalizedPage,
                normalizedLimit
        );
    }

    @Override
    @Transactional(readOnly = true)
    public AttachmentDto getAttachment(UUID attachmentUid) {
        return AttachmentDto.from(getCurrentUserAttachment(attachmentUid));
    }

    @Override
    @Transactional
    public void deleteAttachment(UUID attachmentUid) {
        NoteAttachment attachment = getCurrentUserAttachment(attachmentUid);
        String key = attachment.getS3Key();

        Note note = attachment.getNote();
        if (note != null) {
            note.removeAttachment(attachment);
        } else {
            noteAttachmentRepository.delete(attachment);
        }

        if (key != null && !key.isBlank()) {
            uploadService.deleteObject(key);
        }
    }

    private NoteAttachment getCurrentUserAttachment(UUID attachmentUid) {
        if (attachmentUid == null) {
            throw new BusinessException(AttachmentErrorCode.INVALID_ATTACHMENT_REQUEST);
        }
        UUID userUid = currentUserProvider.getUid();
        return noteAttachmentRepository.findByUidAndUserUidAndStatus(attachmentUid, userUid, AttachmentStatus.ATTACHED)
                .orElseThrow(() -> new BusinessException(AttachmentErrorCode.ATTACHMENT_NOT_FOUND));
    }

    private AttachmentType parseType(String type) {
        if (type == null || type.isBlank()) {
            return null;
        }
        try {
            return AttachmentType.valueOf(type.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException e) {
            throw new BusinessException(AttachmentErrorCode.INVALID_ATTACHMENT_REQUEST);
        }
    }
}
