package com.tastyhouse.application.product.port.out;

import java.util.List;

/**
 * 옵션그룹 합치기 추천 묶음 하나 — 동일성 서명이 같은 그룹들과 대표 옵션 집합.
 *
 * <p><b>챕터 09</b>에서 신설. 이 묶음은 표현 계약이 만들 수 없다 — 서명(SHA-256)을 <b>여기서 계산</b>하고
 * ({@code ProductOptionGroupSignature}), 점주가 제외한 서명을 걸러내며, 세 개의 조회 결과
 * (후보·연결 메뉴·관리 목록)를 payload 기준으로 그룹핑해 합친 결과다.
 */
public record ProductOptionGroupMergeSuggestionResult(
    String signature,
    String name,
    Integer minSelect,
    Integer maxSelect,
    Integer groupCount,
    Integer linkedProductCount,
    List<Option> options,
    List<Group> groups
) {

    /** 묶음의 대표 옵션 한 건(묶음 안의 그룹은 정의상 옵션 집합이 같다). */
    public record Option(
        Long id,
        String name,
        Integer additionalPrice
    ) {
    }

    /** 묶음에 속한 옵션그룹 한 건과 그 연결 메뉴. */
    public record Group(
        Long id,
        Integer linkedProductCount,
        List<String> linkedProductNames
    ) {
    }
}
