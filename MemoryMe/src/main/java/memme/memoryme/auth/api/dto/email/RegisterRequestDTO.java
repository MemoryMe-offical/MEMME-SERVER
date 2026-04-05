package memme.memoryme.auth.api.dto.email;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RegisterRequestDTO {
    private String email;
    private String password;
}
