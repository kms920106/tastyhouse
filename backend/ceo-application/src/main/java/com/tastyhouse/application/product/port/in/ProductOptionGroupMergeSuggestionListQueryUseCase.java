package com.tastyhouse.application.product.port.in;

import java.util.List;

import com.tastyhouse.application.product.port.out.ProductOptionGroupMergeSuggestionResult;

public interface ProductOptionGroupMergeSuggestionListQueryUseCase {

    List<ProductOptionGroupMergeSuggestionResult> getMergeSuggestions(Long ceoId, Long shopId);
}
