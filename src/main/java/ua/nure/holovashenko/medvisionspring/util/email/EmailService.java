package ua.nure.holovashenko.medvisionspring.util.email;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.ClassPathResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import java.io.UnsupportedEncodingException;

@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;

    public void sendDoctorCredentials(String to, String doctorName, String password) {
        String subject = "Ваш акаунт лікаря створено";

        String htmlContent = """
            <html>
            <body style="font-family: 'Segoe UI', Tahoma, sans-serif; background-color: #f4f4f4; padding: 30px;">
                <div style="max-width: 600px; margin: auto; background: white; padding: 30px; border-radius: 8px; box-shadow: 0 4px 12px rgba(0,0,0,0.05);">
                    <div style="text-align: center; margin-bottom: 20px;">
                        <img src='cid:medvision' alt='MedVision Logo' style='height: 64px;'/>
                    </div>
            
                    <h2 style="color: #333;">Вітаємо, %s!</h2>
            
                    <p style="font-size: 15px; color: #555;">
                        Вас було успішно зареєстровано як лікаря в системі <strong>MedVision</strong>.
                    </p>
            
                    <p style="font-size: 15px; color: #555; margin-top: 20px;">
                        <strong>Ваші облікові дані для входу:</strong>
                    </p>
            
                    <table style="width: 100%%; font-size: 15px; color: #333; background-color: #f9f9f9; border-collapse: collapse; margin-top: 10px;">
                        <tr>
                            <td style="padding: 10px; font-weight: bold;">Email:</td>
                            <td style="padding: 10px;">%s</td>
                        </tr>
                        <tr>
                            <td style="padding: 10px; font-weight: bold;">Тимчасовий пароль:</td>
                            <td style="padding: 10px;">%s</td>
                        </tr>
                    </table>
            
                    <p style="font-size: 14px; color: #777; margin-top: 20px;">
                        Будь ласка, змініть пароль після першого входу для забезпечення безпеки вашого облікового запису.
                    </p>
            
                    <hr style="margin: 30px 0; border: none; border-top: 1px solid #eee;"/>
            
                    <p style="font-size: 14px; color: #777;">
                        З повагою,<br/>
                        <strong>Команда MedVision</strong>
                    </p>
                </div>
            </body>
            </html>
            """.formatted(doctorName, to, password);


        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setTo(to);
            helper.setSubject(subject);
            helper.setFrom("illia.holovashenko@nure.ua", "MedVision Support");
            helper.setText(htmlContent, true);

            // Додаємо іконку як inline-ресурс
            ClassPathResource logo = new ClassPathResource("static/medvision.png");
            helper.addInline("medvision", logo);

            mailSender.send(message);
        } catch (MessagingException | UnsupportedEncodingException e) {
            throw new RuntimeException("Не вдалося надіслати листа", e);
        }
    }
}
