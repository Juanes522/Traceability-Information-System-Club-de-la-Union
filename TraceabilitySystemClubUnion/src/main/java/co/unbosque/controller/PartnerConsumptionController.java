package co.unbosque.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import co.unbosque.dto.ConsumptionCreateRequest;
import co.unbosque.model.PartnerConsumption;
import co.unbosque.service.PartnerConsumptionService;

@RestController
@RequestMapping("/partnerconsumption")
public class PartnerConsumptionController {

	@Autowired
	private PartnerConsumptionService consumptionServ;

	public PartnerConsumptionController() {
	}

	@PostMapping(path = "/registerconsumption")
	public ResponseEntity<PartnerConsumption> registerConsumption(@RequestBody ConsumptionCreateRequest req) {
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
	public ResponseEntity<List<PartnerConsumption>> getByEnvironment(@PathVariable String env) {
		List<PartnerConsumption> list = consumptionServ.getByEnviroment(env);
		if (list == null)
			return new ResponseEntity<>(HttpStatus.NOT_FOUND);
		if (list.isEmpty())
			return new ResponseEntity<>(list, HttpStatus.NO_CONTENT);
		return new ResponseEntity<>(list, HttpStatus.OK);
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
