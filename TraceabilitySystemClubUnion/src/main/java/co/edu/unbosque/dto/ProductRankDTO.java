package co.edu.unbosque.dto;

public class ProductRankDTO {
	private String productId;
	private String name;
	private long quantity;
	private double revenue;

	public ProductRankDTO() {
	}

	public ProductRankDTO(String productId, String name, long quantity, double revenue) {
		this.productId = productId;
		this.name = name;
		this.quantity = quantity;
		this.revenue = revenue;
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
