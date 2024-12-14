package com.assessment.FileSaver.controller;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.Key;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;

import org.apache.tomcat.util.http.fileupload.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import com.assessment.response.UploadResponseDTO;

import jakarta.annotation.PreDestroy;

@RestController
public class FileSaverController {

    Logger logger = LoggerFactory.getLogger(FileSaverController.class.getName());
    private final Map<UUID, String> storageMap = new HashMap<>();

    @Value("${cipher.key.salt:SaltAndPepper}")
    private String salt;

    @Value("${filestorage.base.path:D:\\uploads\\}")
    private String sourcePath;

    @Value("${cipher.algorithm:AES}")
    private String algorithm;

    @Value("${filestorage.maximum.permitted.file.size:5000000}")
    private int maxPermittedFileSize;

    @Value("${filestorage.maximum.storage.hours:1}")
    private int maxPermittedStorageHours;

    @PostMapping(path = "/upload", consumes = { MediaType.MULTIPART_FORM_DATA_VALUE })
    public ResponseEntity<?> postMethodName(@RequestParam("file") MultipartFile file,
            @RequestParam("passcode") String passcode) {
        if (file.getSize() > maxPermittedFileSize) {
            return new ResponseEntity<String>("File too large", HttpStatus.BAD_REQUEST);
        }
        Path directory = Paths.get(sourcePath);
        if (!Files.exists(directory)) {
            try {
                Files.createDirectories(directory);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        UUID uuid = UUID.randomUUID();
        storageMap.put(uuid, file.getOriginalFilename());
        Path filePath = directory.resolve(uuid.toString());

        try (FileOutputStream fos = new FileOutputStream(filePath.toString())) {// sourcePath +
                                                                                // file.getOriginalFilename())) {
            Cipher cipher = Cipher.getInstance(algorithm);
            cipher.init(Cipher.ENCRYPT_MODE, getKey(passcode));
            fos.write(cipher.doFinal(file.getBytes()));
            logger.info(file.getOriginalFilename() + " Stored with passcode " + passcode);
            String retrievePath = ServletUriComponentsBuilder.fromCurrentContextPath().replacePath("/retrieve/")
                    .toUriString();
            return ResponseEntity.ok(new UploadResponseDTO("Uploaded",
                    retrievePath + uuid.toString() + "?passcode=" + passcode));
        } catch (Exception e) {
            logger.error("exception in upload", e);// ,e.printStackTrace());
        }
        return ResponseEntity.internalServerError().build();
    }

    @GetMapping(path = "/retrieve/{uuid}")
    public ResponseEntity<?> getMethodName(@PathVariable(value = "uuid", required = true) UUID uuid,
            @RequestParam("passcode") String passcode) {
        if (!storageMap.containsKey(uuid)) {
            return new ResponseEntity<String>("No file found with this name", HttpStatus.NOT_FOUND);
        }
        try (FileInputStream fileInputStream = new FileInputStream(
                Paths.get(sourcePath).resolve(uuid.toString()).toString())) {
            Cipher cipher = Cipher.getInstance(algorithm);
            cipher.init(Cipher.DECRYPT_MODE, getKey(passcode));
            Resource resource = new ByteArrayResource(cipher.doFinal(fileInputStream.readAllBytes()));
            if (resource.exists()) {
                return ResponseEntity.ok()
                        .header(HttpHeaders.CONTENT_DISPOSITION,
                                "attachment; filename=\"" + storageMap.get(uuid) + "\"")
                        .body(resource);
            }
        } catch (BadPaddingException bpe) {
            return new ResponseEntity<String>("bad passcode", HttpStatus.BAD_REQUEST);
        } catch (FileNotFoundException fnfe) {
            storageMap.remove(uuid);
            return new ResponseEntity<String>("No file found with this name", HttpStatus.NOT_FOUND);
        } catch (Exception e) {
            logger.error("exception in retrieve for file " + uuid, e);
            return ResponseEntity.internalServerError().build();
        }
        return ResponseEntity.notFound().build();
    }

    @Scheduled(fixedRateString = "${filestorage.removal.scheduleRate:3600000}")
    private void removeOldFiles() {
        File[] files = new File(sourcePath).listFiles(this::expiryCheck);
        try {
            if (files == null || files.length == 0)
                return;
            for (File file : files)
                Files.delete(file.toPath());
        } catch (Exception e) {
            logger.error("old file removal failed", e);
        }
    }

    public boolean expiryCheck(File file) {
        return file.exists() && file.isFile() && System.currentTimeMillis()
                - file.lastModified() > (maxPermittedStorageHours * 60 * 60 * 1000);
    }

    @PreDestroy
    public void cleanUp() {
        try {
            File f = Paths.get(sourcePath).toFile();
            if (f.exists() && f.isDirectory()) {
                FileUtils.cleanDirectory(f);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private Key getKey(String passcode) throws NoSuchAlgorithmException, InvalidKeySpecException {
        SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
        KeySpec spec = new PBEKeySpec(passcode.toCharArray(), salt.getBytes(), 65536, 256);
        return (new SecretKeySpec(factory.generateSecret(spec).getEncoded(), algorithm));
    }
}