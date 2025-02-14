package com.assessment.FileSaver.service;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.UUID;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import com.assessment.FileSaver.response.DownloadResponseDTO;
import com.assessment.FileSaver.response.UploadResponseDTO;

@Service
public class FileSaverService {
    Logger logger = LoggerFactory.getLogger(FileSaverService.class.getName());

    private EncryptionService encryptionService;

    private StorageService storageService;

    public FileSaverService(EncryptionService encryptionService, StorageService storageService) {
        this.encryptionService = encryptionService;
        this.storageService = storageService;
    }

    public UploadResponseDTO upload(MultipartFile file, String passcode)
            throws InvalidKeyException, IllegalBlockSizeException, BadPaddingException, NoSuchAlgorithmException,
            NoSuchPaddingException, InvalidKeySpecException, IOException {
        UUID uuid = storageService.saveFile(encryptionService.encrypt(file, passcode), file.getOriginalFilename());
        logger.info(file.getOriginalFilename() + " Stored with passcode " + passcode);
        String retrievePath = ServletUriComponentsBuilder.fromCurrentContextPath().replacePath("/download/")
                .toUriString();
        return new UploadResponseDTO("Uploaded",
                retrievePath + uuid.toString());
    }

    public DownloadResponseDTO download(UUID uuid, String passcode)
            throws InvalidKeyException, FileNotFoundException, NoSuchAlgorithmException, NoSuchPaddingException,
            InvalidKeySpecException, IllegalBlockSizeException, BadPaddingException, IOException {
        Resource resource = new ByteArrayResource(
                encryptionService.decrypt(storageService.retrieveFile(uuid), passcode));
        return new DownloadResponseDTO(resource, storageService.getFileName(uuid));
    }
}