package com.tastyhouse.core.domain.file.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import com.tastyhouse.core.domain.file.domain.model.UploadedFile;

public interface UploadedFileJpaRepository extends JpaRepository<UploadedFile, Long> {
}
