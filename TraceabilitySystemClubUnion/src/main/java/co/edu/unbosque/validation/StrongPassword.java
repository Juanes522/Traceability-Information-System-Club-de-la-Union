package co.edu.unbosque.validation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

@Documented
@Target({ ElementType.FIELD, ElementType.PARAMETER })
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = StrongPasswordValidator.class)
public @interface StrongPassword {
	String message() default "La contraseña debe tener al menos 12 caracteres e incluir minúscula, mayúscula y dígito.";
	Class<?>[] groups() default {};
	Class<? extends Payload>[] payload() default {};
}
