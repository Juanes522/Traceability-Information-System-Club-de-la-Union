package co.edu.unbosque.validation;

import java.util.regex.Pattern;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

/**
 * Comprueba la política de contraseñas mediante una única expresión regular.
 *
 * <h2>Reglas exactas</h2>
 *
 * <ol>
 *   <li>Al menos una minúscula ASCII</li>
 *   <li>Al menos una mayúscula ASCII</li>
 *   <li>Al menos un dígito</li>
 *   <li>Longitud entre 12 y 100 caracteres</li>
 * </ol>
 *
 * <p>Y lo que <strong>no</strong> exige, que conviene saber para no atribuirle garantías que no da: no
 * requiere símbolos, no prohíbe espacios, no consulta listas de contraseñas filtradas ni diccionarios, no
 * compara contra el nombre de usuario y no guarda historial.
 *
 * <h2>Dos consecuencias poco evidentes</h2>
 *
 * <p><strong>Las clases de caracteres son ASCII.</strong> Al no activarse {@code UNICODE_CHARACTER_CLASS},
 * las letras acentuadas cuentan únicamente para la longitud: {@code "ÁÉÍÓÚáéíóú12"} <strong>no pasa</strong>
 * los requisitos de mayúscula y minúscula a pesar de mezclarlas a la vista. Es un detalle relevante en un
 * sistema en español, donde un usuario puede elegir una contraseña con tildes y recibir un rechazo que le
 * parecerá inexplicable.
 *
 * <p><strong>El tope de 100 caracteres es arbitrario, no protector.</strong> BCrypt trunca en 72 bytes, de
 * modo que el techo real de entropía está antes de ese límite.
 *
 * <p>Sin {@code DOTALL}, el punto no abarca saltos de línea: una contraseña que contenga {@code \n} o
 * {@code \r} se rechaza siempre.
 *
 * <h2>Duplicación con el cliente</h2>
 *
 * <p>La misma regla está reimplementada en el frontend, en {@code core/validators/password.validator.ts},
 * con un patrón equivalente. <strong>No derivan de una fuente común</strong>, así que pueden divergir sin
 * que nada lo advierta, y entonces la interfaz y la API discreparían sobre qué contraseña es aceptable.
 */
public class StrongPasswordValidator implements ConstraintValidator<StrongPassword, String> {

	private static final Pattern PATTERN = Pattern.compile("^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d).{12,100}$");

	/**
	 * Evalúa la contraseña contra el patrón.
	 *
	 * <p>Devuelve {@code true} para {@code null}, siguiendo la convención de Bean Validation: la
	 * obligatoriedad es responsabilidad de otra restricción. Por eso ambos DTO que usan
	 * {@link StrongPassword} lo combinan con {@code @NotBlank}; usarlo solo permitiría enviar una contraseña
	 * nula sin que nada la rechace.
	 *
	 * @param value contraseña propuesta
	 * @param context contexto de validación; no se usa, de modo que el mensaje es siempre el de la anotación
	 * @return {@code true} si cumple la política o si es {@code null}
	 */
	@Override
	public boolean isValid(String value, ConstraintValidatorContext context) {
		if (value == null) {
			return true;
		}
		return PATTERN.matcher(value).matches();
	}
}
