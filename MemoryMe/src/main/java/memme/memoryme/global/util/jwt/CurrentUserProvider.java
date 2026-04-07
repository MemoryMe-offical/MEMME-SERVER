package memme.memoryme.global.util.jwt;

import memme.memoryme.global.exception.CommonErrorCode;
import memme.memoryme.global.exception.BusinessException;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class CurrentUserProvider {
    public UUID getUid() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null
                || !authentication.isAuthenticated()
                || authentication instanceof AnonymousAuthenticationToken) {
            throw new BusinessException(CommonErrorCode.UNAUTHORIZED);
        }

        Object principal = authentication.getPrincipal();

        if (!(principal instanceof String uid) || uid.isBlank()) {
            throw new BusinessException(CommonErrorCode.INVALID_USER_UID);
        }

        try {
            return UUID.fromString(uid);
        } catch (IllegalArgumentException e) {
            throw new BusinessException(CommonErrorCode.INVALID_USER_UID);
        }
    }
}
