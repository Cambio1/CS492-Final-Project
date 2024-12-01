import org.bson.Document;
import org.bson.conversions.Bson;

import static com.mongodb.client.model.Filters.eq;

import java.util.Arrays;
import java.util.List;
import java.util.Scanner;
import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.MongoException;
import com.mongodb.ServerApi;
import com.mongodb.ServerApiVersion;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
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
 *	password: Xb~G;.n6?2#C3f5T[u4V&(
 */

public class Main {
    public static void main(String[] args) throws Exception {
    	String uri = "mongodb+srv://cs492finalproject123:ZfIAfxrK8W6PvAcL@cs-492.burpl.mongodb.net/?retryWrites=true&w=majority&appName=CS-492";
    	Scanner scanner = new Scanner(System.in);
    	boolean userFound;
    	
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
                userFound = false;
                System.out.println("ERROR ABORTING");
                System.exit(0);
            } else {
                userFound = true;
            }
        }
    	
    	// Send email for 2FA if there's a match!
    	if (userFound = true) {
    		String sentCode = EmailSender.generateCode("ABCDEFGHIJKLMNOPQRSTUVWXYZ1234567890", 10);
        	EmailSender.sendVerificationCode(userEmail, sentCode);
        	System.out.println("An email has been sent to you. Please enter the code sent: ");
        	String inputtedCode = scanner.nextLine();
        	if (sentCode.equals(inputtedCode)) {
        		System.out.println("Codes match!");
        	} else {
        		System.out.println("Codes do not match, aborting process...");
        		System.exit(0);
        	}
    	}
        
        //MongoDB stuff starts here; below is sample code taken from MongoDB
    	/*
        String connectionString = "mongodb+srv://cs492finalproject123:ZfIAfxrK8W6PvAcL@cs-492.burpl.mongodb.net/?retryWrites=true&w=majority&appName=CS-492";
        ServerApi serverApi = ServerApi.builder()
                .version(ServerApiVersion.V1)
                .build();
        MongoClientSettings settings = MongoClientSettings.builder()
                .applyConnectionString(new ConnectionString(connectionString))
                .serverApi(serverApi)
                .build();
        // Create a new client and connect to the server
        try (MongoClient mongoClient = MongoClients.create(settings)) {
            try {
                // Send a ping to confirm a successful connection
                MongoDatabase database = mongoClient.getDatabase("admin");
                database.runCommand(new Document("ping", 1));
                System.out.println("Pinged your deployment. You successfully connected to MongoDB!");
            } catch (MongoException e) {
                e.printStackTrace();
            }
        }*/
       
    	/*
        // Connect to your Atlas Cluster and insert a document
        try (MongoClient mongoClient = MongoClients.create(uri)) {
            // Reference the database and collection to use
            MongoDatabase database = mongoClient.getDatabase("cs492data");
            MongoCollection<Document> collection = database.getCollection("user_data");
            // Create two documents
            List<Document> userData = Arrays.asList(
                    new Document().append("x", new Document().append("username", "username1").append("password", "password1"))
                    );
            try {
                // Insert the documents into the specified collection
                InsertManyResult result = collection.insertMany(userData);
            } catch (MongoException me) {
                System.err.println("Unable to insert due to an error: " + me);
            }
            // Find the document
            Document document = collection.find(eq("x.username", "username1"))
                    .first();
            // Print results
            if (document == null) {
                System.out.println("No results found.");
            } else {
                System.out.println("Document found:");
                System.out.println(document.toJson());
            }
        
        }
        */
    }
}
