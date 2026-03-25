package memme.memoryme.auth.api.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class VerifyEmailRequestDTO {
    private String email;
    private String code;
}
