package co.edu.unbosque.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import co.edu.unbosque.service.SnapshotService;

@RestController
@RequestMapping("/metrics/snapshots")
public class SnapshotController {

	private final SnapshotService snapshotService;

	public SnapshotController(SnapshotService snapshotService) {
		this.snapshotService = snapshotService;
	}

	@PreAuthorize("hasAnyRole('MANAGER', 'ADMIN')")
	@GetMapping
	public ResponseEntity<?> snapshots() {
		return ResponseEntity.ok(snapshotService.list());
	}
}
