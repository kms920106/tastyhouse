package com.tastyhouse.core.domain.shop.domain.model;

import java.time.LocalDateTime;

import lombok.Getter;

import com.tastyhouse.core.exception.BusinessException;
import com.tastyhouse.core.exception.ErrorCode;

/**
 * 상점 영업 임시중지 순수 도메인 모델.
 *
 * <p>JPA/프레임워크에 의존하지 않는 POJO다. 영속화는 infrastructure-module의
 * {@code ShopSuspensionJpaEntity} + {@code ShopSuspensionMapper}가 담당한다. 상태전이는 즉시 해제(release)만
 * 존재한다.
 */
@Getter
public class ShopSuspension {

    private final Long id; // null이면 아직 영속되지 않은 신규 상태
    private final Long shopId;
    private final SuspensionReason reason;
    private final OrderMethod orderMethod; // null이면 전체 주문유형 대상
    private final LocalDateTime startAt;
    private final LocalDateTime endAt;
    private LocalDateTime releasedAt; // 해제 시각 (release로 재대입됨)
    private final LocalDateTime createdAt;
    private final LocalDateTime updatedAt;

    private ShopSuspension(
        Long id,
        Long shopId,
        SuspensionReason reason,
        OrderMethod orderMethod,
        LocalDateTime startAt,
        LocalDateTime endAt,
        LocalDateTime releasedAt,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
    ) {
        this.id = id;
        this.shopId = shopId;
        this.reason = reason;
        this.orderMethod = orderMethod;
        this.startAt = startAt;
        this.endAt = endAt;
        this.releasedAt = releasedAt;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    /**
     * 신규 영업 임시중지를 생성한다. 아직 영속되지 않았으므로 식별자·감사 시각은 없고, 해제 시각도 없다.
     */
    public static ShopSuspension of(
        Long shopId,
        SuspensionReason reason,
        OrderMethod orderMethod,
        LocalDateTime startAt,
        LocalDateTime endAt
    ) {
        if (endAt.isBefore(startAt)) {
            throw new BusinessException(ErrorCode.SHOP_SUSPENSION_INVALID_PERIOD);
        }

        return new ShopSuspension(null, shopId, reason, orderMethod, startAt, endAt, null, null, null);
    }

    /**
     * DB에 저장된 상태로부터 도메인 객체를 재구성한다. 영속 계층(infrastructure) 전용이다.
     */
    public static ShopSuspension reconstitute(
        Long id,
        Long shopId,
        SuspensionReason reason,
        OrderMethod orderMethod,
        LocalDateTime startAt,
        LocalDateTime endAt,
        LocalDateTime releasedAt,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
    ) {
        return new ShopSuspension(id, shopId, reason, orderMethod, startAt, endAt, releasedAt, createdAt, updatedAt);
    }

    /**
     * 임시중지를 즉시 해제한다.
     */
    public void release(LocalDateTime releasedAt) {
        this.releasedAt = releasedAt;
    }

    /**
     * 주어진 시각 기준으로 이 임시중지가 활성 상태인지 판단한다(해제되지 않았고, 기간 내인 경우).
     */
    public boolean isActive(LocalDateTime now) {
        return releasedAt == null && !now.isBefore(startAt) && now.isBefore(endAt);
    }
}
