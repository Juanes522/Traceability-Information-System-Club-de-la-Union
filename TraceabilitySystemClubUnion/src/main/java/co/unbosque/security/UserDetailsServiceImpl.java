package co.unbosque.security;

import co.unbosque.model.PersonPartner;
import co.unbosque.repository.PersonPartnerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collections;

@Service
public class UserDetailsServiceImpl implements UserDetailsService {

    @Autowired
    private PersonPartnerRepository personPartnerRepository;

    @Override
    public UserDetails loadUserByUsername(String identification) throws UsernameNotFoundException {
        PersonPartner titular = personPartnerRepository.findByIdentification(identification)
                .orElseThrow(() -> new UsernameNotFoundException("Socio Titular no encontrado con la identificación: " + identification));

        String userPassword = titular.getPassword();
        if (userPassword == null) {
            userPassword = "";
        }

        return new User(
                identification,
                userPassword,
                Collections.singletonList(new SimpleGrantedAuthority(titular.getRole()))
        );
    }
}