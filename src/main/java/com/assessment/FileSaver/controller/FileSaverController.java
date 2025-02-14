package com.assessment.FileSaver.controller;

import java.io.FileNotFoundException;
import java.util.UUID;

import javax.crypto.BadPaddingException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

import com.assessment.FileSaver.service.FileSaverService;
import com.assessment.response.DownloadResponseDTO;
import com.assessment.response.UploadResponseDTO;

@RestController
public class FileSaverController {

    Logger logger = LoggerFactory.getLogger(FileSaverController.class.getName());

    FileSaverService fileSaverService;

    public FileSaverController(FileSaverService fileSaverService) {
        this.fileSaverService = fileSaverService;
    }

    @PostMapping(path = "/upload", consumes = { MediaType.MULTIPART_FORM_DATA_VALUE })
    public ResponseEntity<?> upload(@RequestParam("file") MultipartFile file,
            @RequestParam("passcode") String passcode) {
        try {
            UploadResponseDTO uploadResponseDTO = fileSaverService.upload(file, passcode);
            return ResponseEntity.ok(uploadResponseDTO);
        } catch (IllegalArgumentException iae) {
            return new ResponseEntity<String>("File too large", HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            logger.error("exception in upload", e);
        }
        return ResponseEntity.internalServerError().build();
    }

    @GetMapping(path = "/download/{uuid}")
    public ModelAndView downloadLink(@PathVariable(value = "uuid", required = true) String uuid) {
        return new ModelAndView("redirect:/?uuid=" + uuid);
    }

    @GetMapping(path = "/retrieve/{uuid}")
    public ResponseEntity<?> download(@PathVariable(value = "uuid", required = true) UUID uuid,
            @RequestParam("passcode") String passcode) {
        try {
        DownloadResponseDTO downloadResponseDTO = fileSaverService.download(uuid, passcode);
            if (downloadResponseDTO.getResource().exists()) {
                return ResponseEntity.ok()
                        .header(HttpHeaders.CONTENT_DISPOSITION,
                                "attachment; filename=\"" + downloadResponseDTO.getOriginalFileName() + "\"")
                        .body(downloadResponseDTO.getResource());
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