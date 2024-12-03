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
public class Main {

    public static void main(String[] args) throws Exception {
        /*
		 * @author Pankaj on Digital Ocean
		 * Original code here: https://www.digitalocean.com/community/tutorials/java-read-file-to-string
		 * For URI access so it isn't stored directly in code
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
		
        String uri = content;						// needed to connect to database

        String initialInput;						// log in, register, or something invalid?
        Scanner scanner = new Scanner(System.in);
        boolean userFound;							// does the user's email exist in the database?
        Document userDoc = null;					// current user's document in user_data
        String userCollection = null;				// user's collection of credentials
        String currentEncryptedUsername;
        String currentEncryptedPassword;
        String[] userText;							// temp variables used to store text when encrypting/decrypting
        String[] splitHolder;						
        EncryptionUtility encryptionUtil = new EncryptionUtility();
        String userSalt = null;						// used when salt is generated for usernames
        String userIv = null;						// used when iv is generated for usernames
        String passwordSalt;						// generated salt for password. initialized when retrieving data
        String passwordIv;							// generated iv for password. initialized when retrieving data
        String tempString;
        String userEmail;							// "root" username
        String userPassword = null;					// "root" password
        String command = null;						// add, get, exit after logging in

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
        System.out.println("Would you like to 'log in' or 'register?'");
        initialInput = scanner.nextLine();
		// when user registers
        if (initialInput.equals("register")) {
            System.out.println("Great, please enter your email: ");
            userEmail = scanner.nextLine();
            System.out.println("Please enter a password: ");
            userPassword = scanner.nextLine();
            String sentCode = EmailSender.generateCode("ABCDEFGHIJKLMNOPQRSTUVWXYZ1234567890", 10);
            EmailSender.sendVerificationCode(userEmail, sentCode);
            System.out.println("An email has been sent to you. Please enter the code sent: ");
            String inputtedCode = scanner.nextLine();
            // If codes match
            if (sentCode.equals(inputtedCode)) {
                System.out.println("Codes match!");
                // Encrypt password
                userText = new String[]{userPassword};
                EncryptionUtility.EncryptedData encryptedData1 = EncryptionUtility.cbcEncrypt(userText, userPassword);
                passwordIv = encryptedData1.getIv();
                passwordSalt = encryptedData1.getSalt();
                currentEncryptedPassword = String.join("", encryptedData1.getEncryptedMessage());

                // Establish connection again...
                try (MongoClient mongoClient = MongoClients.create(uri)) {
                    // Reference the database and collection to use
                    MongoDatabase database = mongoClient.getDatabase("cs492data");
                    MongoCollection<Document> collection = database.getCollection("user_data");
                    // Create document
                    List<Document> userData = Arrays.asList(new Document()
                            .append("root_email", userEmail).append("root_password", currentEncryptedPassword)
                            .append("password_iv", passwordIv).append("password_salt", passwordSalt));
                    try {
                        // Insert the documents into the specified collection
                        InsertManyResult result = collection.insertMany(userData);
                        Bson filter = Filters.and(Filters.eq("root_email", userEmail), Filters.eq("root_password", currentEncryptedPassword));
                        Document document = collection.find(filter).first();
                        userCollection = document.get("_id").toString();
                        database.createCollection(userCollection);
                        System.out.println("Successfully registered! Please login with those credentials.");
                    } catch (MongoException me) {
                        System.err.println("Unable to register due to an error: " + me);
                    }
                }
            } else {
                // If codes don't match, ask again
                System.out.println("Codes do not match. Want to try again?");
            }
		// when user wants to log in
        } else if (initialInput.equals("log in")) {
            System.out.println("Please enter your email: ");

            // Get user's email associated with password storage service
            userEmail = scanner.nextLine();

            // Get user's password associated with password storage service
            System.out.println("Please enter your password: ");
            userPassword = scanner.nextLine();

            // Check if credentials match any user data
            // Decrypt until match is found
            // Establish connection
            try (MongoClient mongoClient = MongoClients.create(uri)) {
                // Assign entered email to filer
                Bson tempFilter = Filters.and(Filters.eq("root_email", userEmail));

                // Get collection
                MongoDatabase database = mongoClient.getDatabase("cs492data");
                MongoCollection<Document> collection = database.getCollection("user_data");

                // Find doc with email
                Document tempDocument = collection.find(tempFilter).first();

                // Decrypt password
                String encryptedPassword = tempDocument.get("root_password").toString();
                passwordIv = tempDocument.get("password_iv").toString();
                passwordSalt = tempDocument.get("password_salt").toString();
                splitHolder = EncryptionUtility.cbcDecrypt(new String[]{encryptedPassword}, userPassword, passwordIv, passwordSalt);
                String decryptedPassword = splitHolder[0];

				// if password is valid, preemptively get unique id value
                if (decryptedPassword.equals(userPassword)) {
                    userFound = true;
                    userDoc = tempDocument;
                    /*
				 * Help from S Vinay Kumar on Stack Exchange Original code:
				 * https://stackoverflow.com/a/53963664 Converts _id to String
                     */
                    userCollection = tempDocument.get("_id").toString();
                } else {
                    userFound = false; // If no match, abort
                    System.out.println("ERROR ABORTING");
                    System.exit(0);
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
            }
			// what the user can do once they log in
        	/*
		 	* get = View credentials 
		 	* add = Add new credentials 
		 	* exit = Stop program
             */
            while (true) {
                System.out.println("""
                                   LIST OF COMMANDS:
                                   get - View credentials 
                                   add - Add new credentials
                                   exit - Quit the program""");
                System.out.println("Please enter command: ");
                command = scanner.nextLine();
				// if user's input doesn't match a command, have them try again
                while (true) {
                    if (command.equals("get") == true || command.equals("add") == true || command.equals("exit") == true) {
                        break;
                    } else {
                        System.out.println("Not a recognized command, please try again!");
                        command = scanner.nextLine();
                        continue;
                    }
                }
                // exit
                if (command.equals("exit")) {
                    System.out.println("Thank you for using Password Storage Program! Exiting...");
                    System.exit(0);
                    break;
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

                            // Decrypt requested username and display
                            String encryptedUsername = foundDoc.get("username").toString();
                            userIv = foundDoc.get("user_iv").toString();
                            userSalt = foundDoc.get("user_salt").toString();
                            splitHolder = EncryptionUtility.cbcDecrypt(new String[]{encryptedUsername}, userPassword, userIv, userSalt);
                            tempString = splitHolder[0];
                            System.out.println("User: " + tempString);

                            // Decrypt requested password and display
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
					// get name of service
                    System.out.println("Which service are you making the credentials for?");
                    String serviceName = scanner.nextLine();
					// get username to encrypt and store
                    System.out.println("Please enter your username for " + serviceName);
                    String serviceUsername = scanner.nextLine();
                    // Encrypt username
                    userText = new String[]{serviceUsername};
                    EncryptionUtility.EncryptedData encryptedData = EncryptionUtility.cbcEncrypt(userText, userPassword);
                    userIv = encryptedData.getIv();
                    userSalt = encryptedData.getSalt();
                    currentEncryptedUsername = encryptedData.getEncryptedMessage()[0];
					//get password to encrypt and store
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
                        // Create document with encrypted credentials
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
					// when command (add, get, exit) is invalid
                    System.out.println("Not a recognized command! Please try again!");
                }
            }
        } else {
			// when input != 'log in' or 'register', stop
            System.out.println("Not a recognized command! Closing program...");
        }
    }
}
