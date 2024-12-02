package cs492finalproject;

import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Base64;

import javax.crypto.Cipher;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;

/**
 * Utility class providing AES encryption and decryption functionality using CBC mode.
 * Implements password-based encryption with salt and IV for secure data protection.
 */
public class EncryptionUtility {
    
    /**
     * Container class for encrypted data including the encrypted message, initialization vector, and salt.
     */
    public static class EncryptedData {
        private final String[] encryptedMessage;
        private final String iv;
        private final String salt;
        
        /**
         * Constructs a new EncryptedData object.
         *
         * @param encryptedMessage      Array of encrypted message blocks in Base64 format
         * @param iv                    Base64-encoded initialization vector
         * @param salt                  Base64-encoded salt value
         */
        public EncryptedData(String[] encryptedMessage, String iv, String salt) {
            this.encryptedMessage = encryptedMessage;
            this.iv = iv;
            this.salt = salt;
        }
        
        /**
         * Gets the encrypted message blocks.
         *
         * @return Array of Base64-encoded encrypted message blocks
         */
        public String[] getEncryptedMessage() { 
            return encryptedMessage; 
        }

        /**
         * Gets the initialization vector.
         *
         * @return Base64-encoded initialization vector
         */
        public String getIv() { 
            return iv; 
        }

        /**
         * Gets the salt value.
         *
         * @return Base64-encoded salt
         */
        public String getSalt() { 
            return salt; 
        }
    }


    /**
     * Encrypts an array of strings using AES in CBC mode with password-based key derivation.
     *
     * @param msg                       Array of strings to encrypt
     * @param password                  Password used for key derivation
     * @return                          EncryptedData object containing the encrypted message, IV, and salt
     * @throws EncryptionException      if encryption fails
     */
    public static EncryptedData cbcEncrypt(String[] msg, String password) throws EncryptionException {
        try {
            byte[] iv = generateIV();
            String ivBase64 = Base64.getEncoder().encodeToString(iv);
            
            byte[] salt = generateSalt(16);
            String saltBase64 = Base64.getEncoder().encodeToString(salt);
            
            SecretKeySpec key = deriveKey(password, salt);
            
            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            IvParameterSpec ivSpec = new IvParameterSpec(iv);
            cipher.init(Cipher.ENCRYPT_MODE, key, ivSpec);
            
            ArrayList<String> encryptedMsg = new ArrayList<>();
            
            for (String block : msg) {
                byte[] encryptedBlock = cipher.doFinal(block.getBytes(StandardCharsets.UTF_8));
                String encryptedBlockBase64 = Base64.getEncoder().encodeToString(encryptedBlock);
                encryptedMsg.add(encryptedBlockBase64);
            }
            
            return new EncryptedData(
                encryptedMsg.toArray(new String[0]),
                ivBase64,
                saltBase64
            );
        } catch (Exception e) {
            throw new EncryptionException("Failed to encrypt data", e);
        }
    }

    /**
     * Decrypts an array of encrypted strings using AES in CBC mode.
     *
     * @param encryptedMsg      Array of Base64-encoded encrypted strings
     * @param password          Password used for key derivation
     * @param encodedIV         Base64-encoded initialization vector
     * @param encodedSalt       Base64-encoded salt
     * @return                  Array of decrypted strings
     * @throws Exception        if decryption fails
     */
    public static String[] cbcDecrypt(String[] encryptedMsg, String password, String encodedIV, String encodedSalt) throws Exception {
        // Normalize and decode IV and salt
        byte[] iv = Base64.getDecoder().decode(normalizeBase64(encodedIV));
        byte[] salt = Base64.getDecoder().decode(normalizeBase64(encodedSalt));
        
        // Derive key
        SecretKeySpec key = deriveKey(password, salt);
    
        // Initialize Cipher
        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        IvParameterSpec ivSpec = new IvParameterSpec(iv);
        cipher.init(Cipher.DECRYPT_MODE, key, ivSpec);
    
        // Decrypt each block
        ArrayList<String> decryptedMsg = new ArrayList<>();
        for (String encryptedBlock : encryptedMsg) {
            byte[] decodedBlock = Base64.getDecoder().decode(normalizeBase64(encryptedBlock));
            byte[] decryptedBlock = cipher.doFinal(decodedBlock);
            decryptedMsg.add(new String(decryptedBlock, StandardCharsets.UTF_8));
        }
    
        return decryptedMsg.toArray(new String[0]);
    }
    

    /**
     * Decrypts encrypted data using the provided password.
     *
     * @param encryptedData     EncryptedData object containing the encrypted message, IV, and salt
     * @param password          Password used for key derivation
     * @return                  Array of decrypted strings
     * @throws Exception        if decryption fails
     */
    public static String[] cbcDecrypt(EncryptedData encryptedData, String password) throws Exception {
        return cbcDecrypt(
            encryptedData.getEncryptedMessage(),
            password,
            encryptedData.getIv(),
            encryptedData.getSalt()
        );
    }

    /**
     * Generates a random 16-byte initialization vector for AES encryption.
     *
     * @return byte array containing the random IV
     */
    private static byte[] generateIV() {
        SecureRandom random = new SecureRandom();
        byte[] iv = new byte[16];  // AES block size is 16 bytes
        random.nextBytes(iv);
        return iv;
    }

    /**
     * Generates a random salt of specified length for key derivation.
     *
     * @param length    Length of the salt in bytes
     * @return          byte array containing the random salt
     */
    private static byte[] generateSalt(int length) {
        SecureRandom r = new SecureRandom();
        byte[] salt = new byte[length];
        r.nextBytes(salt);
        return salt;
    }

    /**
     * Derives an AES key from a password and salt using PBKDF2 with HMAC-SHA256.
     * Uses 65536 iterations and generates a 256-bit key.
     *
     * @param password      Password to derive key from
     * @param salt          Salt for key derivation
     * @return              SecretKeySpec for AES encryption
     * @throws Exception    if key derivation fails
     */
    public static SecretKeySpec deriveKey(String password, byte[] salt) throws Exception {
        PBEKeySpec spec = new PBEKeySpec(password.toCharArray(), salt, 65536, 256);
        SecretKeyFactory keyFactory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
        byte[] key = keyFactory.generateSecret(spec).getEncoded();
        return new SecretKeySpec(key, "AES");
    }

    /**
     * Encodes a byte array to Base64 string.
     *
     * @param data      Byte array to encode
     * @return          Base64-encoded string
     */
    public static String encodeBase64(byte[] data) {
        return Base64.getEncoder().encodeToString(data);
    }

    /**
     * Decodes a Base64 string to byte array.
     *
     * @param data      Base64-encoded string
     * @return          Decoded byte array
     */
    public static byte[] decodeBase64(String data) {
        return Base64.getDecoder().decode(data);
    }

    /**
    * Validates and adds necessary padding to a Base64 string.
    *
    * @param base64String   Base64 string to validate and pad
    * @return               Padded Base64 string
    */
    private static String normalizeBase64(String base64String) {
        if (base64String == null) {
            throw new IllegalArgumentException("Base64 string is null");
        }
        // Add padding if necessary
        return base64String + "=".repeat((4 - base64String.length() % 4) % 4);
    }


    /**
     * Converts a hexadecimal string to byte array.
     *
     * @param hex Hexadecimal string
     * @return Byte array representation
     */
    public static byte[] convertFromHex(String hex) {
        int length = hex.length();
        byte[] result = new byte[length / 2];
        for (int i = 0; i < length; i += 2) {
            result[i / 2] = (byte) ((Character.digit(hex.charAt(i), 16) << 4)
                    + Character.digit(hex.charAt(i + 1), 16));
        }
        return result;
    }

    /**
     * Custom exception class for encryption-related errors.
     */
    public static class EncryptionException extends Exception {
        public EncryptionException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}
