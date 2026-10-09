package com.tastyhouse.infrastructure.jpa.event.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

interface EventAnnouncementJpaRepository extends JpaRepository<EventAnnouncementJpaEntity, Long> {
}
