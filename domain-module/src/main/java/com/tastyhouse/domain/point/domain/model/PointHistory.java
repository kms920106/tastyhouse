package com.tastyhouse.domain.point.domain.model;

import java.time.LocalDateTime;

import com.tastyhouse.domain.member.domain.vo.MemberId;

/**
 * 회원 포인트 변동 이력 순수 도메인 모델.
 *
 * <p>JPA/프레임워크에 의존하지 않는 POJO다. 영속화는 infrastructure-module의
 * {@code PointHistoryJpaEntity} + {@code PointHistoryMapper}가 담당한다.
 * 변경 없이 신규 생성만 존재하는 insert 전용 이력이므로 상태전이 메서드는 없다.
 */
public class PointHistory {

    private final Long id; // null이면 아직 영속되지 않은 신규 상태
    private final MemberId memberId;
    private final PointType pointType;
    private final Integer pointAmount;
    private final String reason;
    private final LocalDateTime createdAt; // DB 재구성 시에만 값 존재 (신규 생성 시 null)

    private PointHistory(
        Long id,
        MemberId memberId,
        PointType pointType,
        Integer pointAmount,
        String reason,
        LocalDateTime createdAt
    ) {
        this.id = id;
        this.memberId = memberId;
        this.pointType = pointType;
        this.pointAmount = pointAmount;
        this.reason = reason;
        this.createdAt = createdAt;
    }

    /**
     * 신규 포인트 이력을 생성한다. 아직 영속되지 않았으므로 식별자·감사 시각은 없다.
     */
    public static PointHistory of(MemberId memberId, PointType pointType, Integer pointAmount, String reason) {
        return new PointHistory(null, memberId, pointType, pointAmount, reason, null);
    }

    /**
     * DB에 저장된 상태로부터 도메인 객체를 재구성한다. 영속 계층(infrastructure) 전용이며,
     * 불변식을 우회한 임의 생성을 막기 위해 이 팩토리로만 식별자·감사 시각을 주입한다.
     */
    public static PointHistory reconstitute(
        Long id,
        MemberId memberId,
        PointType pointType,
        Integer pointAmount,
        String reason,
        LocalDateTime createdAt
    ) {
        return new PointHistory(id, memberId, pointType, pointAmount, reason, createdAt);
    }

    public Long getId() {
        return this.id;
    }

    public MemberId getMemberId() {
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

    public LocalDateTime getCreatedAt() {
        return this.createdAt;
    }
}
