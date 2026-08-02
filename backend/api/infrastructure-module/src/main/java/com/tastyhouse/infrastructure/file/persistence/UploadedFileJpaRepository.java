package com.tastyhouse.infrastructure.file.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

public interface UploadedFileJpaRepository extends JpaRepository<UploadedFileJpaEntity, Long> {
}
