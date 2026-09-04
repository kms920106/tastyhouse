package com.tastyhouse.application.order.port.in;

import java.time.LocalDateTime;
import java.util.List;

import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.ErrorCode;

/**
 * 주문 생성 command.
 *
 * <p>과거 {@code OrderCommandService#createOrder}의 파라미터 15개 시그니처를 대체한다. 그중 금액이
 * {@code Integer} 8개 연속이라 이 리포에서 위치 기반 인자 뒤바뀜 위험이 가장 큰 지점이었다(챕터 02 §5).
 *
 * <p><b>필드 선언 순서는 {@code OrderPlacement}(도메인 입력 record)의 컴포넌트 순서와 일치시켰다.</b>
 * 서비스가 이 command를 {@code OrderPlacement.of(...)}로 위치 기반 전달하므로 두 순서가 어긋나면
 * 컴파일은 통과하고 금액만 조용히 뒤바뀐다. 한쪽을 고치면 반드시 다른 쪽도 함께 고친다.
 *
 * <p>주의: HTTP 요청 record {@code OrderCreateRequest}는 {@code deliveryAddressId}를
 * {@code usePoint}보다 <b>먼저</b> 선언한다(Swagger 문서상의 배치). 이 command와 순서가 다르므로,
 * {@code toCommand}는 반드시 이름 기반 접근자로 각 값을 짚어 넘긴다.
 */
public record OrderCreateCommand(
    Long memberId,
    Long shopId,
    String orderMethod,
    List<OrderLineCommand> orderLines,
    Long memberCouponId,
    Integer usePoint,
    Long deliveryAddressId,
    Integer totalProductAmount,
    Integer totalDiscountAmount,
    Integer productDiscountAmount,
    Integer couponDiscountAmount,
    Integer deliveryTipAmount,
    Integer cupDepositAmount,
    Integer finalAmount,
    LocalDateTime scheduledAt
) {
    public OrderCreateCommand {
        if (memberId == null || shopId == null || orderMethod == null || orderLines == null) {
            throw new BusinessException(ErrorCode.INVALID_INPUT);
        }
    }
}
