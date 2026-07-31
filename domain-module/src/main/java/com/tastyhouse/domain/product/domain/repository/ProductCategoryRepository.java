package com.tastyhouse.domain.product.domain.repository;

import java.util.List;
import java.util.Optional;

import com.tastyhouse.domain.product.domain.model.ProductCategory;
import com.tastyhouse.domain.product.domain.vo.ProductCategoryId;

/**
 * 상품 카테고리 write 포트.
 *
 * <p>{@link #findCategoriesByNameAndShopId}는 BBQ 크롤링이 "같은 이름의 카테고리가 이미 있으면 재사용"
 * 하는 중복 방지 규칙에 쓰이므로(command 경로 불변식) 여기에 남는다. 화면 표시용 카테고리 목록은
 * {@code ProductQueryDao#findProductCategories}가 담당한다.
 */
public interface ProductCategoryRepository {

    Optional<ProductCategory> findById(ProductCategoryId id);

    List<ProductCategory> findCategoriesByNameAndShopId(String name, Long shopId);

    ProductCategory save(ProductCategory productCategory);
}
