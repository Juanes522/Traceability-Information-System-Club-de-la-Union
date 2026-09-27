/**
 * Restricciones de validación propias.
 *
 * <p>Contiene una sola restricción, {@link co.edu.unbosque.validation.StrongPassword}, con su
 * validador {@link co.edu.unbosque.validation.StrongPasswordValidator}.
 *
 * <p>Se aplica a los dos DTO que establecen una contraseña nueva
 * ({@link co.edu.unbosque.dto.ChangePasswordRequest} y
 * {@link co.edu.unbosque.dto.ResetPasswordRequest}), y deliberadamente <strong>no</strong> al DTO
 * de inicio de sesión: si se aplicara allí, las contraseñas heredadas que no cumplen la política
 * dejarían de poder usarse antes de que su titular tuviera ocasión de cambiarlas.
 *
 * <p>La misma regla está reimplementada en el frontend
 * ({@code core/validators/password.validator.ts}) con un patrón equivalente. Las dos definiciones
 * no derivan de una fuente común, de modo que pueden divergir sin que nada lo advierta.
 */
package co.edu.unbosque.validation;
