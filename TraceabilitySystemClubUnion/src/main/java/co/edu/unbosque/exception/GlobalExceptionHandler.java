package co.edu.unbosque.exception;

import java.util.HashMap;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * Traduce los fallos de validación de cuerpo a respuestas HTTP 400.
 *
 * <p><strong>El nombre promete mucho más de lo que la clase entrega: no es global.</strong> Maneja <strong>una sola</strong>
 * excepción, la de validación de argumentos anotados, y devuelve un mapa plano de campo a mensaje.
 *
 * <p>Quedan <strong>sin manejador</strong>, y por tanto llegan al cliente como HTTP 500 sin diagnóstico:
 *
 * <ul>
 *   <li>Las violaciones de restricciones sobre parámetros de consulta o de ruta.</li>
 *   <li>El JSON malformado en el cuerpo de la petición.</li>
 *   <li>Las violaciones de integridad de la base, incluida la de unicidad de la identificación y el truncamiento de columna
 *       que previene el script de migración.</li>
 *   <li>Los fallos de los servicios de cifrado ante una clave incorrecta o un dato corrupto.</li>
 * </ul>
 *
 * <p>Que un manejador tan delgado baste en la práctica se explica por el estilo del resto del código: los controladores
 * señalan sus errores devolviendo directamente la respuesta con su código, en lugar de lanzar excepciones de dominio. De
 * hecho <strong>no existe ninguna clase de excepción propia en todo el proyecto</strong>.
 *
 * <p>La contrapartida es que la API expone cuatro formatos de error distintos: este mapa plano, los mensajes en objeto JSON
 * de las validaciones de rango, los cuerpos de texto plano del controlador de autenticación, y el formato por defecto del
 * framework, despojado de mensaje por configuración.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

	/**
	 * Traduce un fallo de validacion de cuerpo a una respuesta con el detalle por campo.
	 *
	 * <p>Construye un mapa plano de campo a mensaje. Conserva <strong>el primer mensaje de cada campo</strong>, de modo que un
	 * campo con varias restricciones incumplidas reporta solo una; y como el mapa no esta ordenado, el orden de las claves no
	 * es estable entre respuestas.
	 *
	 * <p>Solo atiende los errores de campo: las restricciones declaradas a nivel de clase se descartan en silencio.
	 *
	 * @param ex excepcion de validacion que lanza el framework al fallar la comprobacion del cuerpo
	 * @return respuesta 400 con un objeto JSON de campo a mensaje
	 */
	@ExceptionHandler(MethodArgumentNotValidException.class)
	public ResponseEntity<Map<String, String>> handleValidation(MethodArgumentNotValidException ex) {
		Map<String, String> errors = new HashMap<>();
		for (FieldError error : ex.getBindingResult().getFieldErrors()) {
			errors.putIfAbsent(error.getField(), error.getDefaultMessage());
		}
		return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errors);
	}
}
