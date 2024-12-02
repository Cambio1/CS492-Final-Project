package cs492finalproject;

import java.util.Properties;
import java.util.Random;

import javax.mail.Authenticator;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

public class EmailSender {
	
	/*
	 * @author MaVRoSCy on Stack Exchange
	 * Code from here: https://stackoverflow.com/a/20536819
	 */
	public static String generateCode(String candidateChars, int length) {
        StringBuilder sb = new StringBuilder ();
        Random random = new Random ();
        for (int i = 0; i < length; i ++) {
            sb.append (candidateChars.charAt (random.nextInt (candidateChars
                    .length ())));
        }
        return sb.toString ();
	}
	
    public static void sendVerificationCode(String recipient, String code) throws MessagingException {
        String host = "smtp.gmail.com";
        String from = "cs492finalproject123@gmail.com";
        String password = "ipqejohmvzlbaxrz"; // Use app-specific passwords if 2FA is enabled.
        //ipqe johm vzlb axrz
        

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