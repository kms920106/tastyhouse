package com.tastyhouse.core.repository.place;

import com.tastyhouse.core.entity.place.PlacePhotoCategoryImage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PlacePhotoCategoryImageJpaRepository extends JpaRepository<PlacePhotoCategoryImage, Long> {
}
