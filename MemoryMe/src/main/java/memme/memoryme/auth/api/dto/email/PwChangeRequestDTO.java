package memme.memoryme.auth.api.dto.email;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PwChangeRequestDTO {
    private String currentPassword;
    private String newPassword;
}