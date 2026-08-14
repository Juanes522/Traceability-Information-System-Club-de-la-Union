package co.unbosque.controller;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import co.unbosque.dto.ConsumptionCreateRequest;
import co.unbosque.model.PartnerConsumption;
import co.unbosque.service.PartnerConsumptionService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/partnerconsumption")
public class PartnerConsumptionController {

	@Autowired
	private PartnerConsumptionService consumptionServ;

	public PartnerConsumptionController() {
	}

	@PostMapping(path = "/registerconsumption")
	public ResponseEntity<PartnerConsumption> registerConsumption(@Valid @RequestBody ConsumptionCreateRequest req) {
		try {
			PartnerConsumption consumption = consumptionServ.register(req);
			if (consumption == null) {
				return new ResponseEntity<>(HttpStatus.NOT_FOUND);
			}
			return new ResponseEntity<>(consumption, HttpStatus.CREATED);
		} catch (Exception e) {
			return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
		}
	}

	@GetMapping("/by-environment/{env}")
	public ResponseEntity<?> getByEnvironment(
			@PathVariable String env,
			@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
			@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to,
			@RequestParam(defaultValue = "0") int page,
			@RequestParam(defaultValue = "10") int size) {
		if (from != null && to != null && ChronoUnit.DAYS.between(from, to) > 92) {
			return ResponseEntity.badRequest().body(Map.of("message", "El rango no puede superar 3 meses"));
		}
		int safePage = Math.max(page, 0);
		int safeSize = Math.min(Math.max(size, 1), 100);
		Pageable pageable = PageRequest.of(safePage, safeSize, Sort.by(Sort.Direction.DESC, "consumptionOpening"));
		return ResponseEntity.ok(consumptionServ.getByEnviromentPaged(env, from, to, pageable));
	}

	@GetMapping("/by-partner/{partnerId}")
	public ResponseEntity<List<PartnerConsumption>> getByPartner(@PathVariable Long partnerId) {
		List<PartnerConsumption> list = consumptionServ.getByPartnerId(partnerId);
		if (list == null)
			return new ResponseEntity<>(HttpStatus.NOT_FOUND);
		if (list.isEmpty())
			return new ResponseEntity<>(list, HttpStatus.NO_CONTENT);
		return new ResponseEntity<>(list, HttpStatus.OK);
	}
}
