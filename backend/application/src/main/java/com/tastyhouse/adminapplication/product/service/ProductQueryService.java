package com.tastyhouse.adminapplication.product.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.domain.exception.ErrorCode;
import com.tastyhouse.domain.exception.ResourceNotFoundException;
import com.tastyhouse.domain.shared.page.PageQuery;
import com.tastyhouse.domain.shared.page.PageResult;
import com.tastyhouse.application.product.port.out.ProductCategoryResult;
import com.tastyhouse.application.product.port.out.ProductDetailResult;
import com.tastyhouse.application.product.port.out.ProductListItemResult;
import com.tastyhouse.application.product.port.out.ProductOptionsResult;
import com.tastyhouse.application.product.port.out.ProductManagementQueryPort;
import com.tastyhouse.application.product.port.out.ProductSearchCondition;
import com.tastyhouse.adminapplication.product.port.in.ProductQueryUseCase;

/**
 * 관리자 상품 조회 서비스. infrastructure의 read 어댑터 {@link ProductManagementQueryPort}만 주입한다.
 * 생성·수정은 {@link ProductCommandService}가 담당한다.
 *
 * <p><b>챕터 06</b> — 읽기 포트의 {@code *Result}를 그대로 반환하고 Response로 변환하지 않는다.
 * 표현 계약(@Schema 붙은 Response·PaginationResponse) 조립은 컨트롤러의 책임이다.
 */
@Service
@Transactional(readOnly = true)
public class ProductQueryService implements ProductQueryUseCase {

    private final ProductManagementQueryPort productManagementQueryPort;

    public ProductQueryService(ProductManagementQueryPort productManagementQueryPort) {
        this.productManagementQueryPort = productManagementQueryPort;
    }

    @Override
    public PageResult<ProductListItemResult> getProducts(
        Long shopId,
        Long productCategoryId,
        String name,
        Boolean visible,
        Boolean soldOut,
        int page,
        int size
    ) {
        ProductSearchCondition condition = ProductSearchCondition.of(shopId, productCategoryId, name, visible, soldOut);
        PageQuery pageQuery = PageQuery.of(page, size);
        return productManagementQueryPort.findProducts(condition, pageQuery);
    }

    @Override
    public ProductDetailResult getProduct(Long id) {
        return productManagementQueryPort.findProductDetailById(id)
            .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.PRODUCT_NOT_FOUND));
    }

    @Override
    public ProductOptionsResult getProductOptions(Long id) {
        return productManagementQueryPort.findProductOptions(id);
    }

    @Override
    public List<String> getProductImages(Long id) {
        return productManagementQueryPort.findProductImageUrls(id);
    }

    @Override
    public List<ProductCategoryResult> getProductCategories(Long shopId) {
        return productManagementQueryPort.findProductCategories(shopId);
    }
}
