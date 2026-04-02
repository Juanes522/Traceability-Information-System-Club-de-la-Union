package co.unbosque.controller;

import co.unbosque.dto.AuthRequest;
import co.unbosque.dto.AuthResponse;
import co.unbosque.dto.ChangePasswordRequest;
import co.unbosque.model.PersonPartner;
import co.unbosque.security.JwtUtil;
import co.unbosque.service.PersonPartnerService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/auth")
public class AuthController {

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private UserDetailsService userDetailsService;

    @Autowired
    private JwtUtil jwtUtil;
    
    @Autowired
    private PersonPartnerService personPartnerService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @PostMapping("/login")
    public ResponseEntity<?> createAuthenticationToken(@RequestBody AuthRequest authRequest) {
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(authRequest.getEmail(), authRequest.getPassword())
            );
        } catch (Exception e) {
        	 System.out.println(e);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Credenciales incorrectas");
        }
        final UserDetails userDetails = userDetailsService.loadUserByUsername(authRequest.getEmail());
        System.out.println("Pase 2");
        final String jwt = jwtUtil.generateToken(userDetails);
        PersonPartner titular = personPartnerService.getTitularByEmail(authRequest.getEmail());
        if (titular==null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }

        Boolean needsChange = titular.getForcePasswordChange();

        return ResponseEntity.ok(new AuthResponse(jwt, needsChange != null ? needsChange : true));
    }

    @PostMapping("/change-password")
    public ResponseEntity<?> changePassword(@RequestBody ChangePasswordRequest request) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || auth.getPrincipal() instanceof String) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        String email = ((UserDetails) auth.getPrincipal()).getUsername();

        PersonPartner titular = personPartnerService.getTitularByEmail(email);
        if (titular==null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }

        titular.setPassword(passwordEncoder.encode(request.getNewPassword()));
        titular.setForcePasswordChange(false);
        personPartnerService.savePartner(titular);

        return ResponseEntity.ok("Contraseña cambiada exitosamente.");
    }

    @PostMapping("/recover-password")
    public ResponseEntity<?> recoverPassword(@RequestParam String email) {
    	PersonPartner titular = personPartnerService.getTitularByEmail(email);
        if (titular!=null) {
            System.out.println("Mock recovery email sent to: " + email);
            return ResponseEntity.ok("Si el correo existe, se han enviado instrucciones de recuperación.");
        }
        return ResponseEntity.ok("Si el correo existe, se han enviado instrucciones de recuperación.");
    }
}
