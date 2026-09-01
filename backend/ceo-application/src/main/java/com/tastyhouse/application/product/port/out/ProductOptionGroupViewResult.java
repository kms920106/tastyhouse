package com.tastyhouse.application.product.port.out;

import java.util.List;

/**
 * 옵션그룹 하나와 그 옵션들 — 보증금액까지 계산해 넣은 형태.
 *
 * <p><b>챕터 09</b>에서 신설. 옵션의 {@code depositAmount}는 저장값이 아니라 조회 시점에
 * <b>도메인 서비스 {@code CupDepositPolicy}가 요율로 계산</b>하는 값이다(옵션 행에는 컵 개수만 남기기로
 * 한 결정의 표시 측 대응). 도메인 서비스 호출은 application의 일이므로 표현 계약이 대신할 수 없어,
 * 계산을 마친 값을 이 결과에 담아 넘긴다.
 */
public record ProductOptionGroupViewResult(
    Long id,
    String name,
    String description,
    Boolean required,
    Boolean multipleSelect,
    Integer minSelect,
    Integer maxSelect,
    Integer sort,
    Boolean visible,
    String groupType,
    Long linkedProductCount,
    List<Option> options
) {

    /** 옵션 한 건. {@code depositAmount}는 조회 시점 계산값이다. */
    public record Option(
        Long id,
        String name,
        Integer additionalPrice,
        Integer sort,
        Boolean visible,
        Integer cupCount,
        Integer depositAmount,
        Integer personalCupDiscountAmount
    ) {
    }
}
