package com.tastyhouse.webapi.menureview.application.port.in;

import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.ErrorCode;

/**
 * 메뉴 평가 등록 command.
 *
 * <p>평점 범위(1~5)·코멘트 길이 검증은 Request의 jakarta.validation이 담당하고, 이 record는 필수값
 * 누락 같은 구조적 가드만 둔다.
 *
 * <p><b>{@code shopId}·{@code productId}·{@code orderId}는 필드에 없다</b> — 클라이언트가 보낸 값을
 * 믿으면 다른 상품에 평점을 붙일 수 있어 서비스가 주문 항목에서 읽어 채운다.
 */
public record MenuReviewCreateCommand(
    Long memberId,
    Long orderProductId,
    Integer rating,
    String comment
) {
    public MenuReviewCreateCommand {
        if (memberId == null || orderProductId == null || rating == null) {
            throw new BusinessException(ErrorCode.INVALID_INPUT);
        }
    }
}
