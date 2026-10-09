package com.tastyhouse.infrastructure.jpa.file.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

interface UploadedFileJpaRepository extends JpaRepository<UploadedFileJpaEntity, Long> {
}
