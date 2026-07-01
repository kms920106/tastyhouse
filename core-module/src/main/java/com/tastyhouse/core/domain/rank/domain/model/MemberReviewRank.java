package com.tastyhouse.core.domain.rank.domain.model;

import java.time.LocalDate;
import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import com.tastyhouse.core.shared.entity.BaseEntity;

@Getter
@Entity
@Table(
    name = "MEMBER_REVIEW_RANK",
    uniqueConstraints = {
        @UniqueConstraint(
            name = "uk_member_rank",
            columnNames = {"member_id", "rank_type", "base_date"}
        )
    },
    indexes = {
        @Index(name = "idx_rank_query", columnList = "rank_type, base_date, rank_no"),
        @Index(name = "idx_member_rank", columnList = "member_id, rank_type")
    }
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MemberReviewRank extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "member_id", nullable = false)
    private Long memberId;

    @Column(name = "review_count", nullable = false)
    private Integer reviewCount;

    @Column(name = "rank_no", nullable = false)
    private Integer rankNo;

    @Enumerated(EnumType.STRING)
    @Column(name = "rank_type", nullable = false, length = 20, columnDefinition = "VARCHAR(20)")
    private RankType rankType;

    @Column(name = "base_date", nullable = false)
    private LocalDate baseDate;

    @Column(name = "last_review_at")
    private LocalDateTime lastReviewAt;

    private MemberReviewRank(
        Long memberId,
        Integer reviewCount,
        Integer rankNo,
        RankType rankType,
        LocalDate baseDate,
        LocalDateTime lastReviewAt
    ) {
        this.memberId = memberId;
        this.reviewCount = reviewCount;
        this.rankNo = rankNo;
        this.rankType = rankType;
        this.baseDate = baseDate;
        this.lastReviewAt = lastReviewAt;
    }

    public static MemberReviewRank of(
        Long memberId,
        Integer reviewCount,
        Integer rankNo,
        RankType rankType,
        LocalDate baseDate,
        LocalDateTime lastReviewAt
    ) {
        return new MemberReviewRank(memberId, reviewCount, rankNo, rankType, baseDate, lastReviewAt);
    }
}
