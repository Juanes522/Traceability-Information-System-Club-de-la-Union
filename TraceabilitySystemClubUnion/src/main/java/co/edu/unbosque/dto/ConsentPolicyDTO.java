package co.edu.unbosque.dto;

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
