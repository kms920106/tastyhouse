package com.tastyhouse.ceoapi.review.adapter.in.web.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import com.tastyhouse.ceoapi.review.application.port.in.ReviewOwnerReplyCreateCommand;
import com.tastyhouse.ceoapi.review.application.port.in.ReviewOwnerReplyUpdateCommand;

/**
 * 사장님 답변 등록·수정 요청. 등록과 수정이 같은 필드 셋이라 한 record를 공용한다.
 */
@Schema(description = "사장님 답변 등록·수정 요청")
public record ReviewOwnerReplyCreateRequest(
    @NotBlank(message = "답변 내용은 필수입니다.")
    @Size(max = 1000, message = "답변 내용은 1000자를 초과할 수 없습니다.")
    @Schema(
        description = "답변 내용(최대 1000자). 금칙어가 포함되면 저장되지 않습니다.",
        example = "소중한 리뷰 감사합니다. 더 좋은 맛으로 보답하겠습니다!",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    String content
) {

    public ReviewOwnerReplyCreateCommand toCommand(Long ceoId, Long shopId, Long reviewId) {
        return new ReviewOwnerReplyCreateCommand(ceoId, shopId, reviewId, content);
    }

    public ReviewOwnerReplyUpdateCommand toUpdateCommand(Long ceoId, Long shopId, Long reviewId) {
        return new ReviewOwnerReplyUpdateCommand(ceoId, shopId, reviewId, content);
    }
}
