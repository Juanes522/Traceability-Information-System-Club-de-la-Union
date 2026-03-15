package co.unbosque.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import co.unbosque.model.PartnerConsumption;
import co.unbosque.model.PersonPartner;
import co.unbosque.service.PersonPartnerService;

@RestController
@RequestMapping("/personpartner")
public class PersonPartnerController {

    @Autowired
    private PersonPartnerService partnerServ;

    public PersonPartnerController() {
    }

    @GetMapping(path = "/getall")
    public ResponseEntity<List<PersonPartner>> getAll(){
        List<PersonPartner> partners = partnerServ.getAll();
        if (partners.isEmpty()) {
            return new ResponseEntity<List<PersonPartner>>(partners, HttpStatus.NO_CONTENT);
        }
        return new ResponseEntity<List<PersonPartner>>(partners, HttpStatus.OK);
    }

    @GetMapping(path = "/getbyid/{id}")
    public ResponseEntity<PersonPartner> getById(@PathVariable Long id){
        PersonPartner partner = partnerServ.getById(id);
        if(partner == null){
            return new ResponseEntity<PersonPartner>(partner, HttpStatus.NOT_FOUND);
        }

        return new ResponseEntity<PersonPartner>(partner, HttpStatus.OK);
    }

    @GetMapping(path = "/getbyidentification/{identification}")
    public ResponseEntity<PersonPartner> getByIdentification(@PathVariable String identification){
        PersonPartner partner = partnerServ.getByIdentification(identification);

        if(partner == null){
            return new ResponseEntity<PersonPartner>(partner, HttpStatus.NOT_FOUND);
        }

        return new ResponseEntity<PersonPartner>(partner, HttpStatus.OK);
    }

    @GetMapping(path = "/getbyfirstname/{firstname}")
    public ResponseEntity<List<PersonPartner>> getByFirstName(@PathVariable String firstname){
        List<PersonPartner> partners = partnerServ.getByFirstName(firstname);

        if(partners == null){
            return new ResponseEntity<List<PersonPartner>>(partners, HttpStatus.NOT_FOUND);
        }

        if(partners.isEmpty()){
            return new ResponseEntity<List<PersonPartner>>(partners, HttpStatus.NO_CONTENT);
        }

        return new ResponseEntity<List<PersonPartner>>(partners, HttpStatus.OK);
    }

    @GetMapping(path = "/getbysecondname/{secondname}")
    public ResponseEntity<List<PersonPartner>> getBySecondName(@PathVariable String secondname){
        List<PersonPartner> partners = partnerServ.getBySecondName(secondname);

        if(partners == null){
            return new ResponseEntity<List<PersonPartner>>(partners, HttpStatus.NOT_FOUND);
        }

        if(partners.isEmpty()){
            return new ResponseEntity<List<PersonPartner>>(partners, HttpStatus.NO_CONTENT);
        }

        return new ResponseEntity<List<PersonPartner>>(partners, HttpStatus.OK);
    }

    @GetMapping(path = "/getbysharenumber/{sharenumber}")
    public ResponseEntity<List<PersonPartner>> getByShareNumber(@PathVariable Long sharenumber){
        List<PersonPartner> partners = partnerServ.getByShareNumber(sharenumber);

        if(partners == null){
            return new ResponseEntity<List<PersonPartner>>(partners, HttpStatus.NOT_FOUND);
        }

        if(partners.isEmpty()){
            return new ResponseEntity<List<PersonPartner>>(partners, HttpStatus.NO_CONTENT);
        }

        return new ResponseEntity<List<PersonPartner>>(partners, HttpStatus.OK);
    }

    @GetMapping(path = "/getbyownerid/{ownerid}")
    public ResponseEntity<List<PersonPartner>> getByOwnerPersonId(@PathVariable Long ownerid){
        List<PersonPartner> partners = partnerServ.getByOwnerPersonId(ownerid);

        if(partners == null){
            return new ResponseEntity<List<PersonPartner>>(partners, HttpStatus.NOT_FOUND);
        }

        if(partners.isEmpty()){
            return new ResponseEntity<List<PersonPartner>>(partners, HttpStatus.NO_CONTENT);
        }

        return new ResponseEntity<List<PersonPartner>>(partners, HttpStatus.OK);
    }

    @GetMapping(path = "/getconsumptions/{id}")
    public ResponseEntity<List<PartnerConsumption>> getConsumptions(@PathVariable Long id){
        List<PartnerConsumption> consumptions = partnerServ.getByConsuption(id);

        if(consumptions == null){
            return new ResponseEntity<List<PartnerConsumption>>(consumptions, HttpStatus.NOT_FOUND);
        }

        if(consumptions.isEmpty()){
            return new ResponseEntity<List<PartnerConsumption>>(consumptions, HttpStatus.NO_CONTENT);
        }

        return new ResponseEntity<List<PartnerConsumption>>(consumptions, HttpStatus.OK);
    }

}