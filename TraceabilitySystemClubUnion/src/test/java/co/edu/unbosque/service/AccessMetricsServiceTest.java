package co.edu.unbosque.service;

import co.edu.unbosque.dto.AccessSummaryDTO;
import co.edu.unbosque.dto.AttendancePointDTO;
import co.edu.unbosque.dto.EnvironmentOccupancyDTO;
import co.edu.unbosque.model.Access;
import co.edu.unbosque.model.PersonPartner;
import co.edu.unbosque.repository.AccessRepository;
import co.edu.unbosque.repository.PartnerConsumptionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class AccessMetricsServiceTest {

	private AccessRepository accessRepo;
	private PartnerConsumptionRepository consumptionRepo;
	private AccessMetricsService service;
	private final LocalDateTime from = LocalDateTime.of(2026, 8, 10, 0, 0);
	private final LocalDateTime to = LocalDateTime.of(2026, 8, 12, 23, 0);

	@BeforeEach
	void setUp() {
		accessRepo = mock(AccessRepository.class);
		consumptionRepo = mock(PartnerConsumptionRepository.class);
		service = new AccessMetricsService(accessRepo, consumptionRepo);
	}

	private Access access(LocalDateTime adm) {
		Access a = new Access();
		a.setDateTimeAdmission(adm);
		PersonPartner p = new PersonPartner();
		ReflectionTestUtils.setField(p, "personId", 1L);
		a.setPartner(p);
		return a;
	}

	@Test
	void summary_computesPresentVisitsUniqueAndFrequency() {
		when(accessRepo.countByDateTimeDepartureIsNull()).thenReturn(3L);
		when(accessRepo.countByDateTimeAdmissionBetween(from, to)).thenReturn(10L);
		when(accessRepo.countDistinctPartnersInRange(from, to)).thenReturn(4L);

		AccessSummaryDTO dto = service.summary(from, to);

		assertEquals(3L, dto.getPresentNow());
		assertEquals(10L, dto.getVisits());
		assertEquals(4L, dto.getUniquePartners());
		assertEquals(2.5, dto.getAvgFrequency(), 0.001);
	}

	@Test
	void attendance_bucketsByDayWithFill() {
		when(accessRepo.findByDateTimeAdmissionBetween(from, to)).thenReturn(List.of(
				access(LocalDateTime.of(2026, 8, 10, 9, 0)),
				access(LocalDateTime.of(2026, 8, 10, 20, 0)),
				access(LocalDateTime.of(2026, 8, 12, 12, 0))
		));

		List<AttendancePointDTO> att = service.attendance(from, to, "day");

		assertEquals(List.of("2026-08-10", "2026-08-11", "2026-08-12"),
				att.stream().map(AttendancePointDTO::getBucket).toList());
		assertEquals(2L, att.get(0).getCount());
		assertEquals(0L, att.get(1).getCount());
	}

	@Test
	void occupancyToday_mapsRows() {
		when(consumptionRepo.occupancyByEnvironment(any(), any())).thenReturn(List.of(
				new Object[]{"Bar", 5L}, new Object[]{"Restaurante", 2L}));
		List<EnvironmentOccupancyDTO> occ = service.occupancyToday();
		assertEquals("Bar", occ.get(0).getEnvironment());
		assertEquals(5L, occ.get(0).getPartners());
	}
}
