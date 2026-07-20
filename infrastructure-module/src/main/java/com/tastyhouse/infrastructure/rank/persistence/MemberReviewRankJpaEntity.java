package com.tastyhouse.infrastructure.rank.persistence;

import java.time.LocalDate;
import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Convert;
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

import com.tastyhouse.core.domain.member.domain.vo.MemberId;
import com.tastyhouse.core.domain.rank.domain.model.RankType;
import com.tastyhouse.infrastructure.member.persistence.MemberIdConverter;
import com.tastyhouse.infrastructure.shared.persistence.BaseEntity;

/**
 * 회원 리뷰 랭킹 JPA 영속 모델.
 *
 * <p>순수 도메인 모델 {@code MemberReviewRank}와 분리된 영속 전용 엔티티다. DB 매핑(테이블/컬럼/감사 필드)만
 * 담당하고 비즈니스 행위는 갖지 않는다. 도메인↔엔티티 변환은 {@code MemberReviewRankMapper}가 수행한다.
 * 상태전이·삭제가 없는 insert-only 애그리거트라 update용 {@code applyChanges}는 두지 않는다.
 */
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
public class MemberReviewRankJpaEntity extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Convert(converter = MemberIdConverter.class)
    @Column(name = "member_id", nullable = false)
    private MemberId memberId;

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

    private MemberReviewRankJpaEntity(
        MemberId memberId,
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

    /**
     * 신규 저장용 엔티티를 생성한다(식별자 없음). {@code MemberReviewRankMapper#toEntity}에서만 호출한다.
     */
    static MemberReviewRankJpaEntity create(
        MemberId memberId,
        Integer reviewCount,
        Integer rankNo,
        RankType rankType,
        LocalDate baseDate,
        LocalDateTime lastReviewAt
    ) {
        return new MemberReviewRankJpaEntity(memberId, reviewCount, rankNo, rankType, baseDate, lastReviewAt);
    }
}
