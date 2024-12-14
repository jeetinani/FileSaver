package com.assessment.FileSaver.controller;

import java.io.FileNotFoundException;
import java.util.UUID;

import javax.crypto.BadPaddingException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
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
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import com.assessment.FileSaver.service.EncryptionService;
import com.assessment.FileSaver.service.StorageService;
import com.assessment.response.UploadResponseDTO;

@RestController
public class FileSaverController {

    Logger logger = LoggerFactory.getLogger(FileSaverController.class.getName());

    @Autowired
    private EncryptionService encryptionService;

    @Autowired
    private StorageService storageService;

    @GetMapping("/")
    public ModelAndView getMethodName() {
        return new ModelAndView("redirect:/swagger-ui/index.html");
    }

    @PostMapping(path = "/upload", consumes = { MediaType.MULTIPART_FORM_DATA_VALUE })
    public ResponseEntity<?> postMethodName(@RequestParam("file") MultipartFile file,
            @RequestParam("passcode") String passcode) {
        try {
            UUID uuid = storageService.saveFile(encryptionService.encrypt(file, passcode), file.getOriginalFilename());
            logger.info(file.getOriginalFilename() + " Stored with passcode " + passcode);
            String retrievePath = ServletUriComponentsBuilder.fromCurrentContextPath().replacePath("/retrieve/")
                    .toUriString();
            return ResponseEntity.ok(new UploadResponseDTO("Uploaded",
                    retrievePath + uuid.toString() + "?passcode=" + passcode));
        } catch (IllegalArgumentException iae) {
            return new ResponseEntity<String>("File too large", HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            logger.error("exception in upload", e);
        }
        return ResponseEntity.internalServerError().build();
    }

    @GetMapping(path = "/retrieve/{uuid}")
    public ResponseEntity<?> getMethodName(@PathVariable(value = "uuid", required = true) UUID uuid,
            @RequestParam("passcode") String passcode) {
        try {
            Resource resource = new ByteArrayResource(
                    encryptionService.decrypt(storageService.retrieveFile(uuid), passcode));
            if (resource.exists()) {
                return ResponseEntity.ok()
                        .header(HttpHeaders.CONTENT_DISPOSITION,
                                "attachment; filename=\"" + storageService.getFileName(uuid) + "\"")
                        .body(resource);
            }
        } catch (BadPaddingException bpe) {
            return new ResponseEntity<String>("bad passcode", HttpStatus.BAD_REQUEST);
        } catch (FileNotFoundException fnfe) {
            return new ResponseEntity<String>("No file found with this name", HttpStatus.NOT_FOUND);
        } catch (Exception e) {
            logger.error("exception in retrieve for file " + uuid, e);
            return ResponseEntity.internalServerError().build();
        }
        return ResponseEntity.notFound().build();
    }
}