package com.tastyhouse.core.domain.rank.infrastructure.persistence;

import com.tastyhouse.core.domain.rank.domain.model.MemberReviewRank;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MemberReviewRankJpaRepository extends JpaRepository<MemberReviewRank, Long> {
}
