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
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        PersonPartner titular = personPartnerRepository.findTitularByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Socio Titular no encontrado con el correo: " + email));

        String userPassword = titular.getPassword();
        if (userPassword == null) {
            userPassword = ""; // Prevent IllegalArgumentException if password is null in DB
        }

        String assignedRole = titular.getRole();
        if (assignedRole == null || assignedRole.trim().isEmpty()) {
            assignedRole = "ROLEPARTNER"; // Default fallback
        } else if (!assignedRole.startsWith("ROLE")) {
            assignedRole = "ROLE_" + assignedRole.toUpperCase(); // Standardize adding the prefix if missing
        }

        return new User(
                email, // We use the email to identify the principal
                userPassword, 
                Collections.singletonList(new SimpleGrantedAuthority(assignedRole))
        );
    }
}