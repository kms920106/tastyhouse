package com.tastyhouse.core.domain.banner.infrastructure.persistence;

import com.tastyhouse.core.domain.banner.domain.model.Banner;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BannerJpaRepository extends JpaRepository<Banner, Long> {
}
