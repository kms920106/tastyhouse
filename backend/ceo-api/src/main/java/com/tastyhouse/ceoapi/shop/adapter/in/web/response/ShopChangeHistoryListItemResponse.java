package com.tastyhouse.ceoapi.shop.adapter.in.web.response;

import java.time.LocalDateTime;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * 가게 변경이력 목록 항목 응답.
 *
 * <p>코드와 한글 라벨을 함께 내려준다 — 코드는 프론트 분기용, 라벨은 표시용이다. 라벨을 서버가 내려주면
 * 프론트에 29개 중분류 라벨 상수를 복제하지 않아, 표기 변경이 서버 배포만으로 반영된다.
 *
 * <p>{@code actorType}/{@code actorId}는 노출하지 않는다 — 점주는 자기 가게 이력만 보므로 행위자 정보가
 * 필요 없고, {@code actorId}는 내부 식별자다.
 */
@Schema(description = "가게 변경이력 목록 항목")
public record ShopChangeHistoryListItemResponse(

    @Schema(description = "이력 ID", example = "1024")
    Long id,

    @Schema(description = "변경 대분류 코드", example = "DELIVERY")
    String category,

    @Schema(description = "변경 대분류 한글 라벨", example = "배달 정보")
    String categoryName,

    @Schema(description = "변경 중분류 코드", example = "DELIVERY_TIP_SCHEDULE")
    String changeType,

    @Schema(description = "변경 중분류 한글 라벨", example = "시간 할증 배달팁")
    String changeTypeName,

    @Schema(description = "조치 유형 코드", example = "UPDATE", allowableValues = {"CREATE", "UPDATE", "DELETE"})
    String actionType,

    @Schema(description = "조치 유형 한글 라벨", example = "수정")
    String actionTypeName,

    @Schema(description = "변경 전 요약. 등록 시 null", example = "18:00~20:00: +1,000원")
    String previousValue,

    @Schema(description = "변경 후 요약. 삭제 시 null", example = "18:00~20:00: +1,500원")
    String newValue,

    @Schema(description = "변경 일시", example = "2026-08-11T19:46:03")
    LocalDateTime changedAt
) {

    public static ShopChangeHistoryListItemResponse from(
        Long id,
        String category,
        String categoryName,
        String changeType,
        String changeTypeName,
        String actionType,
        String actionTypeName,
        String previousValue,
        String newValue,
        LocalDateTime changedAt
    ) {
        return new ShopChangeHistoryListItemResponse(
            id,
            category,
            categoryName,
            changeType,
            changeTypeName,
            actionType,
            actionTypeName,
            previousValue,
            newValue,
            changedAt
        );
    }
}
