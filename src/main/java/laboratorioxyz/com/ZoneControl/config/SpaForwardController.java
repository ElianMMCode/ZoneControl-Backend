package laboratorioxyz.com.ZoneControl.config;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * Fallback para el SPA (React en src/main/frontend, build en src/main/resources/static).
 * Reenvía las rutas del frontend a index.html para que react-router maneje
 * la navegación y los refrescos en rutas anidadas (p.ej. /supervisor/validar).
 * Excluye /api/** y /assets/** (recursos reales servidos por Spring).
 */
@Controller
public class SpaForwardController {

    @GetMapping(value = {
            "/",
            "/login",
            "/configurar-contrasena",
            "/ajustes",
            "/personal/**",
            "/permisos",
            "/supervisor/**",
            "/admin/**",
    })
    public String forwardToIndex() {
        return "forward:/index.html";
    }
}
