package memme.memoryme.global.util.apple;

import java.util.List;

public record ApplePublicKeyResponse(
        List<ApplePublicKey> keys
) {
}
