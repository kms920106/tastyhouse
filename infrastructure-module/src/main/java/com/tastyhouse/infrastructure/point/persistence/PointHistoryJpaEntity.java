package com.tastyhouse.infrastructure.point.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Convert;
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

import com.tastyhouse.core.domain.member.domain.vo.MemberId;
import com.tastyhouse.core.domain.point.domain.model.PointType;
import com.tastyhouse.infrastructure.member.persistence.MemberIdConverter;
import com.tastyhouse.infrastructure.shared.persistence.BaseEntity;

/**
 * 회원 포인트 변동 이력 JPA 영속 모델.
 *
 * <p>순수 도메인 모델 {@code PointHistory}와 분리된 영속 전용 엔티티다. DB 매핑(테이블/컬럼/감사 필드)만
 * 담당하고 비즈니스 행위는 갖지 않는다. 도메인↔엔티티 변환은 {@code PointHistoryMapper}가 수행한다.
 */
@Getter
@Entity
@Table(
    name = "POINT_HISTORY",
    indexes = {
        @Index(name = "idx_point_history_member_id", columnList = "member_id"),
        @Index(name = "idx_point_history_created_at", columnList = "created_at")
    }
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PointHistoryJpaEntity extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Convert(converter = MemberIdConverter.class)
    @Column(name = "member_id", nullable = false)
    private MemberId memberId;

    @Enumerated(EnumType.STRING)
    @Column(name = "point_type", nullable = false, length = 50, columnDefinition = "VARCHAR(50)")
    private PointType pointType;

    @Column(name = "point_amount", nullable = false)
    private Integer pointAmount;

    @Column(name = "reason", nullable = false, length = 200)
    private String reason;

    private PointHistoryJpaEntity(MemberId memberId, PointType pointType, Integer pointAmount, String reason) {
        this.memberId = memberId;
        this.pointType = pointType;
        this.pointAmount = pointAmount;
        this.reason = reason;
    }

    /**
     * 신규 저장용 엔티티를 생성한다(식별자 없음, insert 전용). {@code PointHistoryMapper#toEntity}에서만 호출한다.
     */
    static PointHistoryJpaEntity create(MemberId memberId, PointType pointType, Integer pointAmount, String reason) {
        return new PointHistoryJpaEntity(memberId, pointType, pointAmount, reason);
    }
}
