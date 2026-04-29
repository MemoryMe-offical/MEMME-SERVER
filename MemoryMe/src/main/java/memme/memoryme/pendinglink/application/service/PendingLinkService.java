package memme.memoryme.pendinglink.application.service;

import memme.memoryme.pendinglink.api.dto.CreatePendingLinkRequest;
import memme.memoryme.pendinglink.api.dto.CreatePendingLinkResponse;
import memme.memoryme.pendinglink.api.dto.PendingLinkListResponse;

import java.util.UUID;

public interface PendingLinkService {
    CreatePendingLinkResponse create(CreatePendingLinkRequest request);
    PendingLinkListResponse getPendingLinks();
    void delete(UUID pendingLinkUid);
}
