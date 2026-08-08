package com.tastyhouse.domain.shop.model;

import java.time.LocalDateTime;

import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.ErrorCode;
import com.tastyhouse.domain.shop.vo.ShopId;

/**
 * 상점 영업 임시중지 순수 도메인 모델.
 *
 * <p>JPA/프레임워크에 의존하지 않는 POJO다. 영속화는 infrastructure-module의
 * {@code ShopSuspensionJpaEntity} + {@code ShopSuspensionMapper}가 담당한다. 상태전이는 즉시 해제(release)만
 * 존재한다.
 */
public class ShopSuspension {

    private final Long id; // null이면 아직 영속되지 않은 신규 상태
    private final ShopId shopId;
    private final SuspensionReason reason;
    private final OrderMethod orderMethod; // null이면 전체 주문유형 대상
    private final LocalDateTime startAt;
    private final LocalDateTime endAt;
    private LocalDateTime releasedAt; // 해제 시각 (release로 재대입됨)
    private final LocalDateTime createdAt;
    private final LocalDateTime updatedAt;

    private ShopSuspension(
        Long id,
        ShopId shopId,
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
        ShopId shopId,
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
        ShopId shopId,
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
     *
     * <p>주문유형을 보지 않으므로 <b>유형과 무관한 표시 경로</b>(점주 화면의 임시중지 목록 등)에서 쓴다.
     * 주문가능 여부 판정에는 {@link #isActive(LocalDateTime, OrderMethod)}를 쓴다.
     */
    public boolean isActive(LocalDateTime now) {
        return releasedAt == null && !now.isBefore(startAt) && now.isBefore(endAt);
    }

    /**
     * 이 임시중지가 주어진 주문유형에 적용되는지 판단한다.
     *
     * <p>{@code orderMethod}가 null이면 전체 주문유형이 대상이므로 항상 true다. {@code target}이 null이면
     * "가게 전체" 판정이므로 전체 대상 중지에만 걸린다 — 유형별 중지({@code orderMethod != null})는
     * {@code target == null}과 절대 같지 않아 가게 전체 상태를 멈추지 않는다. "배달만 멈추고 포장은 계속
     * 받는다"는 도메인 규칙이 이 한 줄로 성립한다.
     */
    public boolean appliesTo(OrderMethod target) {
        if (this.orderMethod == null) {
            return true;
        }
        return this.orderMethod == target;
    }

    /**
     * 주어진 시각·주문유형 기준으로 이 임시중지가 활성인지 판단한다.
     * {@code target}이 null이면 가게 전체 판정이며, 유형별 중지는 가게 전체를 멈추지 않는다.
     */
    public boolean isActive(LocalDateTime now, OrderMethod target) {
        return isActive(now) && appliesTo(target);
    }

    public Long getId() {
        return this.id;
    }

    public ShopId getShopId() {
        return this.shopId;
    }

    public SuspensionReason getReason() {
        return this.reason;
    }

    public OrderMethod getOrderMethod() {
        return this.orderMethod;
    }

    public LocalDateTime getStartAt() {
        return this.startAt;
    }

    public LocalDateTime getEndAt() {
        return this.endAt;
    }

    public LocalDateTime getReleasedAt() {
        return this.releasedAt;
    }

    public LocalDateTime getCreatedAt() {
        return this.createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return this.updatedAt;
    }
}
