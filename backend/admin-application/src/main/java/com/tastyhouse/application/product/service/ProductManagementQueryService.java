package com.tastyhouse.application.product.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.domain.exception.ErrorCode;
import com.tastyhouse.domain.exception.ResourceNotFoundException;
import com.tastyhouse.domain.product.model.CupDepositPolicy;
import com.tastyhouse.domain.product.model.ProductOptionGroupType;
import com.tastyhouse.application.product.port.in.ProductManagementQueryUseCase;
import com.tastyhouse.application.product.port.out.ProductCategoryResult;
import com.tastyhouse.application.product.port.out.ProductDetailResult;
import com.tastyhouse.application.product.port.out.ProductListItemResult;
import com.tastyhouse.application.product.port.out.ProductManagementQueryPort;
import com.tastyhouse.application.product.port.out.ProductOptionsResult;
import com.tastyhouse.application.product.port.out.ProductSearchCondition;
import com.tastyhouse.application.shared.port.out.page.PageQuery;
import com.tastyhouse.application.shared.port.out.page.PageResult;

@Service
@Transactional(readOnly = true)
class ProductManagementQueryService implements ProductManagementQueryUseCase {

    private final ProductManagementQueryPort productManagementQueryPort;
    private final CupDepositPolicy cupDepositPolicy;

    public ProductManagementQueryService(
        ProductManagementQueryPort productManagementQueryPort,
        CupDepositPolicy cupDepositPolicy
    ) {
        this.productManagementQueryPort = productManagementQueryPort;
        this.cupDepositPolicy = cupDepositPolicy;
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
        return ProductOptionDepositAmounts.of(
            productManagementQueryPort.findProductOptions(id, ProductOptionGroupType.NORMAL.name()),
            cupDepositPolicy
        );
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
