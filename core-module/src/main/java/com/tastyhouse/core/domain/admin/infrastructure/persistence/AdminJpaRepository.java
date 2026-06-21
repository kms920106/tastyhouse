package com.tastyhouse.core.domain.admin.infrastructure.persistence;

import com.tastyhouse.core.domain.admin.domain.model.Admin;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AdminJpaRepository extends JpaRepository<Admin, Long> {

    Optional<Admin> findByUsername(String username);

    boolean existsByUsername(String username);
}
