package com.tastyhouse.core.domain.member.follow.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import com.tastyhouse.core.domain.member.follow.domain.model.Follow;

public interface FollowJpaRepository extends JpaRepository<Follow, Long> {
}
