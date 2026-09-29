package co.edu.unbosque.dto;

/**
 * Política de tratamiento de datos personales que se presenta al socio.
 *
 * <p>La versión es el campo funcionalmente relevante: es lo que se compara con la que cada socio aceptó para decidir si
 * debe volver a consentir. Publicar una versión nueva obliga a todos los socios a aceptarla otra vez.
 */
public class ConsentPolicyDTO {

	private String version;
	private String title;
	private String text;

	/**
	 * Constructor sin argumentos requerido para la deserialización del cuerpo de la petición.
	 */
	public ConsentPolicyDTO() {
	}

	/**
	 * Crea una instancia con sus valores.
	 *
	 * @param version la versión vigente de la política
	 * @param title el titulo
	 * @param text el texto completo de la política
	 */
	public ConsentPolicyDTO(String version, String title, String text) {
		this.version = version;
		this.title = title;
		this.text = text;
	}

	/**
	 * Devuelve la versión vigente de la política.
	 *
	 * @return la versión vigente de la política
	 */
	public String getVersion() {
		return version;
	}

	/**
	 * Establece la versión vigente de la política.
	 *
	 * @param version la versión vigente de la política
	 */
	public void setVersion(String version) {
		this.version = version;
	}

	/**
	 * Devuelve el titulo.
	 *
	 * @return el titulo
	 */
	public String getTitle() {
		return title;
	}

	/**
	 * Establece el titulo.
	 *
	 * @param title el titulo
	 */
	public void setTitle(String title) {
		this.title = title;
	}

	/**
	 * Devuelve el texto completo de la política.
	 *
	 * @return el texto completo de la política
	 */
	public String getText() {
		return text;
	}

	/**
	 * Establece el texto completo de la política.
	 *
	 * @param text el texto completo de la política
	 */
	public void setText(String text) {
		this.text = text;
	}
}
