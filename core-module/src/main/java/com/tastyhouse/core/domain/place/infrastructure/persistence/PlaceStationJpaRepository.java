package com.tastyhouse.core.domain.place.infrastructure.persistence;

import com.tastyhouse.core.domain.place.domain.model.PlaceStation;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PlaceStationJpaRepository extends JpaRepository<PlaceStation, Long> {
}
