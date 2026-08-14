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

import co.unbosque.dto.NotificationDTO;
import co.unbosque.model.PersonPartner;
import co.unbosque.service.PartnerConsumptionService;
import co.unbosque.service.PersonPartnerService;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;

@RestController
@RequestMapping("/personpartner")
public class PersonPartnerController {

	@Autowired
	private PersonPartnerService partnerServ;

	@Autowired
	private PartnerConsumptionService consumptionServ;

	public PersonPartnerController() {
	}

	@PreAuthorize("hasAnyRole('MANAGER', 'ADMIN')")
	@GetMapping(path = "/getbyidentification/{identification}")
	public ResponseEntity<PersonPartner> getByIdentification(@PathVariable String identification) {
		PersonPartner partner = partnerServ.getByIdentification(identification);
		if (partner == null) {
			return new ResponseEntity<>(partner, HttpStatus.NOT_FOUND);
		}
		return new ResponseEntity<>(partner, HttpStatus.OK);
	}

	@PreAuthorize("hasAnyRole('MANAGER', 'ADMIN')")
	@GetMapping(path = "/getbyfirstname/{firstname}")
	public ResponseEntity<List<PersonPartner>> getByFirstName(@PathVariable String firstname) {
		List<PersonPartner> partners = partnerServ.getByFirstName(firstname);
		if (partners == null || partners.isEmpty()) {
			return new ResponseEntity<>(partners, HttpStatus.NO_CONTENT);
		}
		return new ResponseEntity<>(partners, HttpStatus.OK);
	}

	@PreAuthorize("hasAnyRole('MANAGER', 'ADMIN')")
	@GetMapping(path = "/getbysecondname/{secondname}")
	public ResponseEntity<List<PersonPartner>> getBySecondName(@PathVariable String secondname) {
		List<PersonPartner> partners = partnerServ.getBySecondName(secondname);
		if (partners == null || partners.isEmpty()) {
			return new ResponseEntity<>(partners, HttpStatus.NO_CONTENT);
		}
		return new ResponseEntity<>(partners, HttpStatus.OK);
	}

	@PreAuthorize("hasAnyRole('MANAGER', 'ADMIN')")
	@GetMapping(path = "/getbysharenumber/{sharenumber}")
	public ResponseEntity<List<PersonPartner>> getByShareNumber(@PathVariable Long sharenumber) {
		List<PersonPartner> partners = partnerServ.getByShareNumber(sharenumber);
		if (partners == null || partners.isEmpty()) {
			return new ResponseEntity<>(partners, HttpStatus.NO_CONTENT);
		}
		return new ResponseEntity<>(partners, HttpStatus.OK);
	}

	@PreAuthorize("hasAnyRole('MANAGER', 'ADMIN')")
	@GetMapping(path = "/getall")
	public ResponseEntity<List<PersonPartner>> getAll() {
		List<PersonPartner> partners = partnerServ.getAll();
		if (partners.isEmpty()) {
			return new ResponseEntity<>(partners, HttpStatus.NO_CONTENT);
		}
		return new ResponseEntity<>(partners, HttpStatus.OK);
	}

	@PreAuthorize("hasAnyRole('MANAGER', 'ADMIN')")
	@GetMapping(path = "/getallpaged")
	public ResponseEntity<Page<PersonPartner>> getAllPaged(
			@RequestParam(defaultValue = "0") int page,
			@RequestParam(defaultValue = "10") int size) {
		int safePage = Math.max(page, 0);
		int safeSize = Math.min(Math.max(size, 1), 100);
		return new ResponseEntity<>(partnerServ.getAllPaged(PageRequest.of(safePage, safeSize)), HttpStatus.OK);
	}

	@GetMapping(path = "/me")
	public ResponseEntity<PersonPartner> getMe() {
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		if (auth == null || !auth.isAuthenticated() || auth.getPrincipal() instanceof String) {
			return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
		}
		String identification = ((UserDetails) auth.getPrincipal()).getUsername();
		PersonPartner partner = partnerServ.getByIdentification(identification);

		if (partner == null) {
			return new ResponseEntity<>(HttpStatus.NOT_FOUND);
		}

		return new ResponseEntity<>(partner, HttpStatus.OK);
	}

	@GetMapping(path = "/getconsumptions/me")
	public ResponseEntity<?> getMyConsumptions(
			@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
			@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to,
			@RequestParam(defaultValue = "0") int page,
			@RequestParam(defaultValue = "10") int size) {
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		if (auth == null || !auth.isAuthenticated() || auth.getPrincipal() instanceof String) {
			return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
		}
		if (from != null && to != null && ChronoUnit.DAYS.between(from, to) > 92) {
			return ResponseEntity.badRequest().body(Map.of("message", "El rango no puede superar 3 meses"));
		}
		String identification = ((UserDetails) auth.getPrincipal()).getUsername();
		PersonPartner partner = partnerServ.getByIdentification(identification);
		if (partner == null) {
			return new ResponseEntity<>(HttpStatus.NOT_FOUND);
		}
		int safePage = Math.max(page, 0);
		int safeSize = Math.min(Math.max(size, 1), 100);
		Pageable pageable = PageRequest.of(safePage, safeSize, Sort.by(Sort.Direction.DESC, "consumptionOpening"));
		return ResponseEntity.ok(consumptionServ.getByPartnerPaged(partner.getPersonId(), from, to, pageable));
	}

	@PreAuthorize("hasAnyRole('MANAGER', 'ADMIN')")
	@GetMapping(path = "/getconsumptionsidentification/{identification}")
	public ResponseEntity<?> getConsumptionsByIdentification(
			@PathVariable String identification,
			@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
			@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to,
			@RequestParam(defaultValue = "0") int page,
			@RequestParam(defaultValue = "10") int size) {
		if (from != null && to != null && ChronoUnit.DAYS.between(from, to) > 92) {
			return ResponseEntity.badRequest().body(Map.of("message", "El rango no puede superar 3 meses"));
		}
		PersonPartner titular = partnerServ.getByIdentification(identification);
		if (titular == null) {
			return new ResponseEntity<>(HttpStatus.NOT_FOUND);
		}
		int safePage = Math.max(page, 0);
		int safeSize = Math.min(Math.max(size, 1), 100);
		Pageable pageable = PageRequest.of(safePage, safeSize, Sort.by(Sort.Direction.DESC, "consumptionOpening"));
		return ResponseEntity.ok(consumptionServ.getByPartnerPaged(titular.getPersonId(), from, to, pageable));
	}

	@GetMapping("/notifications/me")
	public ResponseEntity<List<NotificationDTO>> getMyNotifications() {
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		if (auth == null || !auth.isAuthenticated() || auth.getPrincipal() instanceof String) {
			return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
		}
		String identification = ((UserDetails) auth.getPrincipal()).getUsername();
		List<NotificationDTO> list = consumptionServ.getNotificationsForPartner(identification);
		if (list == null || list.isEmpty())
			return new ResponseEntity<>(HttpStatus.NO_CONTENT);
		return new ResponseEntity<>(list, HttpStatus.OK);
	}

}
