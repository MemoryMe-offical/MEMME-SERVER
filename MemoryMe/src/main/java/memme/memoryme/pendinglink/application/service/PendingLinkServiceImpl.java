package memme.memoryme.pendinglink.application.service;

import lombok.RequiredArgsConstructor;
import memme.memoryme.global.exception.BusinessException;
import memme.memoryme.global.util.jwt.CurrentUserProvider;
import memme.memoryme.note.api.dto.OgDataDto;
import memme.memoryme.og.application.service.OgService;
import memme.memoryme.pendinglink.api.dto.CreatePendingLinkRequest;
import memme.memoryme.pendinglink.api.dto.CreatePendingLinkResponse;
import memme.memoryme.pendinglink.api.dto.PendingLinkDto;
import memme.memoryme.pendinglink.api.dto.PendingLinkListResponse;
import memme.memoryme.pendinglink.domain.PendingLink;
import memme.memoryme.pendinglink.exception.PendingLinkErrorCode;
import memme.memoryme.pendinglink.infra.repository.PendingLinkRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.net.URI;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PendingLinkServiceImpl implements PendingLinkService {
    private final PendingLinkRepository pendingLinkRepository;
    private final CurrentUserProvider currentUserProvider;
    private final OgService ogService;

    @Override
    @Transactional
    public CreatePendingLinkResponse create(CreatePendingLinkRequest request) {
        validateUrl(request.url());
        UUID userUid = currentUserProvider.getUid();
        OgDataDto ogData = ogService.fetch(request.url());

        PendingLink pendingLink = PendingLink.builder()
                .uid(UUID.randomUUID())
                .userUid(userUid)
                .url(request.url().trim())
                .ogTitle(ogData != null ? ogData.title() : null)
                .ogDescription(ogData != null ? ogData.description() : null)
                .ogImageUrl(ogData != null ? ogData.imageUrl() : null)
                .ogSiteName(ogData != null ? ogData.siteName() : null)
                .build();

        return new CreatePendingLinkResponse(PendingLinkDto.from(pendingLinkRepository.save(pendingLink)));
    }

    @Override
    @Transactional(readOnly = true)
    public PendingLinkListResponse getPendingLinks() {
        UUID userUid = currentUserProvider.getUid();
        return new PendingLinkListResponse(
                pendingLinkRepository.findAllByUserUidOrderByReceivedAtDesc(userUid).stream()
                        .map(PendingLinkDto::from)
                        .toList(),
                pendingLinkRepository.countByUserUid(userUid)
        );
    }

    @Override
    @Transactional
    public void delete(UUID pendingLinkUid) {
        UUID userUid = currentUserProvider.getUid();
        if (!pendingLinkRepository.existsByUidAndUserUid(pendingLinkUid, userUid)) {
            throw new BusinessException(PendingLinkErrorCode.PENDING_LINK_NOT_FOUND);
        }
        pendingLinkRepository.deleteByUidAndUserUid(pendingLinkUid, userUid);
    }

    private void validateUrl(String url) {
        if (url == null || url.isBlank() || url.length() > 2048) {
            throw new BusinessException(PendingLinkErrorCode.INVALID_PENDING_LINK_REQUEST);
        }
        try {
            URI uri = URI.create(url.trim());
            if (!("http".equalsIgnoreCase(uri.getScheme()) || "https".equalsIgnoreCase(uri.getScheme()))) {
                throw new BusinessException(PendingLinkErrorCode.INVALID_PENDING_LINK_REQUEST);
            }
        } catch (IllegalArgumentException e) {
            throw new BusinessException(PendingLinkErrorCode.INVALID_PENDING_LINK_REQUEST);
        }
    }
}
