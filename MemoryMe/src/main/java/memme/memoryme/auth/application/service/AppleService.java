package memme.memoryme.auth.application.service;

import memme.memoryme.auth.api.dto.apple.AppleLoginRequest;
import memme.memoryme.auth.api.dto.email.LoginResponseDTO;

public interface AppleService {
    LoginResponseDTO login(AppleLoginRequest request);

}
