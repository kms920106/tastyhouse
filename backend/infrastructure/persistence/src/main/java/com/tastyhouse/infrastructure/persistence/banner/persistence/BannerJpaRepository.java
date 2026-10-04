package com.tastyhouse.infrastructure.persistence.banner.persistence;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface BannerJpaRepository extends JpaRepository<BannerJpaEntity, Long> {

    Optional<BannerJpaEntity> findByIdAndDeletedFalse(Long id);
}
