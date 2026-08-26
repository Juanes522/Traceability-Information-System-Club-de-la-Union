package co.edu.unbosque.dto;

public class EnvironmentCategoryDTO {
	private String environment;
	private String category;
	private long quantity;
	private double revenue;

	public EnvironmentCategoryDTO() {
	}

	public EnvironmentCategoryDTO(String environment, String category, long quantity, double revenue) {
		this.environment = environment;
		this.category = category;
		this.quantity = quantity;
		this.revenue = revenue;
	}

	public String getEnvironment() {
		return environment;
	}

	public void setEnvironment(String environment) {
		this.environment = environment;
	}

	public String getCategory() {
		return category;
	}

	public void setCategory(String category) {
		this.category = category;
	}

	public long getQuantity() {
		return quantity;
	}

	public void setQuantity(long quantity) {
		this.quantity = quantity;
	}

	public double getRevenue() {
		return revenue;
	}

	public void setRevenue(double revenue) {
		this.revenue = revenue;
	}
}
