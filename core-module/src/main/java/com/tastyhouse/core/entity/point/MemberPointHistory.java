package com.tastyhouse.core.entity.point;

import com.tastyhouse.core.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
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
    name = "MEMBER_POINT_HISTORY",
    indexes = {
        @Index(name = "idx_member_point_history_member_id", columnList = "member_id"),
        @Index(name = "idx_member_point_history_created_at", columnList = "created_at")
    }
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MemberPointHistory extends BaseEntity {

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

    private MemberPointHistory(
        Long memberId,
        PointType pointType,
        Integer pointAmount,
        String reason
    ) {
        this.memberId = memberId;
        this.pointType = pointType;
        this.pointAmount = pointAmount;
        this.reason = reason;
    }

    public static MemberPointHistory of(
        Long memberId,
        PointType pointType,
        Integer pointAmount,
        String reason
    ) {
        return new MemberPointHistory(
            memberId,
            pointType,
            pointAmount,
            reason
        );
    }
}
