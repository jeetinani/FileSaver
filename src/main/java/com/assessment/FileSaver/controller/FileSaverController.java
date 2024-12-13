package com.assessment.FileSaver.controller;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.security.Key;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import com.assessment.response.UploadResponseDTO;

@RestController
public class FileSaverController {

    Logger logger = LoggerFactory.getLogger(FileSaverController.class.getName());

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
            return new ResponseEntity<String>("File too large", HttpStatus.NOT_ACCEPTABLE);
        }
        try (FileOutputStream fos = new FileOutputStream(sourcePath + file.getOriginalFilename())) {
            Cipher cipher = Cipher.getInstance(algorithm);
            cipher.init(Cipher.ENCRYPT_MODE, getKey(passcode));
            // file.transferTo(new File("D:\\uploads\\"+file.getOriginalFilename()));
            fos.write(cipher.doFinal(file.getBytes()));
            logger.info(file.getOriginalFilename() + " Stored with passcode " + passcode);
            String retrievePath = ServletUriComponentsBuilder.fromCurrentContextPath().replacePath("/retrieve/")
                    .toUriString();
            return ResponseEntity.ok(new UploadResponseDTO("Uploaded",
                    retrievePath + file.getOriginalFilename() + "?passcode=" + passcode));
        } catch (Exception e) {
            logger.error("exception in upload", e);// ,e.printStackTrace());
        }
        return ResponseEntity.internalServerError().build();
    }

    @GetMapping(path = "/retrieve/{fileName}")
    public ResponseEntity<?> getMethodName(@PathVariable(value = "fileName", required = true) String fileName,
            @RequestParam("passcode") String passcode) {
        File file = new File(sourcePath + fileName);
        if(file.exists() && file.isFile() && System.currentTimeMillis()-file.lastModified()>(maxPermittedStorageHours*60*60*1000)){
            file.delete();
            return new ResponseEntity<String>("File too old", HttpStatus.NOT_FOUND);
        }
        try (FileInputStream fileInputStream = new FileInputStream(file)) {
            Cipher cipher = Cipher.getInstance(algorithm);
            cipher.init(Cipher.DECRYPT_MODE, getKey(passcode));
            Resource resource = new ByteArrayResource(cipher.doFinal(fileInputStream.readAllBytes()));// new
                                                                                                      // UrlResource(filePath.toUri());
            if (resource.exists()) {
                return ResponseEntity.ok()
                        .header(HttpHeaders.CONTENT_DISPOSITION,
                                "attachment; filename=\"" + fileName + "\"")
                        .body(resource);
            }
        } catch (BadPaddingException bpe) {
            return new ResponseEntity<String>("bad passcode", HttpStatus.NOT_ACCEPTABLE);
        } catch (FileNotFoundException fnfe) {
            return new ResponseEntity<String>("No file found with this name", HttpStatus.NOT_FOUND);
        } catch (Exception e) {
            logger.error("exception in retrieve for " + fileName, e);
            return ResponseEntity.internalServerError().build();
        }
        return ResponseEntity.notFound().build();
    }

    private Key getKey(String passcode) throws NoSuchAlgorithmException, InvalidKeySpecException {
        SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
        KeySpec spec = new PBEKeySpec(passcode.toCharArray(), salt.getBytes(), 65536, 256);
        return (new SecretKeySpec(factory.generateSecret(spec).getEncoded(), algorithm));
    }
}