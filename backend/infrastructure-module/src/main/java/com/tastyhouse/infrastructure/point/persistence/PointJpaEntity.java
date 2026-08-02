package com.tastyhouse.infrastructure.point.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;

import com.tastyhouse.domain.member.vo.MemberId;
import com.tastyhouse.infrastructure.member.persistence.MemberIdConverter;
import com.tastyhouse.infrastructure.shared.persistence.BaseEntity;

/**
 * 회원 포인트 JPA 영속 모델.
 *
 * <p>순수 도메인 모델 {@code Point}와 분리된 영속 전용 엔티티다. DB 매핑(테이블/컬럼/감사 필드)만
 * 담당하고 비즈니스 행위는 갖지 않는다. 도메인↔엔티티 변환은 {@code PointMapper}가 수행한다.
 */
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

    @Convert(converter = MemberIdConverter.class)
    @Column(name = "member_id", nullable = false, unique = true)
    private MemberId memberId;

    @Column(name = "available_points", nullable = false)
    private Integer availablePoints;

    @Column(name = "expired_this_month", nullable = false)
    private Integer expiredThisMonth;

    protected PointJpaEntity() {
    }

    private PointJpaEntity(MemberId memberId, Integer availablePoints, Integer expiredThisMonth) {
        this.memberId = memberId;
        this.availablePoints = availablePoints;
        this.expiredThisMonth = expiredThisMonth;
    }

    /**
     * 신규 저장용 엔티티를 생성한다(식별자 없음). {@code PointMapper#toEntity}에서만 호출한다.
     */
    static PointJpaEntity create(MemberId memberId, Integer availablePoints, Integer expiredThisMonth) {
        return new PointJpaEntity(memberId, availablePoints, expiredThisMonth);
    }

    /**
     * managed 엔티티에 도메인의 변경 필드를 복사한다(update용 dirty checking 대체). 감사 필드·식별자는 건드리지 않는다.
     */
    void applyChanges(Integer availablePoints, Integer expiredThisMonth) {
        this.availablePoints = availablePoints;
        this.expiredThisMonth = expiredThisMonth;
    }

    public Long getId() {
        return this.id;
    }

    public MemberId getMemberId() {
        return this.memberId;
    }

    public Integer getAvailablePoints() {
        return this.availablePoints;
    }

    public Integer getExpiredThisMonth() {
        return this.expiredThisMonth;
    }
}
