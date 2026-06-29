package memme.memoryme.withdrawal.application.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import memme.memoryme.global.exception.BusinessException;
import memme.memoryme.user.exception.UserErrorCode;
import memme.memoryme.user.infra.UserRepository;
import memme.memoryme.withdrawal.application.service.UserDataDeletionService;
import memme.memoryme.withdrawal.application.service.WithdrawalService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import memme.memoryme.user.domain.UserEntity;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class WithdrawalServiceImpl implements WithdrawalService {
    private final UserDataDeletionService userDataDeletionService;
    private final UserRepository userRepository;

    @Override
    @Transactional
    public void withdrawUser(UUID userUid) {
        UserEntity user = userRepository.findByUid(userUid)
                .orElseThrow(() -> new BusinessException(UserErrorCode.USER_NOT_FOUND));
        userDataDeletionService.deleteUserData(userUid);
        userRepository.delete(user);
    }
}