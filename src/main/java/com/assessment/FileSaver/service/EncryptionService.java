package com.assessment.FileSaver.service;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Paths;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;
import java.util.UUID;

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

    @Value("${cipher.key.salt:SaltAndPepper}")
    private String salt;

    @Value("${cipher.algorithm:AES}")
    private String algorithm;

    @Value("${filestorage.base.path:./uploads}")
    private String sourcePath;

    public byte[] encrypt(MultipartFile input, String passcode) throws IllegalBlockSizeException, BadPaddingException, IOException, NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, InvalidKeySpecException {
        Cipher cipher = Cipher.getInstance(algorithm);
        cipher.init(Cipher.ENCRYPT_MODE, getKey(passcode));
        return cipher.doFinal(input.getBytes());
    }

    public byte[] decrypt(UUID uuid, String passcode) throws FileNotFoundException, IOException, NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, InvalidKeySpecException, IllegalBlockSizeException, BadPaddingException{
        try(FileInputStream fileInputStream = new FileInputStream(
            Paths.get(sourcePath).resolve(uuid.toString()).toString())) {
            Cipher cipher = Cipher.getInstance(algorithm);
            cipher.init(Cipher.DECRYPT_MODE, getKey(passcode));
            return cipher.doFinal(fileInputStream.readAllBytes());
        }
    }

    private Key getKey(String passcode) throws NoSuchAlgorithmException, InvalidKeySpecException {
        SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
        KeySpec spec = new PBEKeySpec(passcode.toCharArray(), salt.getBytes(), 65536, 256);
        return (new SecretKeySpec(factory.generateSecret(spec).getEncoded(), algorithm));
    }
}
