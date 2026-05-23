package com.tastyhouse.core.entity.point;

import com.tastyhouse.core.entity.BaseEntity;
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
    private Long id; // PK

    @Column(name = "member_id", nullable = false, unique = true)
    private Long memberId; // 회원 ID (MEMBER.id 참조)

    @Column(name = "available_points", nullable = false)
    private Integer availablePoints = 0; // 사용 가능한 포인트 잔액

    @Column(name = "expired_this_month", nullable = false)
    private Integer expiredThisMonth = 0; // 이번 달 소멸 예정 포인트

    private MemberPoint(
        Long memberId,
        Integer availablePoints,
        Integer expiredThisMonth
    ) {
        this.memberId = memberId;
        this.availablePoints = availablePoints != null ? availablePoints : 0;
        this.expiredThisMonth = expiredThisMonth != null ? expiredThisMonth : 0;
    }

    public static MemberPoint of(Long memberId) {
        return new MemberPoint(
            memberId,
            0,
            0
        );
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
