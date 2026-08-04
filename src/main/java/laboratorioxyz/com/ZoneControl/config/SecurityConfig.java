package laboratorioxyz.com.ZoneControl.config;

import laboratorioxyz.com.ZoneControl.security.JwtAuthenticationFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.http.HttpMethod;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    @Bean
    public BCryptPasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) throws Exception {
        return authConfig.getAuthenticationManager();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/error").permitAll()
                .requestMatchers(HttpMethod.POST, "/api/auth/change-password").authenticated()
                .requestMatchers("/api/public/**", "/api/auth/**", "/api/setup-password/**",
                        "/swagger-ui.html", "/swagger-ui/**", "/webjars/**",
                        "/v3/api-docs/**", "/v3/api-docs.yaml").permitAll()
                // SPA (React): build en src/main/resources/static servido por Spring.
                // Solo GET; los datos siguen protegidos bajo /api/**.
                .requestMatchers(HttpMethod.GET, "/", "/index.html", "/assets/**",
                        "/favicon.ico", "/favicon.svg", "/vite.svg", "/*.svg",
                        "/login", "/configurar-contrasena", "/ajustes",
                        "/personal/**", "/permisos",
                        "/supervisor/**", "/admin/**").permitAll()
                .requestMatchers("/api/admin/**").hasRole("ADMIN")
                .requestMatchers("/api/personal/**", "/api/permisos/**")
                    .hasAnyRole("ADMIN", "GESTOR_PERSONAL")
                .requestMatchers("/api/access/**")
                    .hasAnyRole("ADMIN", "SUPERVISOR_AUDITOR")
                .requestMatchers("/api/historial/**", "/api/reportes/**")
                    .hasAnyRole("ADMIN", "SUPERVISOR_AUDITOR")
                .anyRequest().authenticated()
            )
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }
}
