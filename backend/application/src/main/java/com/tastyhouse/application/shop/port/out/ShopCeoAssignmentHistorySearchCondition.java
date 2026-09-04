package com.tastyhouse.application.shop.port.out;

import java.time.LocalDate;

import com.tastyhouse.domain.shop.model.ShopCeoAssignmentActionType;

/**
 * 가게-점주 접근권한 이력 목록 조회 조건.
 *
 * <p>{@code ceoId}는 필수다 — 인가가 곧 "토큰의 점주 것만 조회한다"이므로 이 값이 빠지면 남의 접근권한
 * 이력이 새고, 동시에 이 값이 인덱스({@code ceo_id, created_at}) 레인지 스캔의 진입점이다.
 *
 * <p>{@code shopId}는 선택이며 소유권을 검증하지 않는다 — 어차피 {@code ceoId}로 함께 필터하므로 남의
 * 가게 id를 넣으면 빈 목록이 될 뿐이고, 가게 존재 여부가 새지 않는다.
 *
 * @param startDate 조회 시작일(포함)
 * @param endDate 조회 종료일(포함). DAO가 반열림 구간 {@code [startDate 00:00, endDate+1d 00:00)}으로
 *     변환한다
 */
public record ShopCeoAssignmentHistorySearchCondition(
    Long ceoId,
    Long shopId,
    ShopCeoAssignmentActionType actionType,
    LocalDate startDate,
    LocalDate endDate
) {

    public static ShopCeoAssignmentHistorySearchCondition of(
        Long ceoId,
        Long shopId,
        ShopCeoAssignmentActionType actionType,
        LocalDate startDate,
        LocalDate endDate
    ) {
        return new ShopCeoAssignmentHistorySearchCondition(
            ceoId,
            shopId,
            actionType,
            startDate,
            endDate
        );
    }
}
