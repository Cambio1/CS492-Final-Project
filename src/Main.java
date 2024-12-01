import org.bson.Document;
import java.util.Scanner;
import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.MongoException;
import com.mongodb.ServerApi;
import com.mongodb.ServerApiVersion;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoDatabase;

public class Main {
    public static void main(String[] args) throws Exception {
    	Scanner scanner = new Scanner(System.in);
    	
       // Welcome/login
    	System.out.println("Welcome to Password Storage Program!");
    	System.out.println("Please enter your email: ");
    	
    	// Get user's email associated with password storage service
    	String userEmail = scanner.nextLine();  // Read user input
    	
    	//Get user's password associated with password storage service
    	System.out.println("Please enter your password: ");
    	String userPassword = scanner.nextLine();  // Read user input
    	
    	// Check if credentials match any user data
    	
    	// Send email for 2FA if there's a match!
    	String sentCode = EmailSender.generateCode("ABCDEFGHIJKLMNOPQRSTUVWXYZ1234567890", 10);
    	EmailSender.sendVerificationCode("cs492finalproject123@gmail.com", sentCode);
    	System.out.println("An email has been sent to you. Please enter the code sent: ");
    	String inputtedCode = scanner.nextLine();
    	if (sentCode.equals(inputtedCode)) {
    		System.out.println("Codes match!");
    	} else {
    		System.out.println("Code does not match, aborting process...");
    		System.exit(0);
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
       
        
        
    }
}
