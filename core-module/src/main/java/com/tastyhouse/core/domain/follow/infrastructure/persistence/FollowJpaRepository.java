package com.tastyhouse.core.domain.follow.infrastructure.persistence;

import com.tastyhouse.core.domain.follow.domain.model.Follow;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FollowJpaRepository extends JpaRepository<Follow, Long> {
}
