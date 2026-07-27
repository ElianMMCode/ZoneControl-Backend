package laboratorioxyz.com.ZoneControl.modulo_publico.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.util.Map;

@Data
@AllArgsConstructor
@Builder
public class ContactResponse {
    private Map<String, String> contact;
}
