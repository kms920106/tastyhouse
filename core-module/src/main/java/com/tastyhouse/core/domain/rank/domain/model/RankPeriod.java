package com.tastyhouse.core.domain.rank.domain.model;

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

import com.tastyhouse.core.domain.rank.domain.vo.RankPeriodId;
import com.tastyhouse.core.shared.entity.BaseEntity;

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
public class RankPeriod extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "start_at", nullable = false)
    private LocalDateTime startAt;

    @Column(name = "end_at", nullable = false)
    private LocalDateTime endAt;

    @Column(name = "is_visible", nullable = false)
    private boolean visible;

    private RankPeriod(LocalDateTime startAt, LocalDateTime endAt, boolean visible) {
        this.startAt = startAt;
        this.endAt = endAt;
        this.visible = visible;
    }

    public static RankPeriod of(LocalDateTime startAt, LocalDateTime endAt) {
        return new RankPeriod(startAt, endAt, true);
    }

    public static RankPeriod of(LocalDateTime startAt, LocalDateTime endAt, boolean visible) {
        return new RankPeriod(startAt, endAt, visible);
    }

    public RankPeriodId getRankPeriodId() {
        return RankPeriodId.of(this.id);
    }

    public void update(LocalDateTime startAt, LocalDateTime endAt, boolean visible) {
        this.startAt = startAt;
        this.endAt = endAt;
        this.visible = visible;
    }
}
