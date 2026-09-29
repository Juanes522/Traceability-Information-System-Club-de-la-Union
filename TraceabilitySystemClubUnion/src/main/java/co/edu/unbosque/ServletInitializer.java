package co.edu.unbosque;

import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;

/**
 * Punto de entrada cuando la aplicación se despliega como archivo web en un contenedor externo.
 *
 * <p>Es el andamiaje estándar que permite a un contenedor de servlets arrancar la aplicación sin pasar por el método
 * principal. Dado que el empaquetado del proyecto es un archivo web con el servidor en alcance proporcionado, <strong>este es
 * el punto de entrada del despliegue que el proyecto parece tener como objetivo</strong>.
 *
 * <p><strong>Consecuencia que conviene conocer:</strong> esta ruta de arranque <strong>no registra el proveedor criptográfico
 * BouncyCastle</strong>, porque ese registro vive en el método principal de
 * {@link TraceabilitySystemClubUnionApplication}, que aquí nunca se invoca. Las notificaciones Web Push dependerían entonces
 * de los proveedores que ofrezca el contenedor.
 *
 * <p>Los ganchos de arranque basados en ejecutores de aplicación, como el relleno de resúmenes mensuales, sí se ejecutan con
 * normalidad en ambos modos.
 */
public class ServletInitializer extends SpringBootServletInitializer {

	@Override
	protected SpringApplicationBuilder configure(SpringApplicationBuilder application) {
		return application.sources(TraceabilitySystemClubUnionApplication.class);
	}

	
}
