package com.tastyhouse.adminapplication.product.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.domain.exception.ErrorCode;
import com.tastyhouse.domain.exception.ResourceNotFoundException;
import com.tastyhouse.domain.shared.page.PageQuery;
import com.tastyhouse.domain.shared.page.PageResult;
import com.tastyhouse.application.product.port.out.OptionGroupResult;
import com.tastyhouse.application.product.port.out.OptionResult;
import com.tastyhouse.application.product.port.out.ProductCategoryResult;
import com.tastyhouse.application.product.port.out.ProductDetailResult;
import com.tastyhouse.application.product.port.out.ProductListItemResult;
import com.tastyhouse.application.product.port.out.ProductOptionsResult;
import com.tastyhouse.application.product.port.out.ProductManagementQueryPort;
import com.tastyhouse.application.product.port.out.ProductSearchCondition;
import com.tastyhouse.apicommon.common.PaginationResponse;
import com.tastyhouse.adminapplication.product.response.ProductCategoryResponse;
import com.tastyhouse.adminapplication.product.response.ProductDetailResponse;
import com.tastyhouse.adminapplication.product.response.ProductImagesResponse;
import com.tastyhouse.adminapplication.product.response.ProductListItemResponse;
import com.tastyhouse.adminapplication.product.response.ProductOptionGroupResponse;
import com.tastyhouse.adminapplication.product.response.ProductOptionGroupsResponse;
import com.tastyhouse.adminapplication.product.response.ProductOptionResponse;
import com.tastyhouse.adminapplication.product.port.in.ProductQueryUseCase;

/**
 * 관리자 상품 조회 서비스. infrastructure의 read 어댑터 {@link ProductManagementQueryPort}만 주입하고, 조회 결과를
 * Response로 조립한다(private 매퍼). 생성·수정은 {@link ProductCommandService}가 담당한다.
 */
@Service
@Transactional(readOnly = true)
public class ProductQueryService implements ProductQueryUseCase {

    private final ProductManagementQueryPort productManagementQueryPort;

    public ProductQueryService(ProductManagementQueryPort productManagementQueryPort) {
        this.productManagementQueryPort = productManagementQueryPort;
    }

    @Override
    public PaginationResponse<ProductListItemResponse> getProducts(
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
        PageResult<ProductListItemResponse> pageResult = productManagementQueryPort.findProducts(condition, pageQuery)
            .map(this::toProductListItemResponse);
        return PaginationResponse.from(pageResult);
    }

    private ProductListItemResponse toProductListItemResponse(ProductListItemResult dto) {
        return ProductListItemResponse.from(
            dto.id(),
            dto.shopName(),
            dto.name(),
            dto.originalPrice(),
            dto.discountPrice(),
            dto.discountRate(),
            dto.representative(),
            dto.soldOut(),
            dto.visible(),
            dto.sort()
        );
    }

    @Override
    public ProductDetailResponse getProduct(Long id) {
        ProductDetailResult dto = productManagementQueryPort.findProductDetailById(id)
            .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.PRODUCT_NOT_FOUND));
        return toProductDetailResponse(dto);
    }

    private ProductDetailResponse toProductDetailResponse(ProductDetailResult dto) {
        return ProductDetailResponse.from(
            dto.id(),
            dto.shopId(),
            dto.productCategoryId(),
            dto.name(),
            dto.description(),
            dto.originalPrice(),
            dto.discountPrice(),
            dto.discountRate(),
            dto.rating(),
            dto.reviewCount(),
            dto.representative(),
            dto.spiciness(),
            dto.soldOut(),
            dto.visible(),
            dto.sort(),
            dto.createdAt(),
            dto.updatedAt()
        );
    }

    @Override
    public ProductOptionGroupsResponse getProductOptions(Long id) {
        ProductOptionsResult result = productManagementQueryPort.findProductOptions(id);
        List<ProductOptionGroupResponse> optionGroups = result.optionGroups().stream()
            .map(this::toOptionGroupResponse)
            .toList();
        return ProductOptionGroupsResponse.from(optionGroups);
    }

    private ProductOptionGroupResponse toOptionGroupResponse(OptionGroupResult dto) {
        List<ProductOptionResponse> options = dto.options().stream()
            .map(this::toOptionResponse)
            .toList();
        return ProductOptionGroupResponse.from(
            dto.id(),
            dto.name(),
            dto.description(),
            dto.required(),
            dto.multipleSelect(),
            dto.minSelect(),
            dto.maxSelect(),
            dto.common(),
            dto.groupType(),
            options
        );
    }

    private ProductOptionResponse toOptionResponse(OptionResult dto) {
        return ProductOptionResponse.from(
            dto.id(),
            dto.name(),
            dto.additionalPrice(),
            dto.soldOut(),
            dto.cupCount(),
            dto.depositAmount(),
            dto.personalCupDiscountAmount()
        );
    }

    @Override
    public ProductImagesResponse getProductImages(Long id) {
        List<String> imageUrls = productManagementQueryPort.findProductImageUrls(id);
        return ProductImagesResponse.from(imageUrls);
    }

    @Override
    public List<ProductCategoryResponse> getProductCategories(Long shopId) {
        return productManagementQueryPort.findProductCategories(shopId).stream()
            .map(this::toProductCategoryResponse)
            .toList();
    }

    private ProductCategoryResponse toProductCategoryResponse(ProductCategoryResult dto) {
        return ProductCategoryResponse.from(
            dto.id(),
            dto.shopId(),
            dto.name(),
            dto.description(),
            dto.sort(),
            dto.visible()
        );
    }
}
