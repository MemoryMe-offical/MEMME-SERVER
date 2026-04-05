package memme.memoryme.auth.api.dto.email;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class PwChangeResponseDTO {
    private String email;
    private String message;
}