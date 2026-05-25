package com.tastyhouse.core.domain.place.infrastructure.persistence;

import com.tastyhouse.core.domain.place.domain.model.PlaceAmenity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PlaceAmenityJpaRepository extends JpaRepository<PlaceAmenity, Long> {
}
