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

import org.apache.tomcat.util.http.fileupload.FileUtils;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;

//@SpringBootTest(webEnvironment=RANDOM_PORT)
public class StorageServiceTest {

    private byte[] fileByteArray = "FileByteArray".getBytes();
    private String fileName = "fileName";

    private static String tempPath = "./tempPath";
    private int maxPermittedFileStorage = 10000000;

    // @Autowired
    private StorageService storageService = new StorageService(tempPath, 1, maxPermittedFileStorage);

    @Test
    public void testSaveFile() throws IOException {
        UUID uuid = storageService.saveFile(fileByteArray, fileName);
        File file = new File(Paths.get(tempPath).resolve(uuid.toString()).toString());
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
    public void testOldFileRemoval() throws IOException {
        storageService = new StorageService(tempPath, 0, maxPermittedFileStorage);
        UUID uuid = storageService.saveFile(fileByteArray, fileName);
        storageService.removeOldFiles();
        assertFalse(storageService.retrieveFile(uuid).exists());
    }

    @Test
    public void testCleanup() throws IOException {
        UUID uuid = storageService.saveFile(fileByteArray, fileName);
        storageService.cleanUp();
        File file = new File(Paths.get(tempPath).resolve(uuid.toString()).toString());
        assertFalse(file.exists());
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
}