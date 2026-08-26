package co.edu.unbosque.config;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import co.edu.unbosque.service.SnapshotService;

@Component
public class SnapshotBackfillRunner implements ApplicationRunner {

	private final SnapshotService snapshotService;

	public SnapshotBackfillRunner(SnapshotService snapshotService) {
		this.snapshotService = snapshotService;
	}

	@Override
	public void run(ApplicationArguments args) {
		try {
			snapshotService.backfillMissing();
		} catch (Exception e) {
			System.err.println("Snapshot backfill failed: " + e.getMessage());
		}
	}
}
