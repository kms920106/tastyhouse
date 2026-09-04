package com.tastyhouse.application.product.port.in;

import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.ErrorCode;

/**
 * 메뉴 정보 의견 제보 command.
 *
 * <p>형식·길이 검증은 Request의 jakarta.validation이 담당하고(400 계약·한국어 메시지 유지),
 * 이 record는 필수값 누락 같은 구조적 가드만 둔다. "{@code ETC}면 내용 필수"는 도메인 불변식이라
 * 여기서도 표현하지 않는다.
 *
 * <p><b>가게는 필드에 없다</b> — 클라이언트가 보낸 가게를 믿으면 남의 가게에 제보를 꽂을 수 있어
 * 도메인이 메뉴에서 끌어온다.
 */
public record ProductFeedbackCreateCommand(
    Long memberId,
    Long productId,
    String feedbackType,
    String content
) {
    public ProductFeedbackCreateCommand {
        if (memberId == null || productId == null || feedbackType == null) {
            throw new BusinessException(ErrorCode.INVALID_INPUT);
        }
    }
}
