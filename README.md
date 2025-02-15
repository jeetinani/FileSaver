Hi,

### This is as simple File Storage Project developed using SpringBoot(backend) and ReactJS(frontend), packaged using Docker and deployed using Google Cloud Run.

This project takes a file and a passcode, ecrypts the file and stores it.The files are deleted after 48 hours of upload.
The ReactJS frontend is packed into a static build and copied into Spring project's static folder using maven package job.
This entire project is then packaged into a docker image, uploaded to Google Cloud Registry and deployed to Google Cloud Run using a trigger on this repository with Google Cloud Build


## **Application**
The application is currently hosted on Google Cloud Run.

There are 2 actions to be performed
1. Upload a file along with a passcode for encryption.This will return a unique url to the file.
2. Using the URL received before, we need to provide the passcode  to download the file.

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
3. Creating a login and authentication mechanism for users can allow them to view their files.
4. Having a persistent data storage for user information, file metadata, user keys, etc.
5. A serverless script running at a particular can remvoe old files and notify file owners about the same.

---
Deployment Mechanism - <br>
1. To Run using Google Cloud
  -  Create a Google Cloud Run which will link to this repository. This will create a trigger on this repo to build as  and when configured, build the docker image, push it to registry, and run.
2. To Run as a Docker Image
  - Using docker cli, run command `docker image -t filesaver . ` which will give you the image and the same can be run either through command line, or by pulling from a container registry into a server.
3. To Run on a Linux VM
  - Using maven, run the command `./mvnw clean package` 
    will give you a packged jar by the name `filesaver-application.jar`<br>
    This jar is placed into a cloud linux machine and the command `java -jar filesaver-application.jar` can run the application. Alternatively, we can also run this using systemctl services.<br>

--- 
Sample System Architecture Diagram of a simple cloud deployment in production would look something like this.
<img src="https://github.com/jeetinani/FileSaver/blob/main/CloudProdSystemArchitecture.png" title="Architecture" alt="Architecture" width="523" height="343"/>&nbsp;

---
The Swagger for the application can be enabled by uncommenting the swagger dependancy in [pom.xml](https://github.com/jeetinani/FileSaver/blob/main/pom.xml#L51) and can be accessed at hostname/swagger-ui/index.html