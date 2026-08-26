package co.edu.unbosque.service;

import co.edu.unbosque.model.PartnerConsumption;
import co.edu.unbosque.model.PersonPartner;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

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
		String safeLink = escapeHtml(resetLink);
		return ("""
		    <!DOCTYPE html><html lang="es"><head><meta charset="UTF-8">
		    <meta name="viewport" content="width=device-width,initial-scale=1">
		    <title>Recuperación de contraseña</title></head>
		    <body style="margin:0;padding:0;background:#0f1115;font-family:'Segoe UI',Arial,Helvetica,sans-serif;">
		    <table width="100%%" cellpadding="0" cellspacing="0" style="background:#0f1115;padding:40px 16px;">
		    <tr><td align="center">
		    <table width="100%%" style="max-width:480px;background:#1a1d24;border-radius:16px;border:1px solid rgba(255,255,255,0.06);box-shadow:0 32px 80px rgba(0,0,0,0.6);overflow:hidden;">
		    <tr><td style="background:linear-gradient(135deg,#1a1d24 0%%,#252a35 100%%);padding:32px 40px;text-align:center;border-bottom:2px solid rgba(199,165,103,0.25);">
		      <p style="margin:0 0 6px;color:#c7a567;font-size:11px;font-weight:700;letter-spacing:0.16em;text-transform:uppercase;">Club de la Unión</p>
		      <h1 style="margin:0;color:#fff;font-size:22px;font-weight:700;letter-spacing:-0.02em;">Recupera tu contraseña</h1>
		    </td></tr>
		    <tr><td style="padding:32px 40px 24px;">
		      <p style="margin:0 0 6px;color:rgba(255,255,255,0.9);font-size:16px;font-weight:600;">Hola, %s.</p>
		      <p style="margin:0 0 24px;color:rgba(255,255,255,0.55);font-size:14px;line-height:1.7;">
		        Recibimos una solicitud para restablecer la contraseña de tu cuenta. Haz clic en el botón para crear una nueva.
		      </p>
		      <div style="background:rgba(199,165,103,0.1);border:1px solid rgba(199,165,103,0.3);border-radius:8px;padding:12px 16px;margin-bottom:24px;text-align:center;">
		        <p style="margin:0;color:#c7a567;font-size:13px;font-weight:600;">Este enlace es válido por 1 hora</p>
		      </div>
		      <div style="text-align:center;margin-bottom:24px;">
		        <a href="%s" style="display:inline-block;background:#c7a567;color:#0f1115;text-decoration:none;font-weight:700;font-size:15px;padding:14px 40px;border-radius:8px;letter-spacing:0.02em;">
		          Restablecer contraseña
		        </a>
		      </div>
		      <p style="margin:0 0 8px;color:rgba(255,255,255,0.3);font-size:12px;">Si el botón no funciona, copia este enlace en tu navegador:</p>
		      <p style="margin:0 0 28px;word-break:break-all;">
		        <a href="%s" style="color:#c7a567;font-size:12px;">%s</a>
		      </p>
		      <div style="background:#12151b;border:1px solid rgba(255,255,255,0.06);border-radius:8px;padding:14px 16px;">
		        <p style="margin:0;color:rgba(255,255,255,0.4);font-size:12px;line-height:1.6;">
		          <strong style="color:rgba(255,255,255,0.6);">¿No solicitaste este cambio?</strong><br>
		          Cambia tu contraseña inmediatamente y comunícate con el club. No compartas este enlace con nadie.
		        </p>
		      </div>
		    </td></tr>
		    <tr><td style="padding:20px 40px 28px;border-top:1px solid rgba(255,255,255,0.06);text-align:center;">
		      <p style="margin:0;color:rgba(255,255,255,0.2);font-size:11px;">Club de la Unión &middot; Sistema de Trazabilidad</p>
		    </td></tr>
		    </table></td></tr></table></body></html>
		    """).formatted(safeName, safeLink, safeLink, safeLink);
	}

	@Async
	public void sendConsumptionNotificationEmail(PersonPartner partner,
	                                              PartnerConsumption consumption,
	                                              double total) {
		String[] emails = partner.getEmail();
		if (emails == null || emails.length == 0
				|| emails[0] == null || emails[0].isBlank()) {
			return;
		}
		try {
			MimeMessage message = mailSender.createMimeMessage();
			MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
			helper.setFrom(fromAddress);
			helper.setTo(emails[0]);
			helper.setSubject("Nuevo cargo registrado — Club de la Unión");
			helper.setText(buildConsumptionEmailHtml(partner, consumption, total), true);
			mailSender.send(message);
		} catch (Exception e) {
			System.err.println("Consumption email failed for " + emails[0] + ": " + e.getMessage());
		}
	}

	private String buildConsumptionEmailHtml(PersonPartner partner,
	                                          PartnerConsumption consumption,
	                                          double total) {
		DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
		String firstName  = escapeHtml(partner.getFirstName() != null ? partner.getFirstName() : "Socio");
		String shareNum   = partner.getShareNumber() != null ? String.valueOf(partner.getShareNumber()) : "—";
		String ambiente   = escapeHtml(consumption.getEnviroment() != null ? consumption.getEnviroment() : "—");
		String mesa       = escapeHtml(consumption.getTable() != null ? consumption.getTable() : "—");
		String apertura   = consumption.getConsumptionOpening() != null
		                    ? consumption.getConsumptionOpening().format(dtf) : "—";
		String cierre     = consumption.getConsumptionClosing() != null
		                    ? consumption.getConsumptionClosing().format(dtf) : "—";
		String mesero     = escapeHtml(consumption.getWaiterName() != null ? consumption.getWaiterName() : "—");
		String fmtBase    = formatCurrency(consumption.getConsumptionValue());
		String fmtIva     = formatCurrency(consumption.getIva());
		String fmtService = formatCurrency(consumption.getService());
		String fmtTip     = formatCurrency(consumption.getTip());
		String fmtTotal   = formatCurrency(total);

		return ("""
		    <!DOCTYPE html><html lang="es"><head><meta charset="UTF-8">
		    <meta name="viewport" content="width=device-width,initial-scale=1">
		    <title>Nuevo cargo registrado</title></head>
		    <body style="margin:0;padding:0;background:#0f1115;font-family:'Segoe UI',Arial,Helvetica,sans-serif;">
		    <table width="100%%" cellpadding="0" cellspacing="0" style="background:#0f1115;padding:40px 16px;">
		    <tr><td align="center">
		    <table width="100%%" style="max-width:520px;background:#1a1d24;border-radius:16px;border:1px solid rgba(255,255,255,0.06);box-shadow:0 32px 80px rgba(0,0,0,0.6);overflow:hidden;">
		    <tr><td style="background:linear-gradient(135deg,#1a1d24 0%%,#252a35 100%%);padding:32px 40px;text-align:center;border-bottom:2px solid rgba(199,165,103,0.25);">
		      <p style="margin:0 0 6px;color:#c7a567;font-size:11px;font-weight:700;letter-spacing:0.16em;text-transform:uppercase;">Club de la Unión</p>
		      <h1 style="margin:0;color:#fff;font-size:22px;font-weight:700;letter-spacing:-0.02em;">Nuevo cargo registrado</h1>
		    </td></tr>
		    <tr><td style="padding:32px 40px 24px;">
		      <p style="margin:0 0 6px;color:rgba(255,255,255,0.9);font-size:16px;font-weight:600;">Hola, %s.</p>
		      <p style="margin:0 0 28px;color:rgba(255,255,255,0.5);font-size:14px;line-height:1.6;">
		        Se ha registrado un nuevo cargo a tu <span style="color:#c7a567;font-weight:600;">Acción N° %s</span>.
		      </p>
		      <div style="background:#12151b;border:1px solid rgba(255,255,255,0.08);border-radius:10px;padding:20px 24px;margin-bottom:16px;">
		        <p style="margin:0 0 14px;color:#c7a567;font-size:11px;font-weight:700;letter-spacing:0.12em;text-transform:uppercase;">Detalle del cargo</p>
		        <table width="100%%" cellpadding="0" cellspacing="0">
		          <tr><td style="padding:5px 0;color:rgba(255,255,255,0.45);font-size:13px;width:40%%;">Ambiente</td><td style="padding:5px 0;color:rgba(255,255,255,0.85);font-size:13px;font-weight:500;">%s</td></tr>
		          <tr><td style="padding:5px 0;color:rgba(255,255,255,0.45);font-size:13px;">Mesa</td><td style="padding:5px 0;color:rgba(255,255,255,0.85);font-size:13px;font-weight:500;">%s</td></tr>
		          <tr><td style="padding:5px 0;color:rgba(255,255,255,0.45);font-size:13px;">Apertura</td><td style="padding:5px 0;color:rgba(255,255,255,0.85);font-size:13px;font-weight:500;">%s</td></tr>
		          <tr><td style="padding:5px 0;color:rgba(255,255,255,0.45);font-size:13px;">Cierre</td><td style="padding:5px 0;color:rgba(255,255,255,0.85);font-size:13px;font-weight:500;">%s</td></tr>
		          <tr><td style="padding:5px 0;color:rgba(255,255,255,0.45);font-size:13px;">Mesero</td><td style="padding:5px 0;color:rgba(255,255,255,0.85);font-size:13px;font-weight:500;">%s</td></tr>
		        </table>
		      </div>
		      <div style="background:#12151b;border:1px solid rgba(255,255,255,0.08);border-radius:10px;padding:20px 24px;margin-bottom:28px;">
		        <p style="margin:0 0 14px;color:#c7a567;font-size:11px;font-weight:700;letter-spacing:0.12em;text-transform:uppercase;">Desglose</p>
		        <table width="100%%" cellpadding="0" cellspacing="0">
		          <tr><td style="padding:4px 0;color:rgba(255,255,255,0.45);font-size:13px;">Consumo</td><td style="padding:4px 0;color:rgba(255,255,255,0.75);font-size:13px;text-align:right;">%s</td></tr>
		          <tr><td style="padding:4px 0;color:rgba(255,255,255,0.45);font-size:13px;">IVA</td><td style="padding:4px 0;color:rgba(255,255,255,0.75);font-size:13px;text-align:right;">%s</td></tr>
		          <tr><td style="padding:4px 0;color:rgba(255,255,255,0.45);font-size:13px;">Servicio</td><td style="padding:4px 0;color:rgba(255,255,255,0.75);font-size:13px;text-align:right;">%s</td></tr>
		          <tr><td style="padding:4px 0;color:rgba(255,255,255,0.45);font-size:13px;">Propina</td><td style="padding:4px 0;color:rgba(255,255,255,0.75);font-size:13px;text-align:right;">%s</td></tr>
		          <tr><td colspan="2" style="padding-top:12px;border-top:1px solid rgba(255,255,255,0.1);"></td></tr>
		          <tr>
		            <td style="padding-top:8px;color:#fff;font-size:15px;font-weight:700;">Total</td>
		            <td style="padding-top:8px;color:#c7a567;font-size:18px;font-weight:700;text-align:right;">%s</td>
		          </tr>
		        </table>
		      </div>
		    </td></tr>
		    <tr><td style="padding:20px 40px 28px;border-top:1px solid rgba(255,255,255,0.06);text-align:center;">
		      <p style="margin:0 0 6px;color:rgba(255,255,255,0.25);font-size:12px;line-height:1.6;">Si tienes dudas sobre este cargo, comunícate directamente con el club.</p>
		      <p style="margin:0;color:rgba(255,255,255,0.15);font-size:11px;">No respondas este correo.</p>
		    </td></tr>
		    </table></td></tr></table></body></html>
		    """).formatted(firstName, shareNum,
		                   ambiente, mesa, apertura, cierre, mesero,
		                   fmtBase, fmtIva, fmtService, fmtTip, fmtTotal);
	}

	private String formatCurrency(Double value) {
		if (value == null) return "$0.00";
		return String.format(Locale.US, "$%,.2f", value);
	}

	private String escapeHtml(String text) {
		if (text == null)
			return "";
		return text.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
	}
}
