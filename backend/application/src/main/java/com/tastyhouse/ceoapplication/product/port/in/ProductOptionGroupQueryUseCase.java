package com.tastyhouse.ceoapplication.product.port.in;

import java.util.List;

import com.tastyhouse.application.product.port.out.ProductOptionGroupViewResult;
import com.tastyhouse.application.product.port.out.ProductOptionGroupLinkedProductResult;
import com.tastyhouse.application.product.port.out.ProductOptionGroupLinkedProductsResult;

/**
 * 메뉴 옵션그룹 조회 인바운드 포트.
 *
 * <p>컨트롤러는 이 인터페이스만 주입하고 구현({@code ProductOptionGroupQueryService})을 알지 않는다. 도입 근거는
 * 다형성이 아니라 컴파일 게이트와 경계 계약의 문서화다(backend/CLAUDE.md 인바운드 포트 절).
 */
public interface ProductOptionGroupQueryUseCase {

    List<ProductOptionGroupViewResult> getProductOptionGroups(Long ceoId, Long shopId);

    List<ProductOptionGroupLinkedProductResult> getLinkedProducts(Long ceoId, Long shopId, Long optionGroupId);

    List<ProductOptionGroupLinkedProductsResult> getLinkedProductsByShop(Long ceoId, Long shopId);
}
