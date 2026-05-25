package com.tastyhouse.core.domain.place.infrastructure.persistence;

import com.tastyhouse.core.domain.place.domain.model.PlaceFoodTypeCategory;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PlaceFoodTypeCategoryJpaRepository extends JpaRepository<PlaceFoodTypeCategory, Long> {
}
