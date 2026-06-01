package memme.memoryme.search.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;

import java.util.List;
import java.util.Objects;

public record SearchDocument(
        String type,
        String uid,
        String userUid,
        String parentType,
        String parentUid,
        String parentTitle,
        String title,
        String content,
        List<String> tags,
        List<String> urls,
        String ogTitle,
        String ogDescription,
        String ogSummary,
        List<String> attachmentNames,
        boolean bookmarked,
        String createdAt,
        String updatedAt
) {
    public static final String TYPE_MEMO = "memo";
    public static final String TYPE_BOARD = "board";
    public static final String TYPE_NOTE = "note";

    public SearchDocument {
        tags = compact(tags);
        urls = compact(urls);
        attachmentNames = compact(attachmentNames);
    }

    @JsonIgnore
    public String documentId() {
        return type + ":" + uid;
    }

    private static List<String> compact(List<String> values) {
        if (values == null || values.isEmpty()) {
            return List.of();
        }
        return values.stream()
                .filter(Objects::nonNull)
                .map(String::trim)
                .filter(value -> !value.isBlank())
                .distinct()
                .toList();
    }
}
