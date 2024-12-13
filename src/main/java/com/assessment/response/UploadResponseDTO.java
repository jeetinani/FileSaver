package com.assessment.response;

public class UploadResponseDTO {
    
    private String uploadStatus;
    private String retrievePath;

    
    public UploadResponseDTO(String uploadStatus, String retrievePath) {
        this.uploadStatus = uploadStatus;
        this.retrievePath = retrievePath;
    }
    public String getUploadStatus() {
        return uploadStatus;
    }
    public void setUploadStatus(String uploadStatus) {
        this.uploadStatus = uploadStatus;
    }
    public String getRetrievePath() {
        return retrievePath;
    }
    public void setRetrievePath(String retrievePath) {
        this.retrievePath = retrievePath;
    }

    
}
