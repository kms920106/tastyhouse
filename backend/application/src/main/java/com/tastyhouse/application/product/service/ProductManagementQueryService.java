package com.tastyhouse.application.product.service;

import com.tastyhouse.application.shared.marker.AdminApp;
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
import com.tastyhouse.application.product.port.in.ProductManagementQueryUseCase;

@Service
@AdminApp
@Transactional(readOnly = true)
public class ProductManagementQueryService implements ProductManagementQueryUseCase {

    private final ProductManagementQueryPort productManagementQueryPort;

    public ProductManagementQueryService(ProductManagementQueryPort productManagementQueryPort) {
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
