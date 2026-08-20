package com.tastyhouse.domain.product.repository;

import java.util.List;
import java.util.Optional;

import com.tastyhouse.domain.product.model.ProductCategory;
import com.tastyhouse.domain.product.vo.ProductCategoryId;
import com.tastyhouse.domain.shop.vo.ShopId;

/**
 * 상품 카테고리 write 포트.
 *
 * <p>{@link #findCategoriesByNameAndShopId}는 BBQ 크롤링이 "같은 이름의 카테고리가 이미 있으면 재사용"
 * 하는 중복 방지 규칙에 쓰이므로(command 경로 불변식) 여기에 남는다. 화면 표시용 카테고리 목록은
 * {@code ProductQueryDao#findProductCategories}가 담당한다.
 */
public interface ProductCategoryRepository {

    Optional<ProductCategory> findById(ProductCategoryId id);

    List<ProductCategory> findCategoriesByNameAndShopId(String name, ShopId shopId);

    ProductCategory save(ProductCategory productCategory);

    /** 가게의 메뉴그룹을 {@code sort} 오름차순으로 로드한다. 재정렬 대상 집합을 만드는 데 쓴다. */
    List<ProductCategory> findAllByShopId(ShopId shopId);

    void delete(ProductCategory productCategory);
}
