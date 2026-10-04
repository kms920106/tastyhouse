package com.tastyhouse.infrastructure.persistence.point.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;

import com.tastyhouse.infrastructure.persistence.shared.persistence.BaseEntity;

@Entity
@Table(
    name = "POINT_HISTORY",
    indexes = {
        @Index(name = "idx_point_history_member_id", columnList = "member_id"),
        @Index(name = "idx_point_history_created_at", columnList = "created_at")
    }
)
class PointHistoryJpaEntity extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "member_id", nullable = false)
    private Long memberId;

    @Column(name = "point_type", nullable = false, length = 50, columnDefinition = "VARCHAR(50)")
    private String pointType;

    @Column(name = "point_amount", nullable = false)
    private Integer pointAmount;

    @Column(name = "reason", nullable = false, length = 200)
    private String reason;

    protected PointHistoryJpaEntity() {
    }

    private PointHistoryJpaEntity(Long memberId, String pointType, Integer pointAmount, String reason) {
        this.memberId = memberId;
        this.pointType = pointType;
        this.pointAmount = pointAmount;
        this.reason = reason;
    }

    static PointHistoryJpaEntity create(Long memberId, String pointType, Integer pointAmount, String reason) {
        return new PointHistoryJpaEntity(memberId, pointType, pointAmount, reason);
    }

    public Long getId() {
        return this.id;
    }

    public Long getMemberId() {
        return this.memberId;
    }

    public String getPointType() {
        return this.pointType;
    }

    public Integer getPointAmount() {
        return this.pointAmount;
    }

    public String getReason() {
        return this.reason;
    }
}
