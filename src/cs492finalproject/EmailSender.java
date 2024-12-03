package cs492finalproject;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
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
	/**
     * Generates a random code to be sent to the user's email
     * @param candidateChars    The characters that can be used to create the code
     * @param length            lemgth of the codde
     * 
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
    
    public static void sendVerificationCode(String recipient, String code) throws MessagingException, IOException {
        /*
		 * @author Pankaj on Digital Ocean
		 * Original code here: https://www.digitalocean.com/community/tutorials/java-read-file-to-string
         * For getting application password so it isn't stored directly in code
		 */
		BufferedReader reader = new BufferedReader(new FileReader("src/assets/app_password")); 
		StringBuilder stringBuilder = new StringBuilder();
		String line = null;
		String ls = System.getProperty("line.separator");
		while ((line = reader.readLine()) != null) {
			stringBuilder.append(line);
			stringBuilder.append(ls);
		}
		// Delete the last new line separator
		stringBuilder.deleteCharAt(stringBuilder.length() - 1);
		reader.close();
		String content = stringBuilder.toString();

        String host = "smtp.gmail.com";
        String from = "cs492finalproject123@gmail.com";
        String password = content; // Use app-specific passwords if 2FA is enabled.

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