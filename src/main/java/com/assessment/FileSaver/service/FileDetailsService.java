package com.assessment.FileSaver.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.assessment.FileSaver.domain.FileDetails;
import com.assessment.FileSaver.repository.FileDetailsRepository;

@Service
public class FileDetailsService {
    private FileDetailsRepository fileDetailsRepository;

    public FileDetailsService(FileDetailsRepository fileDetailsRepository) {
        this.fileDetailsRepository = fileDetailsRepository;
    }

    public FileDetails saveFileDetails(String originalFileName){
        return fileDetailsRepository.save(new FileDetails(originalFileName));
    }

    public boolean isFilePresent(UUID uuid){
        Optional<FileDetails> fileDetailsOptional = fileDetailsRepository.findById(uuid);
        return fileDetailsOptional.isPresent() && fileDetailsOptional.get().isActive();
    }

    public String getFileName(UUID uuid) {
        return fileDetailsRepository.findById(uuid).get().getFileName();
    }

    public void removeFile(UUID uuid){
        fileDetailsRepository.deleteById(uuid);
    }

    public List<UUID> getListOfExpiredFiles(int maxPermittedStorageHours){
        return fileDetailsRepository.findBySavedAtBefore(LocalDateTime.now().minusHours(maxPermittedStorageHours));
    }
}
