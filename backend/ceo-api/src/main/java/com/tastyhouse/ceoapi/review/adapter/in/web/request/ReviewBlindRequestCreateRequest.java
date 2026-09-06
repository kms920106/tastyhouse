package com.tastyhouse.ceoapi.review.adapter.in.web.request;

import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import com.tastyhouse.application.review.port.in.ReviewBlindRequestCreateCommand;

@Schema(description = "리뷰 게시중단 요청 등록 요청")
public record ReviewBlindRequestCreateRequest(
    @NotBlank(message = "요청 사유는 필수입니다.")
    @Schema(
        description = "게시중단 요청 사유",
        allowableValues = {"ADVERTISEMENT", "PROFANITY", "IRRELEVANT", "PRIVACY", "ETC"},
        example = "PROFANITY",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    String reason,

    @Size(max = 500, message = "상세 사유는 500자를 초과할 수 없습니다.")
    @Schema(description = "상세 사유(최대 500자). 사유가 ETC면 필수입니다.", example = "특정 직원을 지목한 욕설이 포함되어 있습니다.")
    String detailReason,

    @Size(max = 3, message = "증빙 서류는 최대 3개까지 첨부할 수 있습니다.")
    @Schema(description = "증빙 서류 파일 ID 목록(선택, 최대 3개). 신분증·위임장·사업자등록증 등을 첨부합니다.")
    List<Long> attachmentFileIds
) {
    public ReviewBlindRequestCreateCommand toCommand(Long ceoId, Long shopId, Long reviewId) {
        return new ReviewBlindRequestCreateCommand(ceoId, shopId, reviewId, reason, detailReason, attachmentFileIds);
    }
}
