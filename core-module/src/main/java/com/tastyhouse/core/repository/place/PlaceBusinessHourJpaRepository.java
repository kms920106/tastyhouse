package com.tastyhouse.core.repository.place;

import com.tastyhouse.core.entity.place.PlaceBusinessHour;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PlaceBusinessHourJpaRepository extends JpaRepository<PlaceBusinessHour, Long> {
}
