package com.assessment.FileSaver.controller;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

import java.io.File;
import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.UUID;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import com.assessment.FileSaver.service.EncryptionService;
import com.assessment.FileSaver.service.StorageService;
import com.assessment.response.UploadResponseDTO;
import com.fasterxml.jackson.databind.ObjectMapper;

@SpringBootTest(webEnvironment = RANDOM_PORT)
public class FileSaverControllerTest {

    MockMultipartFile mockFile = new MockMultipartFile("testRandomFile", "testRandomFileOriginalName", "text",
            "File text".getBytes());

    String passcode = "testPasscode";

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private StorageService storageService;

    @Autowired
    private EncryptionService encryptionService;

    @Test
    public void testUpload() throws IOException {
        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("file", new org.springframework.core.io.ByteArrayResource(mockFile.getBytes()) {
            @Override
            public String getFilename() {
                return mockFile.getOriginalFilename();
            }
        });
        body.add("passcode", passcode);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);

        HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);
        ResponseEntity<?> resp = restTemplate.exchange(
                "/upload",
                HttpMethod.POST,
                requestEntity,
                Object.class);
        assertEquals(HttpStatus.OK, resp.getStatusCode());
        UploadResponseDTO responseDTO = new ObjectMapper().convertValue(resp.getBody(), UploadResponseDTO.class);
        String uuid = responseDTO.getRetrievePath().substring(responseDTO.getRetrievePath().length() - 36);
        File file = storageService.retrieveFile(UUID.fromString(uuid));
        assertTrue(file.exists());
        assertEquals(uuid, file.getName());
    }

    @Test
    public void testLargeUpload() {
        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("file", new org.springframework.core.io.ByteArrayResource(new byte[10000001]) {
            @Override
            public String getFilename() {
                return "OriginalLargeFileName";
            }
        });
        body.add("passcode", passcode);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);
        HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);
        ResponseEntity<byte[]> resp = restTemplate.exchange(
                "/upload",
                HttpMethod.POST,
                requestEntity,
                byte[].class);
        assertEquals(HttpStatus.BAD_REQUEST, resp.getStatusCode());
    }

    @Test
    public void testDownload() throws InvalidKeyException, IllegalBlockSizeException, BadPaddingException,
            NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeySpecException, IOException {
        UUID uuid = storageService.saveFile(encryptionService.encrypt(mockFile, passcode),
                mockFile.getOriginalFilename());
        String url = String.format("/retrieve/%s?passcode=%s", uuid, passcode);
        ResponseEntity<byte[]> resp = restTemplate.exchange(
                url, // "/retrieve/"+uuid.toString()+"?passcode="+passcode,
                HttpMethod.GET,
                null,
                byte[].class);
        assertEquals(HttpStatus.OK, resp.getStatusCode());
        assertTrue(resp.getHeaders().get("content-disposition").get(0).contains(mockFile.getOriginalFilename()));
        assertArrayEquals(mockFile.getBytes(), resp.getBody());
    }

    @Test
    public void testWrongPasscodeDownload() throws InvalidKeyException, IllegalBlockSizeException, BadPaddingException,
            NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeySpecException, IOException {
        UUID uuid = storageService.saveFile(encryptionService.encrypt(mockFile, passcode),
                mockFile.getOriginalFilename());
        String url = String.format("/retrieve/%s?passcode=%s", uuid, "random");
        ResponseEntity<byte[]> resp = restTemplate.exchange(
                url, // "/retrieve/"+uuid.toString()+"?passcode="+passcode,
                HttpMethod.GET,
                null,
                byte[].class);
        assertEquals(HttpStatus.BAD_REQUEST, resp.getStatusCode());
    }

    @Test
    public void testWrongLinkDownload() throws InvalidKeyException, IllegalBlockSizeException, BadPaddingException,
            NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeySpecException, IOException {
        // storageService.saveFile(encryptionService.encrypt(mockFile, passcode),
        // mockFile.getOriginalFilename());
        String url = String.format("/retrieve/%s?passcode=%s", UUID.randomUUID(), passcode);
        ResponseEntity<byte[]> resp = restTemplate.exchange(
                url, // "/retrieve/"+uuid.toString()+"?passcode="+passcode,
                HttpMethod.GET,
                null,
                byte[].class);
        assertEquals(HttpStatus.NOT_FOUND, resp.getStatusCode());
    }
}