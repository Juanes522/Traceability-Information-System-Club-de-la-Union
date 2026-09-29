package co.edu.unbosque;

import java.security.Security;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Punto de entrada de la aplicación cuando se ejecuta con servidor embebido.
 *
 * <p>Las tres anotaciones que acompañan a la clase están todas en uso real, y conviene saber quién depende de cada una:
 *
 * <ul>
 *   <li>La habilitación de ejecución asíncrona sostiene el registro de auditoría y el correo de aviso de consumo, que no
 *       deben bloquear la operación que los origina.</li>
 *   <li>La habilitación de tareas programadas sostiene tres trabajos: el cierre diario de visitas abiertas, el resumen
 *       mensual y la depuración horaria de los tokens revocados.</li>
 * </ul>
 *
 * <p><strong>Advertencia sobre los dos modos de despliegue.</strong> El artefacto es un archivo web con el servidor en
 * alcance proporcionado, de modo que admite dos puntos de entrada: este método principal con servidor embebido, o
 * {@link ServletInitializer} desplegado en un contenedor externo.
 *
 * <p>El registro del proveedor criptográfico BouncyCastle ocurre <strong>únicamente en este método</strong>. En un despliegue
 * sobre contenedor externo —que es el que sugiere el empaquetado elegido— este método <strong>no se ejecuta</strong> y el
 * proveedor no queda registrado, con lo que la firma de las notificaciones Web Push pasaría a depender de lo que ofrezca el
 * contenedor. Es una asimetría real entre ambos modos.
 */
@SpringBootApplication
@EnableAsync
@EnableScheduling
public class TraceabilitySystemClubUnionApplication {

	/**
	 * Arranca la aplicacion con servidor embebido.
	 *
	 * <p>Registra el proveedor criptografico BouncyCastle <strong>antes</strong> de levantar el contexto, porque la firma de
	 * las notificaciones Web Push lo necesita. Ese registro ocurre unicamente aqui: en un despliegue como archivo web sobre un
	 * contenedor externo este metodo no se ejecuta y el proveedor no queda disponible.
	 *
	 * @param args argumentos de linea de comandos, que se trasladan al contexto de Spring
	 */
	public static void main(String[] args) {
		Security.addProvider(new BouncyCastleProvider());
		SpringApplication.run(TraceabilitySystemClubUnionApplication.class, args);
	}

}
