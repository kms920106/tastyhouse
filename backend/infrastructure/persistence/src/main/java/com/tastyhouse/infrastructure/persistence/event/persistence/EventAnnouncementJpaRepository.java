package com.tastyhouse.infrastructure.persistence.event.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

public interface EventAnnouncementJpaRepository extends JpaRepository<EventAnnouncementJpaEntity, Long> {
}
