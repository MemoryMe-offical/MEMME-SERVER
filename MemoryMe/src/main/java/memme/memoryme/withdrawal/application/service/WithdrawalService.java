package memme.memoryme.withdrawal.application.service;

import java.util.UUID;

public interface WithdrawalService {
    void withdrawUser(UUID userUid);
}