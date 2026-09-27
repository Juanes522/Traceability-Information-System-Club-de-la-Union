/**
 * Traducción de excepciones a respuestas HTTP.
 *
 * <p>El paquete contiene un único archivo,
 * {@link co.edu.unbosque.exception.GlobalExceptionHandler}, y <strong>no hay ninguna clase de
 * excepción propia en todo el proyecto</strong>.
 *
 * <p>Esa ausencia es coherente con el estilo del código: los controladores señalan los errores
 * devolviendo directamente {@code ResponseEntity.status(...)} en lugar de lanzar excepciones de
 * dominio, de modo que en la práctica basta con un manejador delgado. La contrapartida es que la
 * API expone cuatro formatos de error distintos y que varias excepciones de infraestructura llegan
 * al cliente como HTTP 500 sin diagnóstico.
 *
 * @see co.edu.unbosque.exception.GlobalExceptionHandler
 */
package co.edu.unbosque.exception;
