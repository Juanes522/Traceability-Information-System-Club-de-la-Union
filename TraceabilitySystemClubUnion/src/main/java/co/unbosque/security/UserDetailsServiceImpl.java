package co.unbosque.security;

import co.unbosque.model.PersonPartner;
import co.unbosque.repository.PersonPartnerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsPasswordService;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collections;

@Service
public class UserDetailsServiceImpl implements UserDetailsService, UserDetailsPasswordService {

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

    @Override
    public UserDetails updatePassword(UserDetails user, String newPassword) {
        PersonPartner titular = personPartnerRepository.findByIdentification(user.getUsername())
                .orElseThrow(() -> new UsernameNotFoundException(
                        "Socio Titular no encontrado con la identificación: " + user.getUsername()));

        titular.setPassword(newPassword);
        personPartnerRepository.save(titular);

        return new User(user.getUsername(), newPassword, user.getAuthorities());
    }
}