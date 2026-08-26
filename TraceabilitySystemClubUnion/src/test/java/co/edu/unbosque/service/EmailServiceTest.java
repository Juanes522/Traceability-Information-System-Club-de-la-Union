package co.edu.unbosque.service;

import co.edu.unbosque.model.PartnerConsumption;
import co.edu.unbosque.model.PersonPartner;
import jakarta.mail.Session;
import jakarta.mail.internet.MimeMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.Properties;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class EmailServiceTest {

    private JavaMailSender mailSender;
    private EmailService emailService;

    @BeforeEach
    void setUp() {
        mailSender = mock(JavaMailSender.class);
        Session session = Session.getInstance(new Properties());
        when(mailSender.createMimeMessage()).thenReturn(new MimeMessage(session));

        emailService = new EmailService(mailSender);
        ReflectionTestUtils.setField(emailService, "fromAddress", "noreply@club.com");
        ReflectionTestUtils.setField(emailService, "frontendUrl", "http://localhost:4200");
    }

    @Test
    void sendConsumptionEmail_sendsEmail_whenPartnerHasPrimaryEmail() {
        PersonPartner partner = new PersonPartner();
        partner.setFirstName("Juan");
        partner.setShareNumber(42L);
        partner.setEmail(new String[]{"juan@example.com"});

        PartnerConsumption c = new PartnerConsumption();
        c.setEnviroment("Restaurante");
        c.setTable("5");
        c.setConsumptionOpening(LocalDateTime.of(2026, 5, 17, 12, 0));
        c.setConsumptionClosing(LocalDateTime.of(2026, 5, 17, 12, 20));
        c.setWaiterName("Carlos");
        c.setConsumptionValue(50000.0);
        c.setIva(9500.0);
        c.setService(5000.0);
        c.setTip(2000.0);

        emailService.sendConsumptionNotificationEmail(partner, c, 66500.0);

        verify(mailSender, times(1)).send(any(MimeMessage.class));
    }

    @Test
    void sendConsumptionEmail_skips_whenEmailArrayIsEmpty() {
        PersonPartner partner = new PersonPartner();
        partner.setEmail(new String[]{});

        emailService.sendConsumptionNotificationEmail(partner, new PartnerConsumption(), 0.0);

        verify(mailSender, never()).send(any(MimeMessage.class));
    }

    @Test
    void sendConsumptionEmail_skips_whenEmailArrayIsNull() {
        PersonPartner partner = new PersonPartner();
        partner.setEmail(null);

        emailService.sendConsumptionNotificationEmail(partner, new PartnerConsumption(), 0.0);

        verify(mailSender, never()).send(any(MimeMessage.class));
    }
}
