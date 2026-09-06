package com.tastyhouse.infrastructure.point.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;

import com.tastyhouse.domain.point.model.PointType;
import com.tastyhouse.infrastructure.shared.persistence.BaseEntity;

@Entity
@Table(
    name = "POINT_HISTORY",
    indexes = {
        @Index(name = "idx_point_history_member_id", columnList = "member_id"),
        @Index(name = "idx_point_history_created_at", columnList = "created_at")
    }
)
public class PointHistoryJpaEntity extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "member_id", nullable = false)
    private Long memberId;

    @Enumerated(EnumType.STRING)
    @Column(name = "point_type", nullable = false, length = 50, columnDefinition = "VARCHAR(50)")
    private PointType pointType;

    @Column(name = "point_amount", nullable = false)
    private Integer pointAmount;

    @Column(name = "reason", nullable = false, length = 200)
    private String reason;

    protected PointHistoryJpaEntity() {
    }

    private PointHistoryJpaEntity(Long memberId, PointType pointType, Integer pointAmount, String reason) {
        this.memberId = memberId;
        this.pointType = pointType;
        this.pointAmount = pointAmount;
        this.reason = reason;
    }

    static PointHistoryJpaEntity create(Long memberId, PointType pointType, Integer pointAmount, String reason) {
        return new PointHistoryJpaEntity(memberId, pointType, pointAmount, reason);
    }

    public Long getId() {
        return this.id;
    }

    public Long getMemberId() {
        return this.memberId;
    }

    public PointType getPointType() {
        return this.pointType;
    }

    public Integer getPointAmount() {
        return this.pointAmount;
    }

    public String getReason() {
        return this.reason;
    }
}
