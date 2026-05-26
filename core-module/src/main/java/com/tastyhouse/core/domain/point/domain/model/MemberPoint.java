package com.tastyhouse.core.domain.point.domain.model;

import com.tastyhouse.core.domain.point.domain.vo.MemberPointId;
import com.tastyhouse.core.shared.entity.BaseEntity;
import com.tastyhouse.core.exception.BusinessException;
import com.tastyhouse.core.exception.ErrorCode;
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

@Getter
@Entity
@Table(
    name = "MEMBER_POINT",
    indexes = {
        @Index(name = "idx_member_point_member_id", columnList = "member_id")
    }
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MemberPoint extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "member_id", nullable = false, unique = true)
    private Long memberId;

    @Column(name = "available_points", nullable = false)
    private Integer availablePoints = 0;

    @Column(name = "expired_this_month", nullable = false)
    private Integer expiredThisMonth = 0;

    private MemberPoint(Long memberId, Integer availablePoints, Integer expiredThisMonth) {
        this.memberId = memberId;
        this.availablePoints = availablePoints != null ? availablePoints : 0;
        this.expiredThisMonth = expiredThisMonth != null ? expiredThisMonth : 0;
    }

    public static MemberPoint of(Long memberId) {
        return new MemberPoint(memberId, 0, 0);
    }

    public MemberPointId getMemberPointId() {
        return new MemberPointId(this.id);
    }

    public void addPoints(Integer amount) {
        this.availablePoints += amount;
    }

    public void deductPoints(Integer amount) {
        if (this.availablePoints < amount) {
            throw new BusinessException(ErrorCode.POINT_INSUFFICIENT);
        }
        this.availablePoints -= amount;
    }
}
