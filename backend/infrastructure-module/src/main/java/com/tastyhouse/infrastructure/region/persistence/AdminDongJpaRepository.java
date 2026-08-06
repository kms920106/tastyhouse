package com.tastyhouse.infrastructure.region.persistence;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface AdminDongJpaRepository extends JpaRepository<AdminDongJpaEntity, Long> {

    Optional<AdminDongJpaEntity> findBySidoNameAndSigunguNameAndDongName(String sidoName, String sigunguName, String dongName);
}
