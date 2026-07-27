package laboratorioxyz.com.ZoneControl;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Punto de entrada de la aplicación ZoneControl.
 * Levanta Spring Boot con todos los módulos:
 * público, autenticación, administración, gestión de personal,
 * control de acceso físico y reportes de auditoría.
 */
@SpringBootApplication
public class ZoneControlApplication {

	public static void main(String[] args) {
		SpringApplication.run(ZoneControlApplication.class, args);
	}

}
