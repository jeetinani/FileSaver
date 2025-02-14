package com.assessment.FileSaver.service;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.apache.tomcat.util.http.fileupload.FileUtils;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.assessment.FileSaver.domain.FileDetails;

//@SpringBootTest(webEnvironment=RANDOM_PORT)
@ExtendWith(MockitoExtension.class)
public class StorageServiceTest {

    private byte[] fileByteArray = "FileByteArray".getBytes();
    private String fileName = "fileName";

    private static String tempPath = "./tempPath";
    private int maxPermittedFileStorage = 10000000;
    private FileDetails fileDetails = new FileDetails(fileName,UUID.randomUUID(),LocalDateTime.now(),1,true);

    // @Autowired
    @Mock
    private FileDetailsService fileDetailsService;

    @InjectMocks
    private StorageService storageService = new StorageService(tempPath, 1, maxPermittedFileStorage, fileDetailsService);

    @InjectMocks
    private StorageService zeroHoursStorageService = new StorageService(tempPath, 0, maxPermittedFileStorage, fileDetailsService);


    @Test
    public void testSaveFile() throws IOException {
        when(fileDetailsService.saveFileDetails(fileName)).thenReturn(fileDetails);
        when(fileDetailsService.getFileName(fileDetails.getUuid())).thenReturn(fileName);
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
        when(fileDetailsService.saveFileDetails(any(String.class))).thenReturn(fileDetails);
        when(fileDetailsService.getFileName(fileDetails.getUuid())).thenReturn(fileName);
        when(fileDetailsService.isFilePresent(fileDetails.getUuid())).thenReturn(true);
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
        when(fileDetailsService.isFilePresent(any(UUID.class))).thenReturn(false);
        assertThrows(FileNotFoundException.class, () -> storageService.retrieveFile(UUID.randomUUID()));
    }

    /* @Test
    public void testCleanup() throws IOException {
        when(fileDetailsService.saveFileDetails(any(String.class))).thenReturn(fileDetails);
        UUID uuid = storageService.saveFile(fileByteArray, fileName);
        storageService.cleanUp();
        File file = new File(Paths.get(tempPath).resolve(uuid.toString()).toString());
        assertFalse(file.exists());
    } */

    @Test
    public void testOldFileRemoval() throws IOException {
        //storageService = new StorageService(tempPath, 0, maxPermittedFileStorage);
        when(fileDetailsService.saveFileDetails(any(String.class))).thenReturn(fileDetails);
        when(fileDetailsService.isFilePresent(fileDetails.getUuid())).thenReturn(true);
        when(fileDetailsService.getListOfExpiredFiles(1)).thenReturn(List.of(fileDetails));
        UUID uuid = storageService.saveFile(fileByteArray, fileName);
        storageService.removeOldFiles();
        assertFalse(storageService.retrieveFile(uuid).exists());
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