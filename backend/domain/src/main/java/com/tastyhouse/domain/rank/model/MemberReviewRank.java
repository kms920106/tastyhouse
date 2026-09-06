package com.tastyhouse.domain.rank.model;

import java.time.LocalDate;
import java.time.LocalDateTime;

import com.tastyhouse.domain.member.vo.MemberId;

public class MemberReviewRank {
    private final Long id;
    private final MemberId memberId;
    private final Integer reviewCount;
    private final Integer rankNo;
    private final RankType rankType;
    private final LocalDate baseDate;
    private final LocalDateTime lastReviewAt;
    private final LocalDateTime createdAt;
    private final LocalDateTime updatedAt;

    private MemberReviewRank(
        Long id,
        MemberId memberId,
        Integer reviewCount,
        Integer rankNo,
        RankType rankType,
        LocalDate baseDate,
        LocalDateTime lastReviewAt,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
    ) {
        this.id = id;
        this.memberId = memberId;
        this.reviewCount = reviewCount;
        this.rankNo = rankNo;
        this.rankType = rankType;
        this.baseDate = baseDate;
        this.lastReviewAt = lastReviewAt;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public static MemberReviewRank of(
        MemberId memberId,
        Integer reviewCount,
        Integer rankNo,
        RankType rankType,
        LocalDate baseDate,
        LocalDateTime lastReviewAt
    ) {
        return new MemberReviewRank(null, memberId, reviewCount, rankNo, rankType, baseDate, lastReviewAt, null, null);
    }

    public static MemberReviewRank reconstitute(
        Long id,
        MemberId memberId,
        Integer reviewCount,
        Integer rankNo,
        RankType rankType,
        LocalDate baseDate,
        LocalDateTime lastReviewAt,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
    ) {
        return new MemberReviewRank(id, memberId, reviewCount, rankNo, rankType, baseDate, lastReviewAt, createdAt, updatedAt);
    }

    public Long getId() {
        return this.id;
    }

    public MemberId getMemberId() {
        return this.memberId;
    }

    public Integer getReviewCount() {
        return this.reviewCount;
    }

    public Integer getRankNo() {
        return this.rankNo;
    }

    public RankType getRankType() {
        return this.rankType;
    }

    public LocalDate getBaseDate() {
        return this.baseDate;
    }

    public LocalDateTime getLastReviewAt() {
        return this.lastReviewAt;
    }

    public LocalDateTime getCreatedAt() {
        return this.createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return this.updatedAt;
    }
}
