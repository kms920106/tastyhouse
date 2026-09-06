package com.tastyhouse.domain.rank.model;

import java.time.LocalDateTime;

import com.tastyhouse.domain.rank.vo.RankPeriodId;

public class RankPeriod {
    private final Long id;
    private LocalDateTime startAt;
    private LocalDateTime endAt;
    private boolean visible;
    private boolean deleted;
    private final LocalDateTime createdAt;
    private final LocalDateTime updatedAt;

    private RankPeriod(
        Long id,
        LocalDateTime startAt,
        LocalDateTime endAt,
        boolean visible,
        boolean deleted,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
    ) {
        this.id = id;
        this.startAt = startAt;
        this.endAt = endAt;
        this.visible = visible;
        this.deleted = deleted;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public static RankPeriod of(LocalDateTime startAt, LocalDateTime endAt) {
        return new RankPeriod(null, startAt, endAt, true, false, null, null);
    }

    public static RankPeriod of(LocalDateTime startAt, LocalDateTime endAt, boolean visible) {
        return new RankPeriod(null, startAt, endAt, visible, false, null, null);
    }

    public static RankPeriod reconstitute(
        Long id,
        LocalDateTime startAt,
        LocalDateTime endAt,
        boolean visible,
        boolean deleted,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
    ) {
        return new RankPeriod(id, startAt, endAt, visible, deleted, createdAt, updatedAt);
    }

    public RankPeriodId getRankPeriodId() {
        return RankPeriodId.of(this.id);
    }

    public void update(LocalDateTime startAt, LocalDateTime endAt, boolean visible) {
        this.startAt = startAt;
        this.endAt = endAt;
        this.visible = visible;
    }

    public void delete() {
        this.deleted = true;
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

    public LocalDateTime getCreatedAt() {
        return this.createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return this.updatedAt;
    }
}
