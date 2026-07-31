package com.tastyhouse.domain.order.domain.model;

import java.time.LocalDateTime;

import lombok.Getter;

import com.tastyhouse.domain.member.domain.vo.MemberId;
import com.tastyhouse.domain.order.domain.vo.OrderId;
import com.tastyhouse.domain.shop.domain.model.OrderMethod;
import com.tastyhouse.domain.exception.AccessDeniedException;
import com.tastyhouse.domain.exception.ErrorCode;

/**
 * 주문 순수 도메인 모델.
 *
 * <p>JPA/프레임워크에 의존하지 않는 POJO다. 영속화는 infrastructure-module의
 * {@code OrderJpaEntity} + {@code OrderMapper}가 담당한다. 도메인이 프레임워크-프리이므로
 * 변경 후 저장은 더티 체킹이 아니라 command 서비스가 명시적으로 {@code OrderRepository#save}를
 * 호출해야 한다.
 */
@Getter
public class Order {

    private final Long id; // null이면 아직 영속되지 않은 신규 상태
    private final MemberId memberId; // 주문자 회원 ID
    private final Long shopId; // 주문 대상 가게 ID
    private final String orderNumber; // 주문 번호 (unique)
    private final OrderMethod orderMethod; // 주문 방식
    private OrderStatus orderStatus; // 주문 상태
    private final String ordererName; // 주문자 이름
    private final String ordererPhone; // 주문자 전화번호
    private final String ordererEmail; // 주문자 이메일
    private Integer totalProductAmount; // 상품 금액 합계
    private Integer productDiscountAmount; // 상품 할인 금액
    private Integer couponDiscountAmount; // 쿠폰 할인 금액
    private Integer pointDiscountAmount; // 포인트 할인 금액
    private Integer totalDiscountAmount; // 총 할인 금액
    private Integer finalAmount; // 최종 결제 금액
    private Long memberCouponId; // 사용한 회원 쿠폰 ID
    private Integer usedPoint; // 사용한 포인트
    private Integer earnedPoint; // 적립된 포인트
    private boolean deleted; // 삭제 여부 (true: 삭제됨, Soft Delete)
    private final LocalDateTime createdAt; // DB 재구성 시에만 값 존재 (신규 생성 시 null)
    private final LocalDateTime updatedAt; // DB 재구성 시에만 값 존재 (신규 생성 시 null)

    private Order(
        Long id,
        MemberId memberId,
        Long shopId,
        String orderNumber,
        OrderMethod orderMethod,
        OrderStatus orderStatus,
        String ordererName,
        String ordererPhone,
        String ordererEmail,
        Integer totalProductAmount,
        Integer productDiscountAmount,
        Integer couponDiscountAmount,
        Integer pointDiscountAmount,
        Integer totalDiscountAmount,
        Integer finalAmount,
        Long memberCouponId,
        Integer usedPoint,
        Integer earnedPoint,
        boolean deleted,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
    ) {
        this.id = id;
        this.memberId = memberId;
        this.shopId = shopId;
        this.orderNumber = orderNumber;
        this.orderMethod = orderMethod;
        this.orderStatus = orderStatus;
        this.ordererName = ordererName;
        this.ordererPhone = ordererPhone;
        this.ordererEmail = ordererEmail;
        this.totalProductAmount = totalProductAmount;
        this.productDiscountAmount = productDiscountAmount;
        this.couponDiscountAmount = couponDiscountAmount;
        this.pointDiscountAmount = pointDiscountAmount;
        this.totalDiscountAmount = totalDiscountAmount;
        this.finalAmount = finalAmount;
        this.memberCouponId = memberCouponId;
        this.usedPoint = usedPoint;
        this.earnedPoint = earnedPoint;
        this.deleted = deleted;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    /**
     * 신규 주문을 생성한다. 아직 영속되지 않았으므로 식별자·감사 시각은 없다.
     */
    public static Order of(
        MemberId memberId,
        Long shopId,
        String orderNumber,
        OrderMethod orderMethod,
        OrderStatus orderStatus,
        String ordererName,
        String ordererPhone,
        String ordererEmail,
        Integer totalProductAmount,
        Integer productDiscountAmount,
        Integer couponDiscountAmount,
        Integer pointDiscountAmount,
        Integer totalDiscountAmount,
        Integer finalAmount,
        Long memberCouponId,
        Integer usedPoint,
        Integer earnedPoint
    ) {
        return new Order(
            null,
            memberId,
            shopId,
            orderNumber,
            orderMethod,
            orderStatus != null ? orderStatus : OrderStatus.PENDING,
            ordererName,
            ordererPhone,
            ordererEmail,
            totalProductAmount != null ? totalProductAmount : 0,
            productDiscountAmount != null ? productDiscountAmount : 0,
            couponDiscountAmount != null ? couponDiscountAmount : 0,
            pointDiscountAmount != null ? pointDiscountAmount : 0,
            totalDiscountAmount != null ? totalDiscountAmount : 0,
            finalAmount != null ? finalAmount : 0,
            memberCouponId,
            usedPoint != null ? usedPoint : 0,
            earnedPoint != null ? earnedPoint : 0,
            false,
            null,
            null
        );
    }

    /**
     * DB에 저장된 상태로부터 도메인 객체를 재구성한다. 영속 계층(infrastructure) 전용이며,
     * 불변식을 우회한 임의 생성을 막기 위해 이 팩토리로만 식별자·감사 시각을 주입한다.
     */
    public static Order reconstitute(
        Long id,
        MemberId memberId,
        Long shopId,
        String orderNumber,
        OrderMethod orderMethod,
        OrderStatus orderStatus,
        String ordererName,
        String ordererPhone,
        String ordererEmail,
        Integer totalProductAmount,
        Integer productDiscountAmount,
        Integer couponDiscountAmount,
        Integer pointDiscountAmount,
        Integer totalDiscountAmount,
        Integer finalAmount,
        Long memberCouponId,
        Integer usedPoint,
        Integer earnedPoint,
        boolean deleted,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
    ) {
        return new Order(
            id,
            memberId,
            shopId,
            orderNumber,
            orderMethod,
            orderStatus,
            ordererName,
            ordererPhone,
            ordererEmail,
            totalProductAmount,
            productDiscountAmount,
            couponDiscountAmount,
            pointDiscountAmount,
            totalDiscountAmount,
            finalAmount,
            memberCouponId,
            usedPoint,
            earnedPoint,
            deleted,
            createdAt,
            updatedAt
        );
    }

    public OrderId getOrderId() {
        return OrderId.of(this.id);
    }

    public void validateOwnership(MemberId memberId) {
        if (!this.memberId.equals(memberId)) {
            throw new AccessDeniedException(ErrorCode.ORDER_ACCESS_DENIED);
        }
    }

    public void confirm() {
        this.orderStatus = OrderStatus.CONFIRMED;
    }

    public void cancel() {
        this.orderStatus = OrderStatus.CANCELLED;
    }

    public void changeStatus(OrderStatus status) {
        this.orderStatus = status;
    }

    public void delete() {
        this.deleted = true;
    }

    public void updateAmounts(
        Integer totalProductAmount,
        Integer productDiscountAmount,
        Integer couponDiscountAmount,
        Integer pointDiscountAmount,
        Integer totalDiscountAmount,
        Integer finalAmount,
        Long memberCouponId,
        Integer usedPoint
    ) {
        this.totalProductAmount = totalProductAmount;
        this.productDiscountAmount = productDiscountAmount;
        this.couponDiscountAmount = couponDiscountAmount;
        this.pointDiscountAmount = pointDiscountAmount;
        this.totalDiscountAmount = totalDiscountAmount;
        this.finalAmount = finalAmount;
        this.memberCouponId = memberCouponId;
        this.usedPoint = usedPoint;
    }

    public void updateEarnedPoint(Integer earnedPoint) {
        this.earnedPoint = earnedPoint;
    }
}
