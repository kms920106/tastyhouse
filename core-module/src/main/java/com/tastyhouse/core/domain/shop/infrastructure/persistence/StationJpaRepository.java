package com.tastyhouse.core.domain.shop.infrastructure.persistence;

import com.tastyhouse.core.domain.shop.domain.model.Station;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StationJpaRepository extends JpaRepository<Station, Long> {
}
