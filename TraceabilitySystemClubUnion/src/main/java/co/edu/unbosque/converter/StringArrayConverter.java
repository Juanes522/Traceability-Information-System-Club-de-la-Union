package co.edu.unbosque.converter;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

/**
 * Persiste un arreglo de cadenas como CSV <strong>en claro</strong>.
 *
 * <p><strong>Esta clase no se usa.</strong> Una búsqueda en todo el proyecto no encuentra ninguna
 * referencia fuera de su propia declaración: ninguna entidad la aplica con {@code @Convert} y, al no
 * declararse {@code autoApply = true}, es completamente inerte.
 *
 * <p>Es el predecesor de {@link EncryptedStringArrayConverter}: así se almacenaban los correos de
 * {@link co.edu.unbosque.model.PersonPartner} antes de introducir el cifrado en reposo. Quedó en el
 * código tras la migración.
 *
 * <p>Se documenta —en lugar de simplemente ignorarla— porque explica el formato de los datos heredados:
 * las filas escritas por este conversor contienen un CSV sin prefijo, y es precisamente ese caso el que
 * {@code EncryptedStringArrayConverter} sigue sabiendo leer para no romper las bases preexistentes.
 *
 * <p>Es también el único conversor del paquete sin prueba unitaria.
 */
@Converter
public class StringArrayConverter implements AttributeConverter<String[], String> {

    /** Separador entre elementos. Sin escape, igual que en el conversor que lo sustituyó. */
    private static final String SEPARATOR = ",";

    @Override
    public String convertToDatabaseColumn(String[] attribute) {
        if (attribute == null || attribute.length == 0) {
            return null;
        }
        return String.join(SEPARATOR, attribute);
    }

    @Override
    public String[] convertToEntityAttribute(String dbData) {
        if (dbData == null || dbData.isEmpty()) {
            return new String[0];
        }
        return dbData.split(SEPARATOR);
    }
}