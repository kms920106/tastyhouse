package com.tastyhouse.infrastructure.persistence.file.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

public interface UploadedFileJpaRepository extends JpaRepository<UploadedFileJpaEntity, Long> {
}
