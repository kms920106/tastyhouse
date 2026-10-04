package com.tastyhouse.infrastructure.persistence.order.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

interface OrderProductOptionJpaRepository extends JpaRepository<OrderProductOptionJpaEntity, Long> {
}
