Hi,

### This is as simple File Storage Project built SpringBoot(backend) and ReactJS(frontend).

This project takes a file and a passcode, ecrypts the file and stores it.
The ReactJS frontend is packed into a static build and copied into Spring project's static folder.
The files are deleted after 48 hours of upload.

## **Application**
The application is currently hosted at <a href =http://34.100.224.156:8081 > FileSaver</a>.

There are 2 actions to be performed
1. Upload a file along with a passcode for encryption.This will return a unique url to the file.
2. Using the URL received before, we need to provide the passcode  to download the file.

The swagger for the application can be accessed as <a href =http://34.100.224.156:8081/swagger-ui/index.html> FileSaver Swagger</a>

---

API endpoints - 
1. Upload Endpoint - POST /upload<br>
   - Description - this takes the file and passcode as multipart form data, and returns a link to download the file.<br>
   - Request Parameters-<br>
     - file(MultipartFile)<br>
     - passcode(String)<br>
   - Header - <br>
     - Content-Type: multipart/form-data<br>
   - Response - 
      - 200 OK - return json response with status and unique link
          ```
          {
        "status": "uploadStatus",
        "retrievePath": "downloadLink"
        }
          ```
      - 400 Bad Request - File invalid or too large
      - 500 Internal Server error - unknown error


2. Retreive Endpoint - GET /retreive/{uuid}?passcode<br>
   - Description - this takes the unique identifier and passcode as input, and downloads the file.<br>
   - Request Parameters-<br>
     - uuid(UUID)<br>
     - passcode(String)<br>
   
    - Response - <br>
      - 200 OK - Returns the file for download.  
          Includes the file name in `Content-Disposition` header
      - 400 Bad Request -  Invalid passcode.
      - 404 Not Found - File not found(either link is wrong or file is expired).
      - 500 Internal Server Error - Unexpected errors during download.

---

Further enhancements - 
1. Files can be stored in a cloud bucket storage which allows for setting file expiry settings, and frees up system memory.
2. Users can be provided with links which directly download the file, skipping the need for passcode. 
4. Creating a login and authentication mechanism for users can allow them to view their files.

---
Deployment Mechanism - <br>
Using maven plugins, the frontend ReactJS build is transferred into static folder of springboot application which is packaged into a deploy ready jar.<br>
This jar is placed into a cloud linux machine and run using systemctl services.<br>
Traffic on port 8081 and http traffic needs to be enabled on the host machine.