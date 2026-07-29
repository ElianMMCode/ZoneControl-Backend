package laboratorioxyz.com.ZoneControl.modulo_autenticacion.dto;

import java.util.UUID;

public record LoginResponse(String token, Usuario usuario) {
    public record Usuario(UUID id, String nombre, String email, String rol) {}
}
