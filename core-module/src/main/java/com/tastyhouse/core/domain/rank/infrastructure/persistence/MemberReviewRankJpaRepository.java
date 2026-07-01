package com.tastyhouse.core.domain.rank.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import com.tastyhouse.core.domain.rank.domain.model.MemberReviewRank;

public interface MemberReviewRankJpaRepository extends JpaRepository<MemberReviewRank, Long> {
}
