package cs492finalproject;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;

import org.bson.Document;
import org.bson.conversions.Bson;
import org.slf4j.LoggerFactory;

import com.mongodb.MongoException;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import com.mongodb.client.result.InsertManyResult;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;

/*
 * EMAIL CREDENTIALS
 * Credentials:
 *	email: cs492finalproject123@gmail.com
 */

public class Main {

	public static void main(String[] args) throws Exception {

		/*
		 * @author Pankaj on Digital Ocean
		 * Original code here: https://www.digitalocean.com/community/tutorials/java-read-file-to-string
		 */
		BufferedReader reader = new BufferedReader(new FileReader("src/assets/uri"));
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

		String uri = content;
		Scanner scanner = new Scanner(System.in);
		boolean userFound;
		Document userDoc = null;
		String userCollection = null;
		String objectId;
		String currentEncryptedUsername;
		String currentEncryptedPassword;
		String[] userText;
		String[] splitHolder;
		EncryptionUtility encryptionUtil = new EncryptionUtility();
		String decryptedText;
		String userSalt = null;
		String userIv = null;
		String passwordSalt;
		String passwordIv;
		String tempString;

		/*
		 * @author chneau on Stack Exchange Original code found here:
		 * https://stackoverflow.com/a/40884256 Disables MongoDB logging showing up in
		 * console
		 */
		LoggerContext loggerContext = (LoggerContext) LoggerFactory.getILoggerFactory();
		Logger rootLogger = loggerContext.getLogger("org.mongodb.driver");
		rootLogger.setLevel(Level.OFF);

		// Welcome/login
		System.out.println("Welcome to Password Storage Program!");

		// Is it a registered user?
		System.out.println("Would you like to log in or register?");


		System.out.println("Please enter your email: ");

		// Get user's email associated with password storage service
		String userEmail = scanner.nextLine();

		// Get user's password associated with password storage service
		System.out.println("Please enter your password: ");
		String userPassword = scanner.nextLine();

		// Check if credentials match any user data
		Bson filter = Filters.and(Filters.eq("root_email", userEmail), Filters.eq("root_password", userPassword));
		try (MongoClient mongoClient = MongoClients.create(uri)) {
			// Reference the database and collection to use
			MongoDatabase database = mongoClient.getDatabase("cs492data");
			MongoCollection<Document> collection = database.getCollection("user_data");
			// Find if email entered matches any document
			Document document = collection.find(filter).first();
			if (document == null) {
				userFound = false; // If no match, abort
				System.out.println("ERROR ABORTING");
				System.exit(0);
			} else {
				userFound = true;
				userDoc = document;
				/*
				 * Help from S Vinay Kumar on Stack Exchange Original code:
				 * https://stackoverflow.com/a/53963664 Converts _id to String
				 */
				userCollection = document.get("_id").toString();
			}
		}

		// Send email for 2FA if there's a match!
		if (userFound = true) {
			String sentCode = EmailSender.generateCode("ABCDEFGHIJKLMNOPQRSTUVWXYZ1234567890", 10);
			EmailSender.sendVerificationCode(userEmail, sentCode);
			System.out.println("An email has been sent to you. Please enter the code sent: ");
			String inputtedCode = scanner.nextLine();
			// If codes match
			if (sentCode.equals(inputtedCode)) {
				System.out.println("Codes match!");
			}
		} else {
			// If codes don't match, stop program
			System.out.println("Codes do not match, aborting process...");
			System.exit(0);
		}

		/*
		 * get = View credentials 
		 * add = Add new credentials 
		 * exit = Stop program
		 */
		System.out.println("""
                                   LIST OF COMMANDS:
                                   get - View credentials 
                                   add - Add new credentials
                                   exit - Quit the program""");
		System.out.println("Please enter command: ");
		String command = scanner.nextLine();
		// exit
		if (command.equals("exit")) {
			System.out.println("Thank you for using Password Storage Program! Exiting...");
			System.exit(0);
			// get
		} else if (command.equals("get")) {
			// Access user's collection
			try (MongoClient mongoClient = MongoClients.create(uri)) {
				// Reference the database and collection to use
				MongoDatabase database = mongoClient.getDatabase("cs492data");
				MongoCollection<Document> collection = database.getCollection(userCollection);
				System.out.println("Which service would you like the credentials of?");
				String desiredServiceName = scanner.nextLine();

				// Retrieves first document with matching service name
				Document foundDoc = collection.find(Filters.eq("service_name", desiredServiceName)).first();

				
				// Print out found document
				if (foundDoc == null) {
					System.out.println("Looks like you don't have credentials for that service!");
				} else {
					System.out.println(foundDoc);
					
					// Decrypt username
					String encryptedUsername = foundDoc.get("username").toString();
					userIv = foundDoc.get("user_iv").toString();
					userSalt = foundDoc.get("user_salt").toString();
					splitHolder = EncryptionUtility.cbcDecrypt(new String[]{encryptedUsername}, userPassword, userIv, userSalt);
					tempString = splitHolder[0];
					System.out.println("User: " + tempString);
					
					// Decrypt password (if needed, similar to username)
					String encryptedPassword = foundDoc.get("password").toString();
					passwordIv = foundDoc.get("password_iv").toString();
					passwordSalt = foundDoc.get("password_salt").toString();
					splitHolder = EncryptionUtility.cbcDecrypt(new String[]{encryptedPassword}, userPassword, passwordIv, passwordSalt);
					String decryptedPassword = splitHolder[0];
					System.out.println("Password: " + decryptedPassword);
				}
			}
			// add
		} else if (command.equals("add")) {
			System.out.println("Which service are you making the credentials for?");
			String serviceName = scanner.nextLine();
			System.out.println("Please enter your username for " + serviceName);
			String serviceUsername = scanner.nextLine();
			// Encrypt username
			userText = new String[]{serviceUsername}; 
			EncryptionUtility.EncryptedData encryptedData = EncryptionUtility.cbcEncrypt(userText, userPassword);
			userIv = encryptedData.getIv();
			userSalt = encryptedData.getSalt();
			currentEncryptedUsername = encryptedData.getEncryptedMessage()[0];


			System.out.println("Please enter your password for " + serviceName);
			String servicePassword = scanner.nextLine();
			// Encrypt password
			userText = new String[]{servicePassword};
			EncryptionUtility.EncryptedData encryptedData1 = EncryptionUtility.cbcEncrypt(userText, userPassword);
			passwordIv = encryptedData1.getIv();
			passwordSalt = encryptedData1.getSalt();
			currentEncryptedPassword = String.join("", encryptedData1.getEncryptedMessage());
			// Establish connection again...
			try (MongoClient mongoClient = MongoClients.create(uri)) {
				// Reference the database and collection to use
				MongoDatabase database = mongoClient.getDatabase("cs492data");
				MongoCollection<Document> collection = database.getCollection(userCollection);
				// Create document
				List<Document> userData = Arrays.asList(new Document().append("service_name", serviceName)
						.append("username", currentEncryptedUsername).append("password", currentEncryptedPassword)
						.append("user_iv", userIv).append("user_salt", userSalt).append("password_iv", passwordIv)
						.append("password_salt", passwordSalt));
				try {
					// Insert the documents into the specified collection
					InsertManyResult result = collection.insertMany(userData);
					System.out.println("Successfully added credentials!");
				} catch (MongoException me) {
					System.err.println("Unable to add credentials due to an error: " + me);
				}
			}
		} else {
			System.out.println("Not a recognized command!");
		}
	}
}
