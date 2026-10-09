package com.tastyhouse.infrastructure.jpa.banner.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

interface BannerJpaRepository extends JpaRepository<BannerJpaEntity, Long> {
}
