package memme.memoryme.search.application.service;

import memme.memoryme.board.domain.Board;
import memme.memoryme.memo.domain.Memo;
import memme.memoryme.note.domain.AttachmentStatus;
import memme.memoryme.note.domain.Note;
import memme.memoryme.note.domain.NoteAttachment;
import memme.memoryme.search.domain.SearchDocument;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Component
public class SearchDocumentMapper {
    public SearchDocument fromMemo(Memo memo) {
        return new SearchDocument(
                SearchDocument.TYPE_MEMO,
                memo.getUid().toString(),
                memo.getUserUid().toString(),
                null,
                null,
                null,
                null,
                blankToNull(memo.getText()),
                List.of(),
                List.of(),
                null,
                null,
                null,
                attachmentNames(memo.getAttachments()),
                memo.isBookmarked(),
                format(memo.getCreatedAt()),
                format(memo.getUpdatedAt())
        );
    }

    public List<SearchDocument> fromBoard(Board board) {
        List<SearchDocument> documents = new ArrayList<>();
        documents.add(boardDocument(board));
        board.getNotes().stream()
                .map(note -> fromNote(board, note))
                .forEach(documents::add);
        return documents;
    }

    private SearchDocument boardDocument(Board board) {
        return new SearchDocument(
                SearchDocument.TYPE_BOARD,
                board.getUid().toString(),
                board.getUserUid().toString(),
                null,
                null,
                null,
                blankToNull(board.getTitle()),
                blankToNull(board.getDescription()),
                List.copyOf(board.getTags()),
                List.of(),
                null,
                null,
                null,
                List.of(),
                board.isBookmarked(),
                format(board.getCreatedAt()),
                format(board.getUpdatedAt())
        );
    }

    private SearchDocument fromNote(Board board, Note note) {
        return new SearchDocument(
                SearchDocument.TYPE_NOTE,
                note.getUid().toString(),
                board.getUserUid().toString(),
                SearchDocument.TYPE_BOARD,
                board.getUid().toString(),
                blankToNull(board.getTitle()),
                blankToNull(note.getTitle()),
                blankToNull(note.getContent()),
                List.copyOf(board.getTags()),
                noteUrls(note),
                blankToNull(note.getOgTitle()),
                blankToNull(note.getOgDescription()),
                blankToNull(note.getOgSummary()),
                attachmentNames(note.getAttachments()),
                false,
                format(note.getCreatedAt()),
                format(note.getUpdatedAt())
        );
    }

    private List<String> noteUrls(Note note) {
        if (note.getUrls() != null && !note.getUrls().isEmpty()) {
            return note.getUrls();
        }
        String legacyUrl = blankToNull(note.getUrl());
        return legacyUrl == null ? List.of() : List.of(legacyUrl);
    }

    private List<String> attachmentNames(List<NoteAttachment> attachments) {
        if (attachments == null || attachments.isEmpty()) {
            return List.of();
        }
        return attachments.stream()
                .filter(attachment -> attachment.getStatus() == null || attachment.getStatus() == AttachmentStatus.ATTACHED)
                .map(this::attachmentName)
                .filter(name -> name != null && !name.isBlank())
                .toList();
    }

    private String attachmentName(NoteAttachment attachment) {
        String originalName = blankToNull(attachment.getOriginalName());
        if (originalName != null) {
            return originalName;
        }
        return blankToNull(attachment.getStoredName());
    }

    private String format(LocalDateTime dateTime) {
        return dateTime == null ? null : dateTime.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
    }

    private String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}
