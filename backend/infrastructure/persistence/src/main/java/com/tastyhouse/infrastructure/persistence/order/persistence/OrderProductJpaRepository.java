package com.tastyhouse.infrastructure.persistence.order.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderProductJpaRepository extends JpaRepository<OrderProductJpaEntity, Long> {
}
