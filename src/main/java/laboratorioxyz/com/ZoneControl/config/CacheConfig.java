package laboratorioxyz.com.ZoneControl.config;

import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuración de caché para el módulo público.
 * Se usa ConcurrentMapCacheManager (caché en memoria local)
 * porque el contenido público cambia con poca frecuencia y no
 * justifica una solución distribuida como Redis.
 *
 * Cada cache name ("institutional", "contact", "offices", "catalog")
 * se asigna a un método @Cacheable específico en PublicServiceImpl
 * para evitar colisiones de tipos entre respuestas.
 */
@Configuration
@EnableCaching
public class CacheConfig {

    @Bean
    public CacheManager cacheManager() {
        return new ConcurrentMapCacheManager("institutional", "contact", "offices", "catalog");
    }
}
