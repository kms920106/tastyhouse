package com.tastyhouse.infrastructure.persistence.order.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

interface OrderProductJpaRepository extends JpaRepository<OrderProductJpaEntity, Long> {
}
