package com.tastyhouse.infrastructure.persistence.notification.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

interface NotificationJpaRepository extends JpaRepository<NotificationJpaEntity, Long> {
}
