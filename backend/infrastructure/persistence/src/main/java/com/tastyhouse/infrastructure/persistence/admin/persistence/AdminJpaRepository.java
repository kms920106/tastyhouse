package com.tastyhouse.infrastructure.persistence.admin.persistence;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
interface AdminJpaRepository extends JpaRepository<AdminJpaEntity, Long> {

    Optional<AdminJpaEntity> findByUsername(String username);

    boolean existsByUsername(String username);
}
