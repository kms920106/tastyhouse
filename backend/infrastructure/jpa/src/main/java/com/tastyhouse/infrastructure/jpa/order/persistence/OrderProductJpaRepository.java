package com.tastyhouse.infrastructure.jpa.order.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

interface OrderProductJpaRepository extends JpaRepository<OrderProductJpaEntity, Long> {
}
