package com.tastyhouse.infrastructure.persistence.review.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

interface ReviewBlindRequestAttachmentJpaRepository
    extends JpaRepository<ReviewBlindRequestAttachmentJpaEntity, Long> {
}
