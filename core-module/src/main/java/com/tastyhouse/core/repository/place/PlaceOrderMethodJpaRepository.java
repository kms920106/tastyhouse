package com.tastyhouse.core.repository.place;

import com.tastyhouse.core.entity.place.PlaceOrderMethod;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PlaceOrderMethodJpaRepository extends JpaRepository<PlaceOrderMethod, Long> {
}
