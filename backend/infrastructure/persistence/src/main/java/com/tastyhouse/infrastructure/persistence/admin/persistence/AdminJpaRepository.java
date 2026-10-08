package com.tastyhouse.infrastructure.persistence.admin.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
interface AdminJpaRepository extends JpaRepository<AdminJpaEntity, Long> {
}
