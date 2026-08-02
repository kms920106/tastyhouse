package com.tastyhouse.infrastructure.product.query;

import java.util.List;

/**
 * 상품의 옵션 그룹 목록 read model. 개별 옵션 그룹과 공통 옵션 그룹을 단일 목록으로 병합한 결과다.
 */
public record ProductOptionsResult(List<OptionGroupResult> optionGroups) {
}
