package com.tastyhouse.core.repository.place;

import com.tastyhouse.core.entity.place.PlaceBookmark;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PlaceBookmarkJpaRepository extends JpaRepository<PlaceBookmark, Long> {
}