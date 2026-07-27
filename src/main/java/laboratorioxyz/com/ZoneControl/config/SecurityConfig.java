package laboratorioxyz.com.ZoneControl.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Configuración base de seguridad.
 * Expone el bean BCryptPasswordEncoder para encriptar contraseñas
 * (usado en DataInitializer y futuro módulo de autenticación).
 *
 * Actualmente permite todas las rutas sin autenticación mientras se
 * construye la lógica de negocio. La seguridad JWT con roles se
 * implementará en la Fase 7 (HU-03) como capa transversal.
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public BCryptPasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .authorizeHttpRequests(auth -> auth
                .anyRequest().permitAll()
            );
        return http.build();
    }
}
