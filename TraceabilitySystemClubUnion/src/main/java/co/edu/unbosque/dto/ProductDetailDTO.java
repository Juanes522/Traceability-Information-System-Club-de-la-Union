package co.edu.unbosque.dto;

public class ProductDetailDTO {
	private String category;
	private String subcategory;
	private String productId;
	private String name;
	private long quantity;
	private double revenue;

	public ProductDetailDTO() {
	}

	public ProductDetailDTO(String category, String subcategory, String productId, String name, long quantity,
			double revenue) {
		this.category = category;
		this.subcategory = subcategory;
		this.productId = productId;
		this.name = name;
		this.quantity = quantity;
		this.revenue = revenue;
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

	public String getProductId() {
		return productId;
	}

	public void setProductId(String productId) {
		this.productId = productId;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
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
