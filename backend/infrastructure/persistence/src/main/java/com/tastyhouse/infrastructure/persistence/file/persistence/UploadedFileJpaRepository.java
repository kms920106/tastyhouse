package com.tastyhouse.infrastructure.persistence.file.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

interface UploadedFileJpaRepository extends JpaRepository<UploadedFileJpaEntity, Long> {
}
