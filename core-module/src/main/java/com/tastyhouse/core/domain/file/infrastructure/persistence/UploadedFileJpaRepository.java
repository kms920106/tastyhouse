package com.tastyhouse.core.domain.file.infrastructure.persistence;

import com.tastyhouse.core.domain.file.domain.model.UploadedFile;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UploadedFileJpaRepository extends JpaRepository<UploadedFile, Long> {
}
