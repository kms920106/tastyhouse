package com.tastyhouse.webapi.review.application.port.in;

import java.util.List;

import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.ErrorCode;

/**
 * 리뷰 등록 command.
 *
 * <p>평점 범위·내용 길이·이미지 장수 검증은 Request의 jakarta.validation이 담당하고, 이 record는
 * 필수값 누락 같은 구조적 가드만 둔다.
 *
 * <p><b>{@code tasteRating}·{@code amountRating}·{@code priceRating} 세 {@code Integer}가 연달아
 * 있다.</b> 위치 기반으로 옮기면 컴파일은 통과하고 평점만 조용히 뒤바뀌므로, {@code toCommand}는 반드시
 * 이름 기반 접근자로 각 값을 짚어 넘긴다. 필드 순서는 서비스가 도메인 서비스로 위치 기반 전달하는
 * {@code ReviewLifecycleService#register}의 인자 순서(평점 3종 → content → 파일 → 태그 → ownerOnly →
 * 배달 평가)와 맞췄다.
 *
 * <p>{@code orderProductId}는 주문 기반 리뷰에만 있으므로 null을 허용하고, {@code ownerOnly}는
 * 하위호환을 위해 null을 허용한다(서비스가 {@code Boolean.TRUE.equals}로 정규화).
 */
public record ReviewCreateCommand(
    Long memberId,
    Long orderProductId,
    Long productId,
    Integer tasteRating,
    Integer amountRating,
    Integer priceRating,
    String content,
    List<Long> uploadedFileIds,
    List<String> tags,
    Boolean ownerOnly,
    Integer deliveryRating,
    String deliveryComment
) {
    public ReviewCreateCommand {
        if (memberId == null || productId == null || tasteRating == null
            || amountRating == null || priceRating == null || content == null) {
            throw new BusinessException(ErrorCode.INVALID_INPUT);
        }
    }
}
