package com.assessment.FileSaver.service;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.apache.tomcat.util.http.fileupload.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import jakarta.annotation.PreDestroy;

@Component
public class StorageService {

    @Value("${filestorage.base.path:./uploads}")
    private String sourcePath;

    @Value("${filestorage.maximum.storage.hours:1}")
    private int maxPermittedStorageHours;

    @Value("${filestorage.maximum.permitted.file.size:5000000}")
    private int maxPermittedFileSize;

    Logger logger = LoggerFactory.getLogger(StorageService.class.getName());

    private final Map<UUID, String> storageMap = new HashMap<>();

    public UUID saveFile(byte[] fileByteArray, String originalFileName) throws IOException {

        if (fileByteArray.length > maxPermittedFileSize) {
            throw new IllegalArgumentException();
        }
        UUID uuid = UUID.randomUUID();
        storageMap.put(uuid, originalFileName);
        Path filePath = getDirectory().resolve(uuid.toString());
        try (FileOutputStream fos = new FileOutputStream(filePath.toString())) {
            fos.write(fileByteArray);
        }
        return uuid;
    }

    public File retrieveFile(UUID uuid) throws FileNotFoundException {
        if (!storageMap.containsKey(uuid)) {
            throw new FileNotFoundException();
        }
        return new File(Paths.get(sourcePath).resolve(uuid.toString()).toString());
    }

    public String getFileName(UUID uuid) {
        return storageMap.get(uuid);
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

    private Path getDirectory() {
        Path directory = Paths.get(sourcePath);
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