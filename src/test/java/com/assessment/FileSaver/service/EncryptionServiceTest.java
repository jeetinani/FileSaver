package com.assessment.FileSaver.service;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
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

import org.apache.tomcat.util.http.fileupload.FileUtils;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;

public class EncryptionServiceTest {
    private String salt = "testSalt";
    private String algorithm = "AES";
    private String passcode = "testPasscode";
    private static String tempPath = "./tempPath";

    MockMultipartFile mockFile = new MockMultipartFile("testRandomFile", "testRandomFileOriginalName", "text",
            "File text".getBytes());
    private EncryptionService encryptionService = new EncryptionService(salt, algorithm);

    @Test
    public void testEncrypt() throws InvalidKeyException, IllegalBlockSizeException, BadPaddingException,
            NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeySpecException, IOException {
        byte[] encryptedByteArray = encryptionService.encrypt(mockFile, passcode);
        Cipher cipher = Cipher.getInstance(algorithm);
        cipher.init(Cipher.ENCRYPT_MODE, getKey());
        assertArrayEquals(cipher.doFinal(mockFile.getBytes()), encryptedByteArray);
    }

    @Test
    public void testDecrypt() throws NoSuchAlgorithmException, NoSuchPaddingException, IllegalBlockSizeException,
            BadPaddingException, IOException, InvalidKeyException, InvalidKeySpecException {
        Cipher cipher = Cipher.getInstance(algorithm);
        cipher.init(Cipher.ENCRYPT_MODE, getKey());
        try (FileOutputStream fos = new FileOutputStream(
                getDirectory().resolve(mockFile.getOriginalFilename()).toString())) {
            fos.write(cipher.doFinal(mockFile.getBytes()));
        }
        assertArrayEquals(mockFile.getBytes(), encryptionService
                .decrypt(getDirectory().resolve(mockFile.getOriginalFilename()).toFile(), passcode));

    }

    @AfterAll
    public static void cleanUp() {
        try {
            File f = Paths.get(tempPath).toFile();
            if (f.exists() && f.isDirectory()) {
                FileUtils.cleanDirectory(f);
                f.delete();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private Key getKey() throws NoSuchAlgorithmException, InvalidKeySpecException {
        SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
        KeySpec spec = new PBEKeySpec(passcode.toCharArray(), salt.getBytes(), 65536, 256);
        return (new SecretKeySpec(factory.generateSecret(spec).getEncoded(), algorithm));
    }

    private Path getDirectory() {
        Path directory = Paths.get(tempPath);
        if (!Files.exists(directory)) {
            try {
                Files.createDirectories(directory);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return directory;
    }
}
