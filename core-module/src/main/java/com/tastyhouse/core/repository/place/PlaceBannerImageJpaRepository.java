package com.tastyhouse.core.repository.place;

import com.tastyhouse.core.entity.place.PlaceBannerImage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PlaceBannerImageJpaRepository extends JpaRepository<PlaceBannerImage, Long> {
}