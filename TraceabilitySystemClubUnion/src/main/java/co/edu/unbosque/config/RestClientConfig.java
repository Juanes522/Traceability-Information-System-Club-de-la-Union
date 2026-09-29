package co.edu.unbosque.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

/**
 * Expone el constructor de cliente HTTP que usa la sincronización de socios.
 *
 * <p><strong>Consideración importante:</strong> Spring Boot ya autoconfigura un constructor de cliente HTTP con conversores
 * de mensajes, observabilidad y ajustes de fábrica de peticiones. Este bean lo <strong>sustituye por uno desnudo</strong>,
 * de modo que su único consumidor —la llamada al maestro externo de socios— queda sin tiempo de espera de conexión ni de
 * lectura.
 *
 * <p>La consecuencia práctica es que si el servicio externo acepta la conexión y no responde, el hilo de la petición queda
 * bloqueado indefinidamente. Configurar los tiempos de espera aquí, o eliminar el bean y dejar actuar la autoconfiguración,
 * resolvería ese riesgo.
 *
 * @see co.edu.unbosque.service.PartnerSyncService
 */
@Configuration
public class RestClientConfig {

	/**
	 * Expone el constructor de cliente HTTP.
	 *
	 * <p>Devuelve un constructor <strong>sin configurar</strong>, que sustituye al que Spring Boot autoconfigura. Su unico
	 * consumidor, la sincronizacion de socios, queda por tanto sin tiempo de espera de conexion ni de lectura.
	 *
	 * @return el constructor de cliente HTTP que se inyectara donde se requiera
	 */
	@Bean
	public RestClient.Builder restClientBuilder() {
		return RestClient.builder();
	}
}
