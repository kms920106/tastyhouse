package com.tastyhouse.adminapplication.product.port.in;

import java.util.List;

import com.tastyhouse.adminapplication.product.response.ProductCategoryResponse;
import com.tastyhouse.adminapplication.product.response.ProductDetailResponse;
import com.tastyhouse.adminapplication.product.response.ProductImagesResponse;
import com.tastyhouse.adminapplication.product.response.ProductListItemResponse;
import com.tastyhouse.adminapplication.product.response.ProductOptionGroupsResponse;
import com.tastyhouse.apicommon.common.PaginationResponse;

/**
 * 메뉴 조회 인바운드 포트.
 *
 * <p>컨트롤러는 이 인터페이스만 주입하고 구현({@code ProductQueryService})을 알지 않는다. 도입 근거는
 * 다형성이 아니라 컴파일 게이트와 경계 계약의 문서화다(backend/CLAUDE.md 인바운드 포트 절).
 */
public interface ProductQueryUseCase {

    PaginationResponse<ProductListItemResponse> getProducts(
        Long shopId,
        Long productCategoryId,
        String name,
        Boolean visible,
        Boolean soldOut,
        int page,
        int size
    );

    ProductDetailResponse getProduct(Long id);

    ProductOptionGroupsResponse getProductOptions(Long id);

    ProductImagesResponse getProductImages(Long id);

    List<ProductCategoryResponse> getProductCategories(Long shopId);
}
