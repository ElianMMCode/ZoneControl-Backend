package laboratorioxyz.com.ZoneControl.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI zoneControlOpenAPI() {
        var securitySchemeName = "bearer-jwt";
        return new OpenAPI()
                .info(new Info()
                        .title("ZoneControl API")
                        .description("API del sistema de control de acceso físico Laboratorio XYZ. " +
                                "Gestiona empleados, usuarios internos, permisos, " +
                                "validación de credenciales y reportes de auditoría.")
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("Laboratorio XYZ")
                                .email("soporte@laboratorioxzy.com.co")
                                .url("https://laboratorioxzy.com.co"))
                        .license(new License()
                                .name("Propietario")
                                .url("https://laboratorioxzy.com.co/terminos")))
                .servers(List.of(
                        new Server().url("http://localhost:8080").description("Local"),
                        new Server().url("https://api.laboratorioxzy.com.co").description("Producción")
                ))
                .addSecurityItem(new SecurityRequirement().addList(securitySchemeName))
                .components(new Components()
                        .addSecuritySchemes(securitySchemeName, new SecurityScheme()
                                .name(securitySchemeName)
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")
                                .description("Token JWT obtenido de POST /api/auth/login. " +
                                        "Incluir como: Authorization: Bearer {token}")));
    }
}
