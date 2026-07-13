package com.tastyhouse.core.domain.banner.infrastructure.persistence;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.tastyhouse.core.domain.banner.domain.model.Banner;

public interface BannerJpaRepository extends JpaRepository<Banner, Long> {

    Optional<Banner> findByIdAndDeletedFalse(Long id);
}
