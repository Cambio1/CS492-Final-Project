import javax.mail.*;
import javax.mail.internet.*;
import java.util.Properties;

public class EmailSender {
    public static void sendVerificationCode(String recipient, String code) throws MessagingException {
        String host = "smtp.gmail.com";
        String from = "your-email@gmail.com";
        String password = "your-email-password"; // Use app-specific passwords if 2FA is enabled.

        Properties props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host", host);
        props.put("mail.smtp.port", "587");

        Session session = Session.getInstance(props, new Authenticator() {
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(from, password);
            }
        });

        Message message = new MimeMessage(session);
        message.setFrom(new InternetAddress(from));
        message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(recipient));
        message.setSubject("Your Verification Code");
        message.setText("Your verification code is: " + code);

        Transport.send(message);
    }
}
