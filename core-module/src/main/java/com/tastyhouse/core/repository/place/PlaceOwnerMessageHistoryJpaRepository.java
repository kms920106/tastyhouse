package com.tastyhouse.core.repository.place;

import com.tastyhouse.core.entity.place.PlaceOwnerMessageHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PlaceOwnerMessageHistoryJpaRepository extends JpaRepository<PlaceOwnerMessageHistory, Long> {
}
