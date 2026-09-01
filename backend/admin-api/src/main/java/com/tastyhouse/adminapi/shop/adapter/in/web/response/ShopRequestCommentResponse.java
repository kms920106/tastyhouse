package com.tastyhouse.adminapi.shop.adapter.in.web.response;

import java.time.LocalDateTime;

import io.swagger.v3.oas.annotations.media.Schema;

import com.tastyhouse.application.shop.port.out.ShopRequestCommentResult;

/**
 * 요청건 문의 스레드 항목 응답.
 *
 * <p>ceo-api의 동명 record와 필드가 같지만 통합하지 않는다 — 각 모듈이 자기 응답 계약을 소유하는 이 저장소의
 * 관례이고, 담당자 화면에서 작성자 표기가 갈릴 여지가 있다. 작성자 실명·식별자는 양쪽 모두 노출하지 않는다.
 */
@Schema(description = "요청건 문의 스레드 항목")
public record ShopRequestCommentResponse(

    @Schema(description = "댓글 ID", example = "88")
    Long commentId,

    @Schema(description = "작성자 유형 코드", example = "CEO", allowableValues = {"CEO", "ADMIN"})
    String authorType,

    @Schema(description = "작성자 유형 한글 라벨", example = "점주")
    String authorTypeDescription,

    @Schema(description = "내용", example = "반려 사유를 좀 더 자세히 알려주실 수 있나요?")
    String content,

    @Schema(description = "작성 일시", example = "2026-08-12T09:20:11")
    LocalDateTime createdAt
) {

    public static ShopRequestCommentResponse from(ShopRequestCommentResult result) {
        return new ShopRequestCommentResponse(
            result.commentId(),
            result.authorType().name(),
            result.authorType().getDescription(),
            result.content(),
            result.createdAt()
        );
    }
}
