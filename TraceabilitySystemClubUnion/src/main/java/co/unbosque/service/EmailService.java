package co.unbosque.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;

@Service
public class EmailService {

	private final JavaMailSender mailSender;

	@Value("${spring.mail.username}")
	private String fromAddress;

	@Value("${app.frontend.url}")
	private String frontendUrl;

	public EmailService(JavaMailSender mailSender) {
		this.mailSender = mailSender;
	}

	public void sendPasswordResetEmail(String toEmail, String resetToken, String partnerFirstName)
			throws MessagingException {
		MimeMessage message = mailSender.createMimeMessage();
		MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

		helper.setFrom(fromAddress);
		helper.setTo(toEmail);
		helper.setSubject("Recuperación de contraseña - Club de la Unión");

		String resetLink = frontendUrl + "/auth/reset-password?token=" + resetToken;
		helper.setText(buildEmailHtml(partnerFirstName, resetLink), true);
		mailSender.send(message);
	}

	private String buildEmailHtml(String firstName, String resetLink) {
		String safeName = escapeHtml(firstName);
		return "<!DOCTYPE html>" + "<html lang='es'><head><meta charset='UTF-8'>"
				+ "<meta name='viewport' content='width=device-width,initial-scale=1'>"
				+ "<title>Recuperación de contraseña</title></head>"
				+ "<body style='margin:0;padding:0;background:#151719;font-family:Arial,Helvetica,sans-serif;'>"
				+ "<table width='100%' cellpadding='0' cellspacing='0' style='background:#151719;padding:40px 16px;'>"
				+ "<tr><td align='center'>"
				+ "<table width='100%' style='max-width:480px;background:#1e2227;border-radius:16px;"
				+ "border:1px solid rgba(255,255,255,0.07);box-shadow:0 24px 60px rgba(0,0,0,0.5);overflow:hidden;'>"
				+ "<tr><td style='background:linear-gradient(135deg,#1e2227 0%,#2a2f38 100%);"
				+ "padding:32px 40px;text-align:center;border-bottom:1px solid rgba(199,165,103,0.2);'>"
				+ "<p style='margin:0;color:#c7a567;font-size:13px;font-weight:700;letter-spacing:0.12em;"
				+ "text-transform:uppercase;'>Club de la Unión</p>"
				+ "<h1 style='margin:8px 0 0;color:#ffffff;font-size:22px;font-weight:700;'>Recupera tu contraseña</h1>"
				+ "</td></tr>" + "<tr><td style='padding:36px 40px;'>"
				+ "<p style='margin:0 0 16px;color:rgba(255,255,255,0.75);font-size:15px;line-height:1.6;'>"
				+ "Hola, <strong style='color:#fff;'>" + safeName + "</strong>.</p>"
				+ "<p style='margin:0 0 28px;color:rgba(255,255,255,0.6);font-size:14px;line-height:1.6;'>"
				+ "Recibimos una solicitud para restablecer la contraseña de tu cuenta. "
				+ "Haz clic en el botón a continuación para crear una nueva contraseña. "
				+ "Este enlace es válido por <strong style='color:#c7a567;'>1 hora</strong>.</p>"
				+ "<div style='text-align:center;margin-bottom:28px;'>" + "<a href='" + resetLink + "' "
				+ "style='display:inline-block;background:#c7a567;color:#212529;text-decoration:none;"
				+ "font-weight:700;font-size:15px;padding:14px 36px;border-radius:8px;"
				+ "letter-spacing:0.03em;'>Restablecer contraseña</a></div>"
				+ "<p style='margin:0 0 12px;color:rgba(255,255,255,0.35);font-size:12px;line-height:1.5;'>"
				+ "Si el botón no funciona, copia y pega este enlace en tu navegador:</p>"
				+ "<p style='margin:0;word-break:break-all;'>" + "<a href='" + resetLink
				+ "' style='color:#c7a567;font-size:12px;'>" + resetLink + "</a></p>" + "</td></tr>"
				+ "<tr><td style='padding:20px 40px;border-top:1px solid rgba(255,255,255,0.07);text-align:center;'>"
				+ "<p style='margin:0;color:rgba(255,255,255,0.25);font-size:12px;'>"
				+ "Si no solicitaste este cambio, puedes ignorar este correo. Tu contraseña no cambiará.</p>"
				+ "</td></tr></table></td></tr></table></body></html>";
	}

	private String escapeHtml(String text) {
		if (text == null)
			return "";
		return text.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
	}
}
