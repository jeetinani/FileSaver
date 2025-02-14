package com.assessment.FileSaver.service;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.UUID;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.multipart.MultipartFile;

import com.assessment.response.DownloadResponseDTO;
import com.assessment.response.UploadResponseDTO;

//@SpringBootTest(webEnvironment = RANDOM_PORT)
@ExtendWith(MockitoExtension.class)
public class FileSaverServiceTest {

    MockMultipartFile mockFile = new MockMultipartFile("testRandomFile", "testRandomFileOriginalName", "text",
            "File text".getBytes());

    byte[] encryptedFileBytes = "encryptedFile".getBytes();

    String passcode = "testPasscode";
    UUID uuid = UUID.randomUUID();

    @Mock
    private StorageService storageService;

    @Mock
    private EncryptionService encryptionService;

    @InjectMocks
    private FileSaverService fileSaverService;

    @Test
    public void testUpload() throws IOException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException,
            NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeySpecException {

        MockHttpServletRequest mockRequest = new MockHttpServletRequest();
        mockRequest.setContextPath("/testContextPath");
        ServletRequestAttributes attributes = new ServletRequestAttributes(mockRequest);
        RequestContextHolder.setRequestAttributes(attributes);
        when(encryptionService.encrypt(mockFile, passcode)).thenReturn(encryptedFileBytes);
        when(storageService.saveFile(encryptedFileBytes, mockFile.getOriginalFilename())).thenReturn(uuid);
        UploadResponseDTO resp = fileSaverService.upload(mockFile, passcode);
        assertEquals("Uploaded", resp.getUploadStatus());
        assertEquals(uuid.toString(),
                resp.getRetrievePath().substring(resp.getRetrievePath().length() - 36));
    }

    @Test
    public void testLargeUpload() throws IOException, InvalidKeyException, IllegalBlockSizeException,
            BadPaddingException, NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeySpecException {
        when(encryptionService.encrypt(any(MultipartFile.class), any(String.class))).thenReturn(encryptedFileBytes);
        when(storageService.saveFile(any(byte[].class), any(String.class))).thenThrow(IllegalArgumentException.class);
        assertThrows(IllegalArgumentException.class, () -> fileSaverService.upload(mockFile, passcode));
    }

    @Test
    public void testDownload() throws InvalidKeyException, IllegalBlockSizeException, BadPaddingException,
            NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeySpecException, IOException {
        File file = new File("randomFile");
        when(storageService.retrieveFile(uuid)).thenReturn(file);
        when(encryptionService.decrypt(file, passcode)).thenReturn(mockFile.getBytes());
        when(storageService.getFileName(uuid)).thenReturn(file.getName());
        DownloadResponseDTO resp = fileSaverService.download(uuid, passcode);
        assertEquals(file.getName(), resp.getOriginalFileName());
        assertInstanceOf(ByteArrayResource.class, resp.getResource());
        assertArrayEquals(mockFile.getBytes(), ((ByteArrayResource) resp.getResource()).getByteArray());
        Files.deleteIfExists(file.toPath());
    }

    @Test
    public void testWrongPasscodeDownload() throws InvalidKeyException, IllegalBlockSizeException, BadPaddingException,
            NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeySpecException, IOException {
        File file = new File("randomFile");
        when(storageService.retrieveFile(uuid)).thenReturn(file);
        when(encryptionService.decrypt(file, passcode)).thenThrow(BadPaddingException.class);
        assertThrows(BadPaddingException.class, () -> fileSaverService.download(uuid, passcode));

    }

    @Test
    public void testWrongLinkDownload() throws InvalidKeyException, IllegalBlockSizeException, BadPaddingException,
            NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeySpecException, IOException {
        when(storageService.retrieveFile(any(UUID.class))).thenThrow(FileNotFoundException.class);
        assertThrows(FileNotFoundException.class, () -> fileSaverService.download(UUID.randomUUID(), passcode));
    }
}
