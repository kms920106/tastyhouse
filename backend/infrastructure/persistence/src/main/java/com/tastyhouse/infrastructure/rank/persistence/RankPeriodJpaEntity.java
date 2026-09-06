package com.tastyhouse.infrastructure.rank.persistence;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;

import com.tastyhouse.infrastructure.shared.persistence.BaseEntity;

@Entity
@Table(
    name = "RANK_PERIOD",
    indexes = {
        @Index(name = "idx_rank_period_active", columnList = "is_visible"),
        @Index(name = "idx_rank_period_range", columnList = "start_at, end_at")
    }
)
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

    protected RankPeriodJpaEntity() {
    }

    private RankPeriodJpaEntity(LocalDateTime startAt, LocalDateTime endAt, boolean visible, boolean deleted) {
        this.startAt = startAt;
        this.endAt = endAt;
        this.visible = visible;
        this.deleted = deleted;
    }

    static RankPeriodJpaEntity create(LocalDateTime startAt, LocalDateTime endAt, boolean visible, boolean deleted) {
        return new RankPeriodJpaEntity(startAt, endAt, visible, deleted);
    }

    void applyChanges(LocalDateTime startAt, LocalDateTime endAt, boolean visible, boolean deleted) {
        this.startAt = startAt;
        this.endAt = endAt;
        this.visible = visible;
        this.deleted = deleted;
    }

    public Long getId() {
        return this.id;
    }

    public LocalDateTime getStartAt() {
        return this.startAt;
    }

    public LocalDateTime getEndAt() {
        return this.endAt;
    }

    public boolean isVisible() {
        return this.visible;
    }

    public boolean isDeleted() {
        return this.deleted;
    }
}
