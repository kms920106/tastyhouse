package com.tastyhouse.core.domain.place.infrastructure.persistence;

import com.tastyhouse.core.domain.place.domain.model.PlaceOrderMethod;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PlaceOrderMethodJpaRepository extends JpaRepository<PlaceOrderMethod, Long> {
}
