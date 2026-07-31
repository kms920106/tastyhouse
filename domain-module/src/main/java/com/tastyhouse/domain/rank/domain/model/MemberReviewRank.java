package com.tastyhouse.domain.rank.domain.model;

import java.time.LocalDate;
import java.time.LocalDateTime;

import lombok.Getter;

import com.tastyhouse.domain.member.domain.vo.MemberId;

/**
 * 회원 리뷰 랭킹 순수 도메인 모델.
 *
 * <p>JPA/프레임워크에 의존하지 않는 POJO다. 영속화는 infrastructure-module의
 * {@code MemberReviewRankJpaEntity} + {@code MemberReviewRankMapper}가 담당한다. 상태전이·삭제가 없는
 * insert-only 애그리거트로, 배치 집계 시 매번 새로 생성되고 {@code deleteByRankTypeAndBaseDate}로
 * 일괄 정리된다.
 */
@Getter
public class MemberReviewRank {

    private final Long id; // null이면 아직 영속되지 않은 신규 상태
    private final MemberId memberId; // 회원 ID
    private final Integer reviewCount; // 리뷰 수
    private final Integer rankNo; // 순위
    private final RankType rankType; // 랭킹 유형 (ALL/MONTHLY/WEEKLY)
    private final LocalDate baseDate; // 집계 기준일
    private final LocalDateTime lastReviewAt; // 마지막 리뷰 작성 일시
    private final LocalDateTime createdAt; // DB 재구성 시에만 값 존재 (신규 생성 시 null)
    private final LocalDateTime updatedAt; // DB 재구성 시에만 값 존재 (신규 생성 시 null)

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

    /**
     * 신규 회원 리뷰 랭킹을 생성한다. 아직 영속되지 않았으므로 식별자·감사 시각은 없다.
     */
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

    /**
     * DB에 저장된 상태로부터 도메인 객체를 재구성한다. 영속 계층(infrastructure) 전용이며,
     * 불변식을 우회한 임의 생성을 막기 위해 이 팩토리로만 식별자·감사 시각을 주입한다.
     */
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
}
