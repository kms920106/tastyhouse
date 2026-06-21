package com.tastyhouse.core.domain.rank.domain.model;

import com.tastyhouse.core.shared.entity.BaseEntity;
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
    private Boolean isVisible;

    private RankPeriod(LocalDateTime startAt, LocalDateTime endAt, Boolean isVisible) {
        this.startAt = startAt;
        this.endAt = endAt;
        this.isVisible = isVisible;
    }

    public static RankPeriod of(LocalDateTime startAt, LocalDateTime endAt) {
        return new RankPeriod(startAt, endAt, true);
    }
}
