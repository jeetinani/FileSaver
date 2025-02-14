package com.assessment.FileSaver.service;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class StorageService {

    private String sourcePath;

    private int maxPermittedStorageHours;

    private int maxPermittedFileSize;

    private FileDetailsService fileDetailsService;

    public StorageService(@Value("${filestorage.base.path:./uploads}") String sourcePath,
            @Value("${filestorage.maximum.storage.hours:1}") int maxPermittedStorageHours,
            @Value("${filestorage.maximum.permitted.file.size:5000000}") int maxPermittedFileSize,
            FileDetailsService fileDetailsService) {
        this.sourcePath = sourcePath;
        this.maxPermittedStorageHours = maxPermittedStorageHours;
        this.maxPermittedFileSize = maxPermittedFileSize;
        this.fileDetailsService = fileDetailsService;
    }

    Logger logger = LoggerFactory.getLogger(StorageService.class.getName());

    // private final Map<UUID, String> storageMap = new HashMap<>();

    public UUID saveFile(byte[] fileByteArray, String originalFileName) throws IOException {

        if (fileByteArray.length > maxPermittedFileSize) {
            throw new IllegalArgumentException();
        }
        // UUID uuid = UUID.randomUUID();
        // storageMap.put(uuid, originalFileName);
        UUID uuid = fileDetailsService.saveFileDetails(originalFileName).getUuid();
        Path filePath = getDirectory().resolve(uuid.toString());
        try (FileOutputStream fos = new FileOutputStream(filePath.toString())) {
            fos.write(fileByteArray);
        }
        return uuid;
    }

    public File retrieveFile(UUID uuid) throws FileNotFoundException {
        // if (!storageMap.containsKey(uuid)) {
        if (!fileDetailsService.isFilePresent(uuid)) {
            throw new FileNotFoundException();
        }
        return new File(Paths.get(sourcePath).resolve(uuid.toString()).toString());
    }

    public String getFileName(UUID uuid) {
        return fileDetailsService.getFileName(uuid);// storageMap.get(uuid);
    }

    @Scheduled(fixedRateString = "${filestorage.removal.scheduleRate:3600000}")
    public void removeOldFiles() {
        List<UUID> filesList = fileDetailsService.getListOfExpiredFiles(maxPermittedStorageHours);
        if (filesList == null || filesList.size() == 0)
            return;
        try {
            for (UUID uuid : filesList) {
                Files.delete(Paths.get(sourcePath).resolve(uuid.toString()));
                fileDetailsService.removeFile(uuid);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /*
     * @Scheduled(fixedRateString = "${filestorage.removal.scheduleRate:3600000}")
     * public void removeOldFiles() {
     * File[] files = new File(sourcePath).listFiles();
     * try {
     * if (files == null || files.length == 0)
     * return;
     * for (File file : files) {
     * if (this.expiryCheck(file)) {
     * //System.out.println("file deleted "+ file.getName());
     * Files.delete(file.toPath());
     * fileDetailsService.removeFile(UUID.fromString(file.getName()));
     * }
     * }
     * } catch (Exception e) {
     * logger.error("old file removal failed", e);
     * }
     * }
     */

    public boolean expiryCheck(File file) {
        return file.exists() && file.isFile() && System.currentTimeMillis()
                - file.lastModified() > (maxPermittedStorageHours * 60 * 60 * 1000);
    }

    /*
     * @PreDestroy
     * public void cleanUp() {
     * try {
     * File f = Paths.get(sourcePath).toFile();
     * if (f.exists() && f.isDirectory()) {
     * FileUtils.cleanDirectory(f);
     * }
     * } catch (IOException e) {
     * e.printStackTrace();
     * }
     * }
     */

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