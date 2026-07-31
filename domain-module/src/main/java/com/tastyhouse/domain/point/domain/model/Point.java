package com.tastyhouse.domain.point.domain.model;

import lombok.Getter;

import com.tastyhouse.domain.member.domain.vo.MemberId;
import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.ErrorCode;

/**
 * 회원 포인트 순수 도메인 모델.
 *
 * <p>JPA/프레임워크에 의존하지 않는 POJO다. 영속화는 infrastructure-module의
 * {@code PointJpaEntity} + {@code PointMapper}가 담당한다. 도메인이
 * 프레임워크-프리이므로 변경 후 저장은 더티 체킹이 아니라 command 서비스가 명시적으로
 * {@code PointRepository#save}를 호출해야 한다.
 */
@Getter
public class Point {

    private final Long id; // null이면 아직 영속되지 않은 신규 상태
    private final MemberId memberId;
    private Integer availablePoints; // 사용 가능 포인트
    private final Integer expiredThisMonth; // 이번 달 소멸 예정 포인트

    private Point(Long id, MemberId memberId, Integer availablePoints, Integer expiredThisMonth) {
        this.id = id;
        this.memberId = memberId;
        this.availablePoints = availablePoints != null ? availablePoints : 0;
        this.expiredThisMonth = expiredThisMonth != null ? expiredThisMonth : 0;
    }

    /**
     * 신규 회원 포인트를 생성한다. 아직 영속되지 않았으므로 식별자는 없다.
     */
    public static Point of(MemberId memberId) {
        return new Point(null, memberId, 0, 0);
    }

    /**
     * DB에 저장된 상태로부터 도메인 객체를 재구성한다. 영속 계층(infrastructure) 전용이며,
     * 불변식을 우회한 임의 생성을 막기 위해 이 팩토리로만 식별자를 주입한다.
     */
    public static Point reconstitute(Long id, MemberId memberId, Integer availablePoints, Integer expiredThisMonth) {
        return new Point(id, memberId, availablePoints, expiredThisMonth);
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
