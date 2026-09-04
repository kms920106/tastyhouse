package com.tastyhouse.application.product.port.in;

import com.tastyhouse.application.shared.marker.CeoApp;

/**
 * 옵션그룹 병합 쓰기 인바운드 포트.
 *
 * <p>컨트롤러는 이 인터페이스만 주입하고 구현을 알지 않는다.
 */
@CeoApp
public interface ProductOptionGroupMergeCommandUseCase {

    Long mergeProductOptionGroups(ProductOptionGroupMergeCommand command);

    Long excludeMergeSuggestion(ProductOptionGroupMergeExclusionCreateCommand command);
}
