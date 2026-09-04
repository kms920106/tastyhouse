package com.tastyhouse.ceoapi.review.adapter.in.web.request;

import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import com.tastyhouse.application.review.port.in.ReviewBlindRequestCreateCommand;

/**
 * 리뷰 게시중단 요청 등록 요청.
 *
 * <p>{@code reason=ETC}일 때 {@code detailReason} 필수라는 규칙은 Bean Validation이 아니라 도메인 서비스가
 * 판정한다 — 두 필드에 걸친 조건부 규칙이라 어노테이션으로 표현할 수 없고, 사유별 필수 여부는 도메인
 * 규칙이기 때문이다({@code REVIEW_BLIND_DETAIL_REASON_REQUIRED}).
 *
 * <p>반면 <b>첨부 개수 상한(3개)은 Bean Validation이 판정한다</b> — 한 필드 안에서 닫히는 규칙이고,
 * 개수는 스키마가 아니라 정책이라 별도 테이블을 두는 대신 여기서 막는다.
 */
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
