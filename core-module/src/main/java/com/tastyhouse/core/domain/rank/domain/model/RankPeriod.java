package com.tastyhouse.core.domain.rank.domain.model;

import com.tastyhouse.core.common.BaseEntity;
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

import java.time.LocalDateTime;

@Getter
@Entity
@Table(
    name = "RANK_PERIOD",
    indexes = {
        @Index(name = "idx_rank_period_active", columnList = "is_active"),
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

    @Column(name = "is_active", nullable = false)
    private Boolean isActive;

    private RankPeriod(LocalDateTime startAt, LocalDateTime endAt, Boolean isActive) {
        this.startAt = startAt;
        this.endAt = endAt;
        this.isActive = isActive;
    }

    public static RankPeriod of(LocalDateTime startAt, LocalDateTime endAt) {
        return new RankPeriod(startAt, endAt, true);
    }
}
