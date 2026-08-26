package co.edu.unbosque.dto;

import java.time.LocalDateTime;

public interface ConsumptionRowView {
	LocalDateTime getConsumptionOpening();
	Double getConsumptionValue();
	Double getIva();
	Double getService();
	Double getTip();
}
