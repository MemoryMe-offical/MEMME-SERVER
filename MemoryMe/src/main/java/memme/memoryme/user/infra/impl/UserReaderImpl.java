package memme.memoryme.user.infra.impl;

import lombok.RequiredArgsConstructor;
import memme.memoryme.memo.application.port.UserReader;
import memme.memoryme.user.infra.UserRepository;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class UserReaderImpl implements UserReader {
    private final UserRepository userRepository;

    @Override
    public boolean existsByUid(UUID uid) {
        return userRepository.existsByUid(uid);
    }
}
