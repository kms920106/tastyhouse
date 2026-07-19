package com.tastyhouse.infrastructure.order.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderProductOptionJpaRepository extends JpaRepository<OrderProductOptionJpaEntity, Long> {
}
