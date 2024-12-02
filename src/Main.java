import org.bson.Document;
import org.bson.conversions.Bson;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;
import com.mongodb.MongoException;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import com.mongodb.client.result.InsertManyResult;
import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import org.slf4j.LoggerFactory;


/*
 * EMAIL CREDENTIALS
 * Credentials:
 *	email: cs492finalproject123@gmail.com
 */

public class Main {
    public static void main(String[] args) throws Exception {
    	String uri = "mongodb+srv://cs492finalproject123:ZfIAfxrK8W6PvAcL@cs-492.burpl.mongodb.net/?retryWrites=true&w=majority&appName=CS-492";
    	Scanner scanner = new Scanner(System.in);
    	boolean userFound;
    	Document userDoc = null;
    	String userCollection = null;
    	String objectId;
    	String currentEncryptedUsername;
    	String currentEncryptedPassword;
    	String[] splitText;
    	String[] splitHolder;
    	EncryptionUtility encryptionUtil = new EncryptionUtility();
    	
    	/*
    	 * @author chneau on Stack Exchange
    	 * Original code found here: https://stackoverflow.com/a/40884256
    	 * Disables MongoDB logging showing up in console
    	 */
    	LoggerContext loggerContext = (LoggerContext) LoggerFactory.getILoggerFactory();
    	Logger rootLogger = loggerContext.getLogger("org.mongodb.driver");
    	rootLogger.setLevel(Level.OFF);	
    	
       // Welcome/login
    	System.out.println("Welcome to Password Storage Program!");
    	System.out.println("Please enter your email: ");
    	
    	// Get user's email associated with password storage service
    	String userEmail = scanner.nextLine();
    	
    	//Get user's password associated with password storage service
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
                 * Help from S Vinay Kumar on Stack Exchange
                 * Original code: https://stackoverflow.com/a/53963664
                 * Converts _id to String
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
        		// Access user's collection
        		try (MongoClient mongoClient = MongoClients.create(uri)) {
                    // Reference the database and collection to use
                    MongoDatabase database = mongoClient.getDatabase("cs492data");
                    MongoCollection<Document> collection = database.getCollection(userCollection);
                    /*
                     * @author Vladi on Stack Exchange
                     * Original code: https://stackoverflow.com/a/68610153
                     */
                    MongoCursor<Document> cursor = collection.find().iterator();
                    while (cursor.hasNext()) {
                    	System.out.println("Your data:");
                        System.out.println(cursor.next() );
                    }
        		}
        	} else {
        		// If codes don't match, stop program
        		System.out.println("Codes do not match, aborting process...");
        		System.exit(0);
        	}
    	}
    	
    	/*
    	 * add = Add new credentials
    	 * exit = Atop program
    	 */
    	System.out.println("LIST OF COMMANDS:\n add - Add new credentials\n exit - Quit the program");
		System.out.println("Please enter command: ");
    	String command = scanner.nextLine();
    	if (command.equals("exit")) {
    		System.out.println("Thank you for using Password Storage Program! Exiting...");
    		System.exit(0);
    	} else if (command.equals("add")) {
    		System.out.println("Which service are you making the credentials for?");
    		String serviceName = scanner.nextLine();
    		System.out.println("Please enter your username for " + serviceName);
    		String serviceUsername = scanner.nextLine();
    		// Encrypt username
    		splitText = serviceUsername.split("");
    		splitHolder = EncryptionUtility.cbcEncrypt(splitText, userCollection);
    		currentEncryptedUsername = String.join("", splitHolder);
    		
    		System.out.println("Please enter your password for " + serviceName);
    		String servicePassword = scanner.nextLine();
    		// Encrypt password
    		splitText = servicePassword.split("");
    		splitHolder = EncryptionUtility.cbcEncrypt(splitText, userCollection);
    		currentEncryptedPassword = String.join("", splitHolder);
    		// Establish connection again...
    		try (MongoClient mongoClient = MongoClients.create(uri)) {
                // Reference the database and collection to use
                MongoDatabase database = mongoClient.getDatabase("cs492data");
                MongoCollection<Document> collection = database.getCollection(userCollection);
                // Create two documents
                List<Document> userData = Arrays.asList(
                        new Document().append(serviceName, new Document().append("username", currentEncryptedUsername).append("password", currentEncryptedPassword))
                        );
                try {
                    // Insert the documents into the specified collection
                    InsertManyResult result = collection.insertMany(userData);
                } catch (MongoException me) {
                    System.err.println("Unable to insert due to an error: " + me);
                }
    		}
    	} else {
    		System.out.println("Not a recognized command!");
    	}
    }
}
