package com.creative.spark.util;

import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.util.Base64;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

public class SecurityManager {
    
    private static final String ENCRYPTION_ALGORITHM = "PBKDF2WithHmacSHA256";
    private static final int SALT_BYTE_SIZE = 32;
    private static final int HASH_BYTE_SIZE = 32;
    private static final int PBKDF2_ITERATIONS = 12000;
    private static final String DELIMITER = ":";
    
    public static String generateSecureHash(String plainPassword) {
        try {
            SecureRandom randomGenerator = new SecureRandom();
            byte[] saltBytes = new byte[SALT_BYTE_SIZE];
            randomGenerator.nextBytes(saltBytes);
            
            byte[] hashBytes = performPBKDF2(plainPassword.toCharArray(), saltBytes, PBKDF2_ITERATIONS, HASH_BYTE_SIZE);
            
            String encodedSalt = Base64.getEncoder().encodeToString(saltBytes);
            String encodedHash = Base64.getEncoder().encodeToString(hashBytes);
            
            return PBKDF2_ITERATIONS + DELIMITER + encodedSalt + DELIMITER + encodedHash;
            
        } catch (Exception exception) {
            throw new RuntimeException("Security error: Unable to generate password hash", exception);
        }
    }
    
    public static boolean validatePassword(String plainPassword, String storedHashString) {
        try {
            String[] hashComponents = storedHashString.split(DELIMITER);
            if (hashComponents.length != 3) {
                return false;
            }
            
            int storedIterations = Integer.parseInt(hashComponents[0]);
            byte[] storedSalt = Base64.getDecoder().decode(hashComponents[1]);
            byte[] storedHash = Base64.getDecoder().decode(hashComponents[2]);
            
            byte[] computedHash = performPBKDF2(plainPassword.toCharArray(), storedSalt, storedIterations, storedHash.length);
            
            return constantTimeEquals(storedHash, computedHash);
            
        } catch (Exception exception) {
            return false;
        }
    }
    
    private static byte[] performPBKDF2(char[] password, byte[] salt, int iterations, int outputBytes) 
            throws NoSuchAlgorithmException, InvalidKeySpecException {
        PBEKeySpec keySpecification = new PBEKeySpec(password, salt, iterations, outputBytes * 8);
        SecretKeyFactory keyFactory = SecretKeyFactory.getInstance(ENCRYPTION_ALGORITHM);
        return keyFactory.generateSecret(keySpecification).getEncoded();
    }
    
    private static boolean constantTimeEquals(byte[] arrayA, byte[] arrayB) {
        if (arrayA.length != arrayB.length) {
            return false;
        }
        
        int difference = 0;
        for (int index = 0; index < arrayA.length; index++) {
            difference |= arrayA[index] ^ arrayB[index];
        }
        return difference == 0;
    }
    
    public static String sanitizeInput(String input) {
        if (input == null) {
            return "";
        }
        return input.trim()
                   .replaceAll("[<>\"']", "")
                   .replaceAll("\\s+", " ");
    }
    
    public static boolean isValidUsername(String username) {
        if (username == null || username.trim().isEmpty()) {
            return false;
        }
        return username.matches("^[a-zA-Z0-9_]{3,20}$");
    }
    
    public static boolean isValidPassword(String password) {
        if (password == null || password.length() < 6) {
            return false;
        }
        return true;
    }
}