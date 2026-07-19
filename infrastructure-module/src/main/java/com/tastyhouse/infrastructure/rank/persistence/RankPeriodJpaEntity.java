package com.tastyhouse.infrastructure.rank.persistence;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import com.tastyhouse.core.shared.entity.BaseEntity;

/**
 * 랭킹 기간 JPA 영속 모델.
 *
 * <p>순수 도메인 모델 {@code RankPeriod}와 분리된 영속 전용 엔티티다. DB 매핑(테이블/컬럼/감사 필드)만
 * 담당하고 비즈니스 행위는 갖지 않는다. 도메인↔엔티티 변환은 {@code RankPeriodMapper}가 수행한다.
 */
@Getter
@Entity
@Table(
    name = "RANK_PERIOD",
    indexes = {
        @Index(name = "idx_rank_period_active", columnList = "is_visible"),
        @Index(name = "idx_rank_period_range", columnList = "start_at, end_at")
    }
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class RankPeriodJpaEntity extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "start_at", nullable = false)
    private LocalDateTime startAt;

    @Column(name = "end_at", nullable = false)
    private LocalDateTime endAt;

    @Column(name = "is_visible", nullable = false)
    private boolean visible;

    @Column(name = "is_deleted", nullable = false)
    private boolean deleted;

    private RankPeriodJpaEntity(LocalDateTime startAt, LocalDateTime endAt, boolean visible, boolean deleted) {
        this.startAt = startAt;
        this.endAt = endAt;
        this.visible = visible;
        this.deleted = deleted;
    }

    /**
     * 신규 저장용 엔티티를 생성한다(식별자 없음). {@code RankPeriodMapper#toEntity}에서만 호출한다.
     */
    static RankPeriodJpaEntity create(LocalDateTime startAt, LocalDateTime endAt, boolean visible, boolean deleted) {
        return new RankPeriodJpaEntity(startAt, endAt, visible, deleted);
    }

    /**
     * managed 엔티티에 도메인의 변경 필드를 복사한다(update용 dirty checking 대체). 감사 필드·식별자는 건드리지 않는다.
     */
    void applyChanges(LocalDateTime startAt, LocalDateTime endAt, boolean visible, boolean deleted) {
        this.startAt = startAt;
        this.endAt = endAt;
        this.visible = visible;
        this.deleted = deleted;
    }
}
