package com.tastyhouse.infrastructure.point.persistence;

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
    name = "POINT",
    indexes = {
        @Index(name = "idx_point_member_id", columnList = "member_id")
    }
)
public class PointJpaEntity extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "member_id", nullable = false, unique = true)
    private Long memberId;

    @Column(name = "available_points", nullable = false)
    private Integer availablePoints;

    @Column(name = "expired_this_month", nullable = false)
    private Integer expiredThisMonth;

    protected PointJpaEntity() {
    }

    private PointJpaEntity(Long memberId, Integer availablePoints, Integer expiredThisMonth) {
        this.memberId = memberId;
        this.availablePoints = availablePoints;
        this.expiredThisMonth = expiredThisMonth;
    }

    static PointJpaEntity create(Long memberId, Integer availablePoints, Integer expiredThisMonth) {
        return new PointJpaEntity(memberId, availablePoints, expiredThisMonth);
    }

    void applyChanges(Integer availablePoints, Integer expiredThisMonth) {
        this.availablePoints = availablePoints;
        this.expiredThisMonth = expiredThisMonth;
    }

    public Long getId() {
        return this.id;
    }

    public Long getMemberId() {
        return this.memberId;
    }

    public Integer getAvailablePoints() {
        return this.availablePoints;
    }

    public Integer getExpiredThisMonth() {
        return this.expiredThisMonth;
    }
}
