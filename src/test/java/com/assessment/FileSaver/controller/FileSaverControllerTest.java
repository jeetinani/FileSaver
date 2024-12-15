package com.assessment.FileSaver.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

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

import com.assessment.FileSaver.service.StorageService;
import com.assessment.response.UploadResponseDTO;
import com.fasterxml.jackson.databind.ObjectMapper;

@SpringBootTest(webEnvironment = RANDOM_PORT)
public class FileSaverControllerTest {

    /*
     * StorageService storageService =new StorageService("./uploads",1,5000000);
     * EncryptionService encryptionService =new EncryptionService("random", "AES");
     * FileSaverController controller = new FileSaverController(encryptionService,
     * storageService);
     */
    MockMultipartFile mockFile = new MockMultipartFile("testRandomFile", "testRandomFileOriginalName", "text",
            "File text".getBytes());

    String passcode = "testPasscode";

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private StorageService storageService;

    @Test
    public void testUpload() throws IOException {
        // ResponseEntity<?> resp = controller.upload(mockFile,passcode);
    MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
    body.add("file", new org.springframework.core.io.ByteArrayResource(mockFile.getBytes()) {
        @Override
        public String getFilename() {
            return mockFile.getOriginalFilename();
        }
    });
    body.add("passcode", passcode);

    // Set the headers for multipart content
    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.MULTIPART_FORM_DATA);

    // Create an HttpEntity containing the body and headers
    HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);

        // Send the request using TestRestTemplate
        ResponseEntity<?> resp = restTemplate.exchange(
                "/upload",
                HttpMethod.POST,
                requestEntity,//new HttpEntity<>(body),
                Object.class);
        assertEquals(HttpStatus.OK, resp.getStatusCode());
        System.out.println("wait here");
        UploadResponseDTO responseDTO = new ObjectMapper().convertValue(resp.getBody(), UploadResponseDTO.class);
        //UploadResponseDTO responseDTO = (UploadResponseDTO) resp.getBody();
        String uuid = responseDTO.getRetrievePath().substring(responseDTO.getRetrievePath().length() - 36);
        File file = storageService.retrieveFile(UUID.fromString(uuid));
        assertTrue(file.exists());
    }
}
