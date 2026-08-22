package com.tastyhouse.infrastructure.product.query;

import java.util.List;

/**
 * 상품 옵션 그룹 read model. {@code common}이 true면 여러 상품이 공유하는 공통 옵션 그룹이다.
 *
 * <p>{@code groupType}은 {@code common}과 <b>다른 축</b>이다 — {@code common}은 "이 그룹을 여러 메뉴가
 * 공유하는가"이고, {@code groupType}은 "이 그룹의 금액이 어떤 성격인가"({@code NORMAL} 추가금 /
 * {@code CUP_DEPOSIT} 보증금)다. 공통 옵션그룹은 구조상 보증금이 될 수 없어 항상 {@code NORMAL}이다.
 */
public record OptionGroupResult(
    Long id,
    String name,
    String description,
    boolean required,
    boolean multipleSelect,
    Integer minSelect,
    Integer maxSelect,
    boolean common,
    String groupType,
    List<OptionResult> options
) {
}
