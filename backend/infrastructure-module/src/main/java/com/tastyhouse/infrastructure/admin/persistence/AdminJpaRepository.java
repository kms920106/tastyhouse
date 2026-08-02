package com.tastyhouse.infrastructure.admin.persistence;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AdminJpaRepository extends JpaRepository<AdminJpaEntity, Long> {

    Optional<AdminJpaEntity> findByUsername(String username);

    boolean existsByUsername(String username);
}
