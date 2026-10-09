package com.tastyhouse.infrastructure.jpa.order.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

interface OrderJpaRepository extends JpaRepository<OrderJpaEntity, Long> {
}
