package co.edu.unbosque.dto;

public class CategoryMixDTO {
	private String category;
	private String subcategory;
	private long quantity;
	private double revenue;
	private double percentage;

	public CategoryMixDTO() {
	}

	public CategoryMixDTO(String category, String subcategory, long quantity, double revenue, double percentage) {
		this.category = category;
		this.subcategory = subcategory;
		this.quantity = quantity;
		this.revenue = revenue;
		this.percentage = percentage;
	}

	public String getCategory() {
		return category;
	}

	public void setCategory(String category) {
		this.category = category;
	}

	public String getSubcategory() {
		return subcategory;
	}

	public void setSubcategory(String subcategory) {
		this.subcategory = subcategory;
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

	public double getPercentage() {
		return percentage;
	}

	public void setPercentage(double percentage) {
		this.percentage = percentage;
	}
}
