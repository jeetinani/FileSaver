package com.assessment.FileSaver.service;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

@Component
public class EncryptionService {

    private String salt;

    private String algorithm;

    public EncryptionService(@Value("${cipher.key.salt:SaltAndPepper}") String salt,
            @Value("${cipher.algorithm:AES}") String algorithm) {
        this.salt = salt;
        this.algorithm = algorithm;
    }

    public byte[] encrypt(MultipartFile input, String passcode)
            throws IllegalBlockSizeException, BadPaddingException, IOException, NoSuchAlgorithmException,
            NoSuchPaddingException, InvalidKeyException, InvalidKeySpecException {
        Cipher cipher = Cipher.getInstance(algorithm);
        cipher.init(Cipher.ENCRYPT_MODE, getKey(passcode));
        return cipher.doFinal(input.getBytes());
    }

    public byte[] decrypt(File file, String passcode)
            throws FileNotFoundException, IOException, NoSuchAlgorithmException, NoSuchPaddingException,
            InvalidKeyException, InvalidKeySpecException, IllegalBlockSizeException, BadPaddingException {

        try (FileInputStream fis = new FileInputStream(file)) {
            Cipher cipher = Cipher.getInstance(algorithm);
            cipher.init(Cipher.DECRYPT_MODE, getKey(passcode));
            return cipher.doFinal(fis.readAllBytes());
        }
    }

    private Key getKey(String passcode) throws NoSuchAlgorithmException, InvalidKeySpecException {
        SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
        KeySpec spec = new PBEKeySpec(passcode.toCharArray(), salt.getBytes(), 65536, 256);
        return (new SecretKeySpec(factory.generateSecret(spec).getEncoded(), algorithm));
    }
}
