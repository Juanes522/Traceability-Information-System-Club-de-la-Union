package co.edu.unbosque.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
/**
 * Configura el esquema de seguridad de la documentación OpenAPI.
 *
 * <p>Declara el esquema {@code bearerAuth} de tipo HTTP con formato JWT y lo aplica globalmente, de modo que Swagger UI
 * ofrece un campo para pegar el token y lo envía en todas las pruebas interactivas. Sin esto, la interfaz solo permitiría
 * probar los endpoints públicos.
 *
 * <p>No declara bloque de información: el documento generado <strong>carece de título, versión, descripción, contacto y
 * licencia</strong>, y Swagger UI muestra los valores por defecto de la biblioteca.
 *
 * <p>Conviene recordar que la documentación queda <strong>públicamente accesible en cualquier entorno</strong>, porque la
 * configuración de seguridad la declara abierta sin guarda de perfil: toda la superficie de la API y todos los esquemas de
 * DTO son legibles sin autenticarse.
 */
public class SwaggerConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        final String securitySchemeName = "bearerAuth";
        return new OpenAPI()
                .addSecurityItem(new SecurityRequirement().addList(securitySchemeName))
                .components(
                    new Components()
                        .addSecuritySchemes(securitySchemeName,
                            new SecurityScheme()
                                .name(securitySchemeName)
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")
                        )
                );
    }
}
