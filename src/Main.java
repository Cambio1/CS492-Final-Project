import javax.mail.MessagingException;

public class Main {
    public static void main(String[] args) throws Exception {
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
    }
}
