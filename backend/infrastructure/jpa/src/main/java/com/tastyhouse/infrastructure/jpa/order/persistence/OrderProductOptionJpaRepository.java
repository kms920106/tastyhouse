package com.tastyhouse.infrastructure.jpa.order.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

interface OrderProductOptionJpaRepository extends JpaRepository<OrderProductOptionJpaEntity, Long> {
}
