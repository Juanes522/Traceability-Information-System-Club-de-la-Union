package co.edu.unbosque;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
/**
 * Verifica que el contexto de Spring arranca por completo.
 *
 * <p>Es la prueba de humo del cableado: detecta beans que no se pueden construir, dependencias circulares y propiedades
 * obligatorias ausentes. Al levantar el contexto real, es también la que confirma que los conversores de cifrado —que
 * Hibernate no puede instanciar por sí solo— quedan correctamente inyectados.
 */
class ApplicationBootTest {

    @Test
    void contextLoads() {
    }
}
