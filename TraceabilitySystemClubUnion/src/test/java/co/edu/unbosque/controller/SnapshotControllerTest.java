package co.edu.unbosque.controller;

import co.edu.unbosque.service.SnapshotService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class SnapshotControllerTest {

	private SnapshotService service;
	private SnapshotController controller;

	@BeforeEach
	void setUp() {
		service = mock(SnapshotService.class);
		controller = new SnapshotController(service);
	}

	@Test
	void snapshots_returnsOkWithList() {
		when(service.list()).thenReturn(java.util.List.of());
		ResponseEntity<?> r = controller.snapshots();
		assertEquals(HttpStatus.OK, r.getStatusCode());
	}
}
