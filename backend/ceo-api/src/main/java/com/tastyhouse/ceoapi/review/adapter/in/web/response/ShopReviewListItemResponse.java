package com.tastyhouse.ceoapi.review.adapter.in.web.response;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;

import com.tastyhouse.domain.shared.model.OrderMethod;
import com.tastyhouse.domain.review.model.ReviewBlindStatus;
import com.tastyhouse.application.review.port.out.ShopReviewManagementListItemResult;
import com.tastyhouse.application.review.port.out.ShopReviewListItemViewResult;

@Schema(description = "점주 리뷰 목록 항목")
public record ShopReviewListItemResponse(
    @Schema(description = "리뷰 ID", example = "482")
    Long id,

    @Schema(description = "리뷰 고유 번호(16자리 표시용)", example = "0000000000000482")
    String reviewNumber,

    @Schema(description = "작성자 닉네임", example = "맛집탐험가")
    String memberNickname,

    @Schema(description = "종합 평점", example = "4.5")
    Double totalRating,

    @Schema(description = "리뷰 내용", example = "국물이 진하고 맛있었어요.")
    String content,

    @Schema(description = "리뷰 사진 URL 목록. 사진이 없으면 빈 배열입니다.")
    List<String> imageUrls,

    @Schema(description = "주문 메뉴명 목록. 미인증 리뷰(주문 정보 없음)면 빈 배열입니다.")
    List<String> productNames,

    @Schema(
        description = "주문유형. 미인증 리뷰면 null입니다.",
        allowableValues = {"TABLE", "RESERVATION", "DELIVERY", "TAKEOUT"},
        example = "DELIVERY"
    )
    String orderMethod,

    @Schema(description = "주문유형 한글명. 미인증 리뷰면 null입니다.", example = "배달")
    String orderMethodDescription,

    @Schema(description = "차단(게시중단) 여부", example = "false")
    Boolean hidden,

    @Schema(description = "사장님만보기 여부. 작성자가 비공개로 등록한 리뷰이며 hidden(게시중단)과는 독립이라 둘 다 true일 수 있습니다.", example = "false")
    Boolean ownerOnly,

    @Schema(description = "사장님 답변 내용. 미답변이면 null입니다.", example = "소중한 리뷰 감사합니다.")
    String ownerReplyContent,

    @Schema(description = "사장님 답변 작성일시. 미답변이면 null입니다.", example = "2026-06-20T14:03:00")
    LocalDateTime ownerReplyCreatedAt,

    @Schema(
        description = "최근 게시중단 요청 상태. 요청 이력이 없으면 null입니다.",
        allowableValues = {"PENDING", "APPROVED", "REJECTED", "CANCELED"},
        example = "PENDING"
    )
    String blindRequestStatus,

    @Schema(description = "최근 게시중단 요청 상태 한글명. 요청 이력이 없으면 null입니다.", example = "대기")
    String blindRequestStatusDescription,

    @Schema(description = "리뷰 작성일시", example = "2026-06-19T20:11:00")
    LocalDateTime createdAt,

    @Schema(description = "답변 마감일 = 리뷰 작성일 + 30일. 이 날짜까지는 하루 종일 등록할 수 있습니다.", example = "2026-07-19")
    LocalDate replyDeadline,

    @Schema(description = "오늘 기준 신규 답변 등록 가능 여부. 이미 답변이 있으면 이 값과 무관하게 수정·삭제할 수 있습니다.", example = "true")
    boolean replyable
) {

    /** 표시용 리뷰 번호 자릿수(0-pad). 원문 ②의 16자리 규격. */
    private static final int REVIEW_NUMBER_LENGTH = 16;

    public static ShopReviewListItemResponse from(ShopReviewListItemViewResult view) {
        ShopReviewManagementListItemResult result = view.review();
        OrderMethod orderMethod = result.orderMethod();
        ReviewBlindStatus blindRequestStatus = result.blindRequestStatus();
        return new ShopReviewListItemResponse(
            result.id(),
            toReviewNumber(result.id()),
            result.memberNickname(),
            result.totalRating(),
            result.content(),
            result.imageUrls(),
            result.productNames(),
            orderMethod == null ? null : orderMethod.name(),
            orderMethod == null ? null : orderMethod.getDisplayName(),
            result.hidden(),
            result.ownerOnly(),
            result.ownerReplyContent(),
            result.ownerReplyCreatedAt(),
            blindRequestStatus == null ? null : blindRequestStatus.name(),
            blindRequestStatus == null ? null : blindRequestStatus.getDescription(),
            result.createdAt(),
            view.replyWindow().replyDeadline(),
            view.replyWindow().replyable()
        );
    }

    /**
     * 리뷰 ID를 {@value #REVIEW_NUMBER_LENGTH}자리 0-pad 표시용 번호로 만든다(원문 ②).
     *
     * <p>챕터 09에서 QueryService의 private 헬퍼를 이 표현 계약으로 옮겼다 — 0-pad 자릿수는 화면 표기
     * 규칙이지 도메인 불변식이 아니다.
     */
    static String toReviewNumber(Long reviewId) {
        return String.format("%0" + REVIEW_NUMBER_LENGTH + "d", reviewId);
    }
}
