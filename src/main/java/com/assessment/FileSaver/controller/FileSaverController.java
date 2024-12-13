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

@RestController
public class FileSaverController {

    Logger logger = LoggerFactory.getLogger(FileSaverController.class.getName());
    private final String salt = "SaltAndPepper";
    private final String sourcePath = "D:\\uploads\\";
    
    @PostMapping(path = "/upload", consumes = { MediaType.MULTIPART_FORM_DATA_VALUE })
    public ResponseEntity<String> postMethodName(@RequestParam("file") MultipartFile file,
            @RequestParam("password") String password) {
        try (FileOutputStream fos = new FileOutputStream(sourcePath + file.getOriginalFilename())) {
            Cipher cipher = Cipher.getInstance("AES");
            cipher.init(Cipher.ENCRYPT_MODE, getKey(password));
            // file.transferTo(new File("D:\\uploads\\"+file.getOriginalFilename()));
            fos.write(cipher.doFinal(file.getBytes()));
        } catch (Exception e) {
        logger.error("exception in upload", e);//,e.printStackTrace());
            return ResponseEntity.internalServerError().build();
        }
        return ResponseEntity.ok(file.getOriginalFilename() + " Stored with password " + password);
    }

    @GetMapping(path = "/retrieve/{fileName}")
    public ResponseEntity<?> getMethodName(@PathVariable(value = "fileName", required = true) String fileName,
            @RequestParam("password") String password) {
        try (FileInputStream fileInputStream = new FileInputStream(new File(sourcePath + fileName))){
            Cipher cipher = Cipher.getInstance("AES");
            cipher.init(Cipher.DECRYPT_MODE, getKey(password));
            Resource resource = new ByteArrayResource(cipher.doFinal(fileInputStream.readAllBytes()));// new UrlResource(filePath.toUri());
            if (resource.exists()) {
                return ResponseEntity.ok()
                        .header(HttpHeaders.CONTENT_DISPOSITION,
                                "attachment; filename=\"" + fileName + "\"")
                        .body(resource);
            }
        } catch(BadPaddingException bpe){
            return new ResponseEntity<String>("bad password",HttpStatus.NOT_ACCEPTABLE);
        }catch(FileNotFoundException fnfe){
            return new ResponseEntity<String>("No file found with this name",HttpStatus.NOT_FOUND);
        }catch(Exception e){
            logger.error("exception in retrieve for "+fileName, e);
            return ResponseEntity.internalServerError().build();
        }
        return ResponseEntity.notFound().build();
    }

    private Key getKey(String password) throws NoSuchAlgorithmException, InvalidKeySpecException {
        SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
        KeySpec spec = new PBEKeySpec(password.toCharArray(), salt.getBytes(), 65536, 256);
        return (new SecretKeySpec(factory.generateSecret(spec).getEncoded(), "AES"));
    }
}
