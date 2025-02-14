package com.assessment.FileSaver.response;

import java.io.Serializable;

public class UploadResponseDTO implements Serializable {

    private String uploadStatus;
    private String retrievePath;

    public UploadResponseDTO() {
    }

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
