package memme.memoryme.auth.api.dto.email;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class RegisterResponseDTO {
    private String message;
    private String email;
    private String userName;
}