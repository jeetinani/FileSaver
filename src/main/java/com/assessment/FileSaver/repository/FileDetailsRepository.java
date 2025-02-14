package com.assessment.FileSaver.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.assessment.FileSaver.domain.FileDetails;

@Repository
public interface FileDetailsRepository extends JpaRepository<FileDetails, UUID> {

    public List<UUID> findBySavedAtBefore(LocalDateTime ldt);
}
