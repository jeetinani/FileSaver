package com.assessment.FileSaver.controller;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
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
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import com.assessment.FileSaver.response.DownloadResponseDTO;
import com.assessment.FileSaver.response.UploadResponseDTO;
import com.assessment.FileSaver.service.FileSaverService;
import com.fasterxml.jackson.databind.ObjectMapper;

//@SpringBootTest(webEnvironment = RANDOM_PORT)
@ExtendWith(MockitoExtension.class)
public class FileSaverControllerUnitTest {

    MockMultipartFile mockFile = new MockMultipartFile("testRandomFile", "testRandomFileOriginalName", "text",
            "File text".getBytes());

    byte[] encryptedFileBytes = "encryptedFile".getBytes();

    String passcode = "testPasscode";
    UUID uuid = UUID.randomUUID();

    @Mock
    private FileSaverService fileSaverService;

    @InjectMocks
    private FileSaverController fileSaverController;

    @Test
    public void testUpload() throws IOException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException,
            NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeySpecException {

        MockHttpServletRequest mockRequest = new MockHttpServletRequest();
        mockRequest.setContextPath("/testContextPath");
        ServletRequestAttributes attributes = new ServletRequestAttributes(mockRequest);
        RequestContextHolder.setRequestAttributes(attributes);
        when(fileSaverService.upload(mockFile, passcode))
                .thenReturn(new UploadResponseDTO("Uploaded", "retrievePath" + uuid.toString()));
        ResponseEntity<?> resp = fileSaverController.upload(mockFile, passcode);
        assertEquals(HttpStatus.OK, resp.getStatusCode());
        UploadResponseDTO responseDTO = new ObjectMapper().convertValue(resp.getBody(), UploadResponseDTO.class);
        assertEquals(uuid.toString(),
                responseDTO.getRetrievePath().substring(responseDTO.getRetrievePath().length() - 36));
    }

    @Test
    public void testLargeUpload() throws IOException, InvalidKeyException, IllegalBlockSizeException,
            BadPaddingException, NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeySpecException {
        when(fileSaverService.upload(mockFile, passcode))
                .thenThrow(IllegalArgumentException.class);
        ResponseEntity<?> resp = fileSaverController.upload(mockFile, passcode);
        assertEquals(HttpStatus.BAD_REQUEST, resp.getStatusCode());
    }

    @Test
    public void testDownload() throws InvalidKeyException, IllegalBlockSizeException, BadPaddingException,
            NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeySpecException, IOException {
        File file = new File("randomFile");
        when(fileSaverService.download(uuid, passcode))
                .thenReturn(new DownloadResponseDTO(new ByteArrayResource(mockFile.getBytes()), file.getName()));
        ResponseEntity<?> resp = fileSaverController.download(uuid, passcode);
        assertEquals(HttpStatus.OK, resp.getStatusCode());
        assertTrue(resp.getHeaders().get("content-disposition").get(0).contains(file.getName()));
        assertInstanceOf(ByteArrayResource.class, resp.getBody());
        assertArrayEquals(mockFile.getBytes(), ((ByteArrayResource) resp.getBody()).getByteArray());
        Files.deleteIfExists(file.toPath());
    }

    @Test
    public void testWrongPasscodeDownload() throws InvalidKeyException, IllegalBlockSizeException, BadPaddingException,
            NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeySpecException, IOException {
        when(fileSaverService.download(uuid, passcode)).thenThrow(BadPaddingException.class);
        ResponseEntity<?> resp = fileSaverController.download(uuid, passcode);
        assertEquals(HttpStatus.BAD_REQUEST, resp.getStatusCode());
    }

    @Test
    public void testWrongLinkDownload() throws InvalidKeyException, IllegalBlockSizeException, BadPaddingException,
            NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeySpecException, IOException {
        when(fileSaverService.download(any(UUID.class), anyString())).thenThrow(FileNotFoundException.class);
        ResponseEntity<?> resp = fileSaverController.download(UUID.randomUUID(), passcode);
        assertEquals(HttpStatus.NOT_FOUND, resp.getStatusCode());
    }
}