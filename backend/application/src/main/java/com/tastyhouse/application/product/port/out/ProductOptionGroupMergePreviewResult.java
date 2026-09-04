package com.tastyhouse.application.product.port.out;

import java.util.List;

/**
 * 옵션그룹 합치기 미리보기 — 기준 그룹·후보들의 차이와 사전 판정 결과.
 *
 * <p><b>챕터 09</b>에서 신설. {@code mergeable}·{@code blockedReason}은 <b>사전 판정</b>으로,
 * 도메인 서비스의 검증 순서와 같은 {@code ErrorCode}를 쓰기 위해 application이 계산한다. 옵션별
 * {@code diffType}과 그룹별 {@code *Differs}도 기준 그룹과의 비교 결과이므로 표현 계약이 만들 수 없다.
 */
public record ProductOptionGroupMergePreviewResult(
    Group base,
    List<Group> candidates,
    boolean mergeable,
    String blockedReason
) {

    /** 미리보기에 등장하는 옵션그룹 한 건과 기준 그룹과의 차이. */
    public record Group(
        Long id,
        String name,
        String description,
        Boolean required,
        Boolean multipleSelect,
        Integer minSelect,
        Integer maxSelect,
        List<String> linkedProductNames,
        Boolean nameDiffers,
        Boolean minSelectDiffers,
        Boolean maxSelectDiffers,
        List<Option> options
    ) {
    }

    /** 미리보기 옵션 한 건과 합치기 후 어떻게 되는지({@code diffType}). */
    public record Option(
        Long id,
        String name,
        Integer additionalPrice,
        Boolean soldOut,
        Boolean visible,
        String diffType
    ) {
    }
}
