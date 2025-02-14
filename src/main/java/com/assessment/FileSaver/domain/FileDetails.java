package com.assessment.FileSaver.domain;

import java.time.LocalDateTime;
import java.util.UUID;

import org.hibernate.annotations.CreationTimestamp;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

@Entity
public class FileDetails {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID uuid;
    private String fileName;
    @CreationTimestamp
    private LocalDateTime savedAt;
    private int downloadCount;
    private boolean isActive;

    public FileDetails() {
    }

    public FileDetails(String fileName) {
        this.fileName = fileName;
        this.savedAt = LocalDateTime.now();
        this.downloadCount = 0;
        this.isActive = true;
    }

    public FileDetails(String fileName, UUID uuid, LocalDateTime savedAt, int downloadCount, boolean isActive) {
        this.fileName = fileName;
        this.uuid = uuid;
        this.savedAt = savedAt;
        this.downloadCount = downloadCount;
        this.isActive = isActive;
    }

    @Override
    public String toString() {
        return "SavedFile [fileName=" + fileName + ", uuid=" + uuid + ", savedAt=" + savedAt + ", downloadCount="
                + downloadCount + ", isActive=" + isActive + "]";
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public UUID getUuid() {
        return uuid;
    }

    public void setUuid(UUID uuid) {
        this.uuid = uuid;
    }

    public LocalDateTime getSavedAt() {
        return savedAt;
    }

    public void setSavedAt(LocalDateTime savedAt) {
        this.savedAt = savedAt;
    }

    public int getDownloadCount() {
        return downloadCount;
    }

    public void setDownloadCount(int downloadCount) {
        this.downloadCount = downloadCount;
    }

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean isActive) {
        this.isActive = isActive;
    }

}
