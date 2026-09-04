package com.tastyhouse.application.shop.port.in;

import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.ErrorCode;

/**
 * 매장 가격 인증 대상 한 줄 command.
 *
 * <p>과거 서비스가 {@code ShopStorePriceVerificationItemRequest}(HTTP 요청 record)를 그대로 받던 자리를
 * 대체한다(챕터 02 §5).
 *
 * <p>{@code applyPickupSamePrice}의 {@code null}은 여기서 접지 않고 서비스가 기존대로
 * {@code Boolean.TRUE.equals(...)}로 판정한다 — 체크박스 미전송("동일 설정 안 함")의 의미가
 * 그대로 보존되어야 하고, 그 판단은 지금까지 서비스에 있었다.
 *
 * <p>{@code productId}·{@code priceId}가 연속한 {@code Long}이라 위치 기반 조립 시 컴파일러가
 * 막아주지 못하므로, 변환은 반드시 이름으로 짚어 넣는다.
 */
public record ShopStorePriceVerificationItemCommand(
    Long productId,
    Long priceId,
    Integer storePrice,
    Boolean applyPickupSamePrice
) {
    public ShopStorePriceVerificationItemCommand {
        if (productId == null || storePrice == null) {
            throw new BusinessException(ErrorCode.INVALID_INPUT);
        }
    }

    public static ShopStorePriceVerificationItemCommand of(
        Long productId,
        Long priceId,
        Integer storePrice,
        Boolean applyPickupSamePrice
    ) {
        return new ShopStorePriceVerificationItemCommand(
            productId,
            priceId,
            storePrice,
            applyPickupSamePrice
        );
    }
}
