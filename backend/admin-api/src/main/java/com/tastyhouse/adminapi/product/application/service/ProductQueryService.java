package com.tastyhouse.adminapi.product.application.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.domain.exception.ErrorCode;
import com.tastyhouse.domain.exception.ResourceNotFoundException;
import com.tastyhouse.domain.shared.page.PageQuery;
import com.tastyhouse.domain.shared.page.PageResult;
import com.tastyhouse.infrastructure.product.query.OptionGroupResult;
import com.tastyhouse.infrastructure.product.query.OptionResult;
import com.tastyhouse.infrastructure.product.query.ProductCategoryResult;
import com.tastyhouse.infrastructure.product.query.ProductDetailResult;
import com.tastyhouse.infrastructure.product.query.ProductListItemResult;
import com.tastyhouse.infrastructure.product.query.ProductOptionsResult;
import com.tastyhouse.infrastructure.product.query.ProductQueryDao;
import com.tastyhouse.infrastructure.product.query.ProductSearchCondition;
import com.tastyhouse.apicommon.common.PaginationResponse;
import com.tastyhouse.adminapi.product.adapter.in.web.response.ProductCategoryResponse;
import com.tastyhouse.adminapi.product.adapter.in.web.response.ProductDetailResponse;
import com.tastyhouse.adminapi.product.adapter.in.web.response.ProductImagesResponse;
import com.tastyhouse.adminapi.product.adapter.in.web.response.ProductListItemResponse;
import com.tastyhouse.adminapi.product.adapter.in.web.response.ProductOptionGroupResponse;
import com.tastyhouse.adminapi.product.adapter.in.web.response.ProductOptionGroupsResponse;
import com.tastyhouse.adminapi.product.adapter.in.web.response.ProductOptionResponse;
import com.tastyhouse.adminapi.product.application.port.in.ProductQueryUseCase;

/**
 * 관리자 상품 조회 서비스. infrastructure의 read 어댑터 {@link ProductQueryDao}만 주입하고, 조회 결과를
 * Response로 조립한다(private 매퍼). 생성·수정은 {@link ProductCommandService}가 담당한다.
 */
@Service
@Transactional(readOnly = true)
public class ProductQueryService implements ProductQueryUseCase {

    private final ProductQueryDao productQueryDao;

    public ProductQueryService(ProductQueryDao productQueryDao) {
        this.productQueryDao = productQueryDao;
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
        PageResult<ProductListItemResponse> pageResult = productQueryDao.findProducts(condition, pageQuery)
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
        ProductDetailResult dto = productQueryDao.findProductDetailById(id)
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
        ProductOptionsResult result = productQueryDao.findProductOptions(id);
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
        List<String> imageUrls = productQueryDao.findProductImageUrls(id);
        return ProductImagesResponse.from(imageUrls);
    }

    @Override
    public List<ProductCategoryResponse> getProductCategories(Long shopId) {
        return productQueryDao.findProductCategories(shopId).stream()
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
