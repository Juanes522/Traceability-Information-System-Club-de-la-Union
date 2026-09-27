package co.edu.unbosque.validation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

/**
 * Exige que una contraseña cumpla la política de robustez del sistema.
 *
 * <p>Se aplica a {@link co.edu.unbosque.dto.ChangePasswordRequest} y
 * {@link co.edu.unbosque.dto.ResetPasswordRequest}, es decir a los dos puntos donde se <em>establece</em>
 * una contraseña nueva.
 *
 * <p><strong>Deliberadamente no se aplica al inicio de sesión.</strong> Si se hiciera, las contraseñas
 * heredadas que no cumplen la política dejarían de poder usarse antes de que su titular tuviera ocasión de
 * cambiarlas, dejándolo fuera del sistema. Quien deba cambiarla se ve obligado por el indicador
 * {@code forcePasswordChange} de su ficha, no por esta restricción.
 *
 * <p>Las reglas concretas están documentadas en {@link StrongPasswordValidator}.
 *
 * <p>Limitaciones de la declaración: solo puede colocarse en campos y parámetros —no en métodos ni como
 * anotación compuesta—, no es repetible, y su mensaje está escrito en español directamente en el código, sin
 * agrupación de mensajes para internacionalización.
 */
@Documented
@Target({ ElementType.FIELD, ElementType.PARAMETER })
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = StrongPasswordValidator.class)
public @interface StrongPassword {
	String message() default "La contraseña debe tener al menos 12 caracteres e incluir minúscula, mayúscula y dígito.";
	Class<?>[] groups() default {};
	Class<? extends Payload>[] payload() default {};
}
