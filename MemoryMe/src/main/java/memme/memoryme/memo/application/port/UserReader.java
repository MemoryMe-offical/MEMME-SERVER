package memme.memoryme.memo.application.port;

import java.util.UUID;

public interface UserReader {
    boolean existsByUid(UUID uid);
}
