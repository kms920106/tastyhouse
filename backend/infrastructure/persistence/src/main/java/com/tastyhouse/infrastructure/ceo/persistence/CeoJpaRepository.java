package com.tastyhouse.infrastructure.ceo.persistence;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CeoJpaRepository extends JpaRepository<CeoJpaEntity, Long> {

    Optional<CeoJpaEntity> findByUsername(String username);

    boolean existsByUsername(String username);
}
