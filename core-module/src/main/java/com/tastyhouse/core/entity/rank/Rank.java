package com.tastyhouse.core.entity.rank;

import com.tastyhouse.core.entity.BaseEntity;
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
    name = "RANKS",
    indexes = {
        @Index(name = "idx_rank_active", columnList = "is_active"),
        @Index(name = "idx_rank_period", columnList = "start_at, end_at")
    }
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Rank extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; // PK

    @Column(name = "start_at", nullable = false)
    private LocalDateTime startAt; // 랭킹 시작 일시

    @Column(name = "end_at", nullable = false)
    private LocalDateTime endAt; // 랭킹 종료 일시

    @Column(name = "is_active", nullable = false)
    private Boolean isActive; // 활성화 여부 (true: 활성)

    private Rank(LocalDateTime startAt, LocalDateTime endAt, Boolean isActive) {
        this.startAt = startAt;
        this.endAt = endAt;
        this.isActive = isActive;
    }

    public static Rank of(LocalDateTime startAt, LocalDateTime endAt) {
        return new Rank(startAt, endAt, true);
    }
}
