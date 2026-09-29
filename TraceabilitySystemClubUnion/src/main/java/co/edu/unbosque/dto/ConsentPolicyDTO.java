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

	public ConsentPolicyDTO() {
	}

	public ConsentPolicyDTO(String version, String title, String text) {
		this.version = version;
		this.title = title;
		this.text = text;
	}

	public String getVersion() {
		return version;
	}

	public void setVersion(String version) {
		this.version = version;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getText() {
		return text;
	}

	public void setText(String text) {
		this.text = text;
	}
}
