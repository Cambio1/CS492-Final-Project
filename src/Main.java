//import javax.mail.MessagingException;
import org.bson.Document;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.MongoClientURI;
import com.mongodb.MongoException;
import com.mongodb.ServerApi;
import com.mongodb.ServerApiVersion;

public class Main {
    public static void main(String[] args) throws Exception {
    	/*
        // Step 1: Initialize Firebase
        FirebaseInitializer.initialize();

        // Step 2: Generate a verification code and send it via email
        String email = "user@example.com"; // Replace with user's email
        String verificationCode = String.valueOf((int) (Math.random() * 900000) + 100000);
        EmailSender.sendVerificationCode(email, verificationCode);
        System.out.println("Verification code sent to: " + email);

        // Simulate verification process
        String inputCode = "123456"; // Replace with user's input
        if (!inputCode.equals(verificationCode)) {
            System.out.println("Invalid verification code.");
            return;
        }

        // Step 3: Encrypt and store credentials
        String userId = "user123";
        String site = "example.com";
        String password = "mypassword";
        String encryptionKey = EncryptionUtility.generateKey();
        String encryptedPassword = EncryptionUtility.encrypt(password, encryptionKey);

        FirebasePasswordManager passwordManager = new FirebasePasswordManager();
        passwordManager.storeCredentials(userId, site, encryptedPassword);
        System.out.println("Credentials stored successfully.");

        // Step 4: Retrieve and decrypt credentials
        passwordManager.retrieveCredentials(userId, site, new FirebasePasswordManager.Callback<>() {
            @Override
            public void onSuccess(String result) {
                try {
                    String decryptedPassword = EncryptionUtility.decrypt(result, encryptionKey);
                    System.out.println("Decrypted Password: " + decryptedPassword);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(Exception e) {
                e.printStackTrace();
            }
        });
        */
        
        //MongoDB stuff starts here; below is sample code taken from MongoDB
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
        }
        
    }
}
