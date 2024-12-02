import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Base64;

import javax.crypto.Cipher;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;

public class EncryptionUtility {
    static String thisSalt;
    static String thisIv;
    
    // Function to generate a random IV (16 bytes)
    private static byte[] generateIV() {
        SecureRandom random = new SecureRandom();
        byte[] iv = new byte[16];  // AES block size is 16 bytes
        random.nextBytes(iv);
        return iv;
    }

    // Function to encrypt data using AES CBC mode
    public static String[] cbcEncrypt(String[] msg, String password) throws Exception {
        // Generate a random IV for each encryption process
        byte[] iv = generateIV();
        
        // Convert IV to Base64 for easy transmission/storage
        String ivBase64 = Base64.getEncoder().encodeToString(iv);
        
        // Generate a salt
        byte[] salt = generateSalt(16);
        
        // Derive the AES key from the password and salt
        SecretKeySpec key = deriveKey(password, salt);
        
        // Initialize AES cipher in CBC mode
        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        IvParameterSpec ivSpec = new IvParameterSpec(iv);
        cipher.init(Cipher.ENCRYPT_MODE, key, ivSpec);
        
        // List to store encrypted message blocks
        ArrayList<String> encryptedMsg = new ArrayList<>();
        
        // Encrypt each block of the message
        for (String block : msg) {
            byte[] encryptedBlock = cipher.doFinal(block.getBytes(StandardCharsets.UTF_8));
            String encryptedBlockBase64 = Base64.getEncoder().encodeToString(encryptedBlock);
            encryptedMsg.add(encryptedBlockBase64);
        }
        
        // Base64 encode the salt
        String encodedSalt = encodeBase64(salt);
        thisSalt = encodedSalt;
        thisIv = ivBase64;
        
        
        /*
        // Combine IV, encrypted message, and salt into a single message for transmission
        System.out.println("IV (Base64 encoded): " + ivBase64);  // Print the IV (for demonstration)
        System.out.println("Salt (Base64 encoded): " + encodedSalt);  // Print the salt
        */
        
        // Return the encrypted message along with the IV and salt (Base64 encoded)
        return encryptedMsg.toArray(new String[0]);
    }

    // Function to decrypt data using AES CBC mode
    public static String[] cbcDecrypt(String[] encryptedMsg, String password, String encodedIV, String encodedSalt) throws Exception {
        // Decode the Base64 encoded IV and salt
        byte[] iv = Base64.getDecoder().decode(encodedIV);
        byte[] salt = decodeBase64(encodedSalt);
        
        // Derive the AES key from the password and salt
        SecretKeySpec key = deriveKey(password, salt);
        
        // Initialize AES cipher in CBC mode
        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        IvParameterSpec ivSpec = new IvParameterSpec(iv);
        cipher.init(Cipher.DECRYPT_MODE, key, ivSpec);
        
        // List to store decrypted message blocks
        ArrayList<String> decryptedMsg = new ArrayList<>();
        
        // Decrypt each encrypted block
        for (String encryptedBlock : encryptedMsg) {
            byte[] decodedBlock = Base64.getDecoder().decode(encryptedBlock);
            byte[] decryptedBlock = cipher.doFinal(decodedBlock);
            String decryptedString = new String(decryptedBlock, StandardCharsets.UTF_8);
            decryptedMsg.add(decryptedString);
        }
        
        // Convert the decrypted message to an array of strings
        return decryptedMsg.toArray(new String[0]);
    }

    // Helper function to generate a random salt (16 bytes)
    private static byte[] generateSalt(int length) {
        SecureRandom r = new SecureRandom();
        byte[] salt = new byte[length];
        r.nextBytes(salt);
        return salt;
    }

    // Function to derive AES key from password and salt using PBKDF2
    public static SecretKeySpec deriveKey(String password, byte[] salt) throws Exception {
        PBEKeySpec spec = new PBEKeySpec(password.toCharArray(), salt, 65536, 256);
        SecretKeyFactory keyFactory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
        byte[] key = keyFactory.generateSecret(spec).getEncoded();
        return new SecretKeySpec(key, "AES");
    }

    // Function to convert byte array to Base64 string
    public static String encodeBase64(byte[] data) {
        return Base64.getEncoder().encodeToString(data);
    }

    // Function to convert Base64 string to byte array
    public static byte[] decodeBase64(String data) {
        return Base64.getDecoder().decode(data);
    }

    // Helper function to convert hex string to byte array
    public static byte[] convertFromHex(String hex) {
        int length = hex.length();
        byte[] result = new byte[length / 2];
        for (int i = 0; i < length; i += 2) {
            result[i / 2] = (byte) ((Character.digit(hex.charAt(i), 16) << 4)
                    + Character.digit(hex.charAt(i + 1), 16));
        }
        return result;
    }
    
    public static String getSalt() {
    	return thisSalt;
    }
    
    public static String getIv() {
    	return thisIv;
    }
}
