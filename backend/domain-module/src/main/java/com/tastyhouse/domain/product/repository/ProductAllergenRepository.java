package com.tastyhouse.domain.product.repository;

import java.util.List;

import com.tastyhouse.domain.product.model.ProductAllergen;
import com.tastyhouse.domain.product.vo.ProductId;

public interface ProductAllergenRepository {

    List<ProductAllergen> findAllByProductId(ProductId productId);

    List<ProductAllergen> saveAll(List<ProductAllergen> productAllergens);

    /**
     * 메뉴의 알레르기 성분을 전부 지운다. 목록이 replace-all로 교체되므로 행 단위 삭제가 아니라
     * 메뉴 단위 일괄 삭제만 필요하다.
     */
    void deleteAllByProductId(ProductId productId);
}
