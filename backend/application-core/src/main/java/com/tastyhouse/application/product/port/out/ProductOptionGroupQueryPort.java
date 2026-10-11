package com.tastyhouse.application.product.port.out;

import java.util.List;
import java.util.Map;
import java.util.Set;

public interface ProductOptionGroupQueryPort {

    List<ProductOptionGroupManagementResult> findProductOptionGroupsForManagement(Long shopId);

    List<ProductOptionGroupLinkedProductResult> findLinkedProductsByOptionGroupId(Long optionGroupId);

    Map<Long, List<ProductOptionGroupLinkedProductResult>> findLinkedProductsByShop(Long shopId);

    List<ProductOptionGroupMergeCandidateResult> findOptionGroupMergeCandidates(Long shopId);

    Set<String> findOptionGroupMergeExcludedSignatures(Long shopId);
}
