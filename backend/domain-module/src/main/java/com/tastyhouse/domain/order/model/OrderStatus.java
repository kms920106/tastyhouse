package com.tastyhouse.domain.order.model;

import java.util.Set;

import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.ErrorCode;

/**
 * 주문 상태와 허용 전이 테이블.
 *
 * <p>허용 전이는 현재 호출부 전수 조사로 확정했다.
 * <ul>
 *   <li>{@code PENDING -> CONFIRMED} — 결제 승인({@code PaymentConfirmationService}의 PG 콜백 확정·토스 승인·
 *       현장결제 완료 3경로). 세 경로 모두 결제 개시({@code open})가 주문이 {@code PENDING}인지 검증한 뒤에야
 *       도달한다.</li>
 *   <li>{@code PENDING -> CANCELLED}, {@code CONFIRMED -> CANCELLED} — 결제 취소
 *       ({@code PaymentCancellationService#cancel}). 그 서비스의 {@code resolveCancelCode}가
 *       {@code PENDING}·{@code CONFIRMED}만 취소 가능으로 판정하므로 취소 전이의 출발 상태도 이 둘뿐이다.</li>
 *   <li>{@code CONFIRMED -> PREPARING}, {@code PREPARING -> COMPLETED} — 관리자 수동 상태 변경
 *       (admin-api {@code PATCH /api/orders/v1/{id}/status})만이 도달시키는 조리 파이프라인. 결제 취소 판정이
 *       {@code PREPARING}(조리 시작)·{@code COMPLETED}(주문 완료)를 취소 불가로 보는 것과 정합한다.</li>
 *   <li>{@code COMPLETED}·{@code CANCELLED} — 종결 상태로 어떤 전이도 허용하지 않는다.</li>
 * </ul>
 */
public enum OrderStatus {
    PENDING,        // 주문 대기
    CONFIRMED,      // 주문 확인
    PREPARING,      // 준비 중
    COMPLETED,      // 완료
    CANCELLED;      // 취소

    public static OrderStatus from(String code) {
        try {
            return valueOf(code);
        } catch (IllegalArgumentException e) {
            throw new BusinessException(ErrorCode.ORDER_STATUS_UNKNOWN,
                ErrorCode.ORDER_STATUS_UNKNOWN.getDefaultMessage() + ": " + code);
        }
    }

    /**
     * 이 상태에서 {@code target}으로 전이할 수 있는지 판정한다.
     *
     * <p>같은 상태로의 재전이(멱등 호출)도 허용하지 않는다 — 종결 상태 재진입과 구분되지 않아 "이미 처리된
     * 주문"을 조용히 통과시키기 때문이다.
     */
    public boolean canTransitionTo(OrderStatus target) {
        return allowedTargets().contains(target);
    }

    private Set<OrderStatus> allowedTargets() {
        return switch (this) {
            case PENDING -> Set.of(CONFIRMED, CANCELLED);
            case CONFIRMED -> Set.of(PREPARING, CANCELLED);
            case PREPARING -> Set.of(COMPLETED);
            case COMPLETED, CANCELLED -> Set.of();
        };
    }
}
