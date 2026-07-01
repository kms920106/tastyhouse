package com.tastyhouse.core.domain.follow.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import com.tastyhouse.core.domain.follow.domain.model.Follow;

public interface FollowJpaRepository extends JpaRepository<Follow, Long> {
}
