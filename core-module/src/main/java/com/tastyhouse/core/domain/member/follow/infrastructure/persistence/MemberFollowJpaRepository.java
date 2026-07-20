package com.tastyhouse.core.domain.member.follow.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import com.tastyhouse.core.domain.member.follow.domain.model.MemberFollow;

public interface MemberFollowJpaRepository extends JpaRepository<MemberFollow, Long> {
}
