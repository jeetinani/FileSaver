package com.assessment.FileSaver.service;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.UUID;

import org.junit.jupiter.api.Test;

//@SpringBootTest(webEnvironment=RANDOM_PORT)
public class StorageServiceTest {

    private byte[] fileByteArray = "FileByteArray".getBytes();
    private String fileName = "fileName";

    private String sourcePath = "./uploads";
    private int maxPermittedFileStorage = 10000000;

    // @Autowired
    private StorageService storageService = new StorageService(sourcePath, 48, maxPermittedFileStorage);

    @Test
    public void testSaveFile() throws IOException {
        UUID uuid = storageService.saveFile(fileByteArray, fileName);
        File file = new File(Paths.get(sourcePath).resolve(uuid.toString()).toString());
        assertTrue(file.exists() && file.isFile());
        assertEquals(fileName, storageService.getFileName(uuid));
        try (FileInputStream fis = new FileInputStream(file)) {
            assertArrayEquals(fileByteArray, fis.readAllBytes());
        }
    }

    @Test
    public void saveLargeFile() {
        assertThrows(IllegalArgumentException.class,
                () -> storageService.saveFile(new byte[maxPermittedFileStorage + 1], fileName));
    }

    @Test
    public void testRetrieveFile() throws IOException {
        UUID uuid = storageService.saveFile(fileByteArray, fileName);
        File file = storageService.retrieveFile(uuid);
        assertTrue(file.exists() && file.isFile());
        assertEquals(fileName, storageService.getFileName(uuid));
        try (FileInputStream fis = new FileInputStream(file)) {
            assertArrayEquals(fileByteArray, fis.readAllBytes());
        }
    }

    @Test
    public void testRetrieveUnknownFile() {
        assertThrows(FileNotFoundException.class, () -> storageService.retrieveFile(UUID.randomUUID()));
    }

    @Test
    public void testCleanup() throws IOException {
        UUID uuid = storageService.saveFile(fileByteArray, fileName);
        storageService.cleanUp();
        File file = new File(Paths.get(sourcePath).resolve(uuid.toString()).toString());
        assertFalse(file.exists());
    }
}