package co.unbosque.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import co.unbosque.model.PartnerConsumption;
import co.unbosque.model.PersonPartner;
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

    public PersonPartnerController() {
    }
    
    @PreAuthorize("hasAnyRole('MANAGER', 'ADMIN')")
    @GetMapping(path = "/getbyidentification/{identification}")
    public ResponseEntity<PersonPartner> getByIdentification(@PathVariable String identification){
        PersonPartner partner = partnerServ.getByIdentification(identification);
        if(partner == null){
            return new ResponseEntity<>(partner, HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<>(partner, HttpStatus.OK);
    }
    
    @PreAuthorize("hasAnyRole('MANAGER', 'ADMIN')")
    @GetMapping(path = "/getbyfirstname/{firstname}")
    public ResponseEntity<List<PersonPartner>> getByFirstName(@PathVariable String firstname){
        List<PersonPartner> partners = partnerServ.getByFirstName(firstname);
        if(partners == null || partners.isEmpty()){
            return new ResponseEntity<>(partners, HttpStatus.NO_CONTENT);
        }
        return new ResponseEntity<>(partners, HttpStatus.OK);
    }
    
    @PreAuthorize("hasAnyRole('MANAGER', 'ADMIN')")
    @GetMapping(path = "/getbysecondname/{secondname}")
    public ResponseEntity<List<PersonPartner>> getBySecondName(@PathVariable String secondname){
        List<PersonPartner> partners = partnerServ.getBySecondName(secondname);
        if(partners == null || partners.isEmpty()){
            return new ResponseEntity<>(partners, HttpStatus.NO_CONTENT);
        }
        return new ResponseEntity<>(partners, HttpStatus.OK);
    }
    
    @GetMapping(path = "/getdependents/me")
    public ResponseEntity<List<PersonPartner>> getMyDependents() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || auth.getPrincipal() instanceof String) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }
        String email = ((UserDetails) auth.getPrincipal()).getUsername();
        PersonPartner partner = partnerServ.getTitularByEmail(email);
        if(partner==null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        List<PersonPartner> dependents = partnerServ.getByOwnerPersonId(partner.getPersonId());
        
        if(dependents == null || dependents.isEmpty()){
            return new ResponseEntity<>(dependents, HttpStatus.NO_CONTENT);
        }

        return new ResponseEntity<>(dependents, HttpStatus.OK);
    }
    
    @PreAuthorize("hasAnyRole('MANAGER', 'ADMIN')")
    @GetMapping(path = "/getdependents/identification/{identification}")
    public ResponseEntity<List<PersonPartner>> getDependentsByIdentification(@PathVariable String identification){
        PersonPartner titular = partnerServ.getByIdentification(identification);
        
        List<PersonPartner> dependents = partnerServ.getByOwnerPersonId(titular.getPersonId());
        if(dependents == null || dependents.isEmpty()){
            return new ResponseEntity<>(dependents, HttpStatus.NO_CONTENT);
        }
        return new ResponseEntity<>(dependents, HttpStatus.OK);
    }
    
    @PreAuthorize("hasAnyRole('MANAGER', 'ADMIN')")
    @GetMapping(path = "/getbysharenumber/{sharenumber}")
    public ResponseEntity<List<PersonPartner>> getByShareNumber(@PathVariable Long sharenumber){
        List<PersonPartner> partners = partnerServ.getByShareNumber(sharenumber);
        if(partners == null || partners.isEmpty()){
            return new ResponseEntity<>(partners, HttpStatus.NO_CONTENT);
        }
        return new ResponseEntity<>(partners, HttpStatus.OK);
    }
    
    @PreAuthorize("hasAnyRole('MANAGER', 'ADMIN')")
    @GetMapping(path = "/getall")
    public ResponseEntity<List<PersonPartner>> getAll(){
        List<PersonPartner> partners = partnerServ.getAll();
        if (partners.isEmpty()) {
            return new ResponseEntity<>(partners, HttpStatus.NO_CONTENT);
        }
        return new ResponseEntity<>(partners, HttpStatus.OK);
    }

    @GetMapping(path = "/me")
    public ResponseEntity<PersonPartner> getMe() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || auth.getPrincipal() instanceof String) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }
        String email = ((UserDetails) auth.getPrincipal()).getUsername();
        PersonPartner partner = partnerServ.getTitularByEmail(email);
        
        if(partner == null){
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        return new ResponseEntity<>(partner, HttpStatus.OK);
    }

    @GetMapping(path = "/getconsumptions/me")
    public ResponseEntity<List<PartnerConsumption>> getMyConsumptions() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || auth.getPrincipal() instanceof String) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }
        String email = ((UserDetails) auth.getPrincipal()).getUsername();
        PersonPartner partner = partnerServ.getTitularByEmail(email);
        if(partner==null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        
        List<PartnerConsumption> consumptions = partnerServ.getByComsuption(partner.getPersonId());
        
        if(consumptions == null || consumptions.isEmpty()){
            return new ResponseEntity<>(consumptions, HttpStatus.NO_CONTENT);
        }

        return new ResponseEntity<>(consumptions, HttpStatus.OK);
    }
    
    @PreAuthorize("hasAnyRole('MANAGER', 'ADMIN')")
    @GetMapping(path = "/getconsumptionsidentification/{identification}")
    public ResponseEntity<List<PartnerConsumption>> getConsumptionsByIdentification(@PathVariable String identification){
        PersonPartner titular = partnerServ.getByIdentification(identification);
        List<PartnerConsumption> consumptions = titular.getConsumptions();
        if(consumptions == null || consumptions.isEmpty()){
            return new ResponseEntity<>(consumptions, HttpStatus.NO_CONTENT);
        }
        return new ResponseEntity<>(consumptions, HttpStatus.OK);
    }
    
}