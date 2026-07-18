package com.tastyhouse.adminapi.product;

import java.math.BigDecimal;
import java.util.List;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import com.tastyhouse.core.domain.product.domain.model.Product;
import com.tastyhouse.core.domain.product.domain.model.ProductCategory;
import com.tastyhouse.core.domain.product.domain.model.ProductOptionGroup;
import com.tastyhouse.core.domain.product.domain.vo.ProductId;
import com.tastyhouse.core.domain.product.application.ProductCommandService;
import com.tastyhouse.core.domain.product.application.ProductQueryService;
import com.tastyhouse.core.domain.product.application.dto.ProductSearchCondition;
import com.tastyhouse.core.domain.product.application.dto.command.ProductCategoryCreateCommand;
import com.tastyhouse.core.domain.product.application.dto.command.ProductCreateCommand;
import com.tastyhouse.core.domain.product.application.dto.command.ProductUpdateCommand;
import com.tastyhouse.core.domain.product.application.dto.command.SaveProductImageCommand;
import com.tastyhouse.core.domain.product.application.dto.command.SaveProductOptionCommand;
import com.tastyhouse.core.domain.product.application.dto.command.SaveProductOptionGroupCommand;
import com.tastyhouse.core.domain.product.application.dto.result.OptionGroupResult;
import com.tastyhouse.core.domain.product.application.dto.result.OptionResult;
import com.tastyhouse.core.domain.product.application.dto.result.ProductListItemResult;
import com.tastyhouse.core.domain.product.application.dto.result.ProductOptionsResult;
import com.tastyhouse.core.exception.EntityNotFoundException;
import com.tastyhouse.core.exception.ErrorCode;
import com.tastyhouse.core.shared.page.PageResult;
import com.tastyhouse.external.file.FileService;
import com.tastyhouse.adminapi.common.PaginationResponse;
import com.tastyhouse.adminapi.product.response.ProductCategoryResponse;
import com.tastyhouse.adminapi.product.response.ProductDetailResponse;
import com.tastyhouse.adminapi.product.response.ProductImagesResponse;
import com.tastyhouse.adminapi.product.response.ProductListItemResponse;
import com.tastyhouse.adminapi.product.response.ProductOptionGroupResponse;
import com.tastyhouse.adminapi.product.response.ProductOptionGroupsResponse;
import com.tastyhouse.adminapi.product.response.ProductOptionResponse;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductCommandService productCommandService;
    private final ProductQueryService productQueryService;
    private final FileService fileService;

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
        PageResult<ProductListItemResponse> pageResult = productQueryService.findProducts(condition, page, size)
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

    public Long createProduct(
        Long shopId,
        Long productCategoryId,
        String name,
        String description,
        Integer originalPrice,
        Integer discountPrice,
        BigDecimal discountRate,
        Double rating,
        Integer reviewCount,
        boolean representative,
        Integer spiciness,
        boolean soldOut,
        boolean visible,
        Integer sort
    ) {
        ProductCreateCommand command = ProductCreateCommand.of(
            shopId, productCategoryId, name, description,
            originalPrice, discountPrice, discountRate,
            rating, reviewCount, representative, spiciness,
            soldOut, visible, sort
        );
        Product product = productCommandService.createProduct(command);
        return product.getId();
    }

    public ProductDetailResponse getProduct(Long id) {
        ProductId productId = ProductId.of(id);
        Product product = productQueryService.findProductById(productId)
            .orElseThrow(() -> new EntityNotFoundException(ErrorCode.PRODUCT_NOT_FOUND));
        return toProductDetailResponse(product);
    }

    private ProductDetailResponse toProductDetailResponse(Product product) {
        return ProductDetailResponse.from(
            product.getId(),
            product.getShopId(),
            product.getProductCategoryId(),
            product.getName(),
            product.getDescription(),
            product.getOriginalPrice(),
            product.getDiscountPrice(),
            product.getDiscountRate(),
            product.getRating(),
            product.getReviewCount(),
            product.isRepresentative(),
            product.getSpiciness(),
            product.isSoldOut(),
            product.isVisible(),
            product.getSort(),
            product.getCreatedAt(),
            product.getUpdatedAt()
        );
    }

    public void updateProduct(
        Long id,
        Long productCategoryId,
        String name,
        String description,
        Integer originalPrice,
        Integer discountPrice,
        BigDecimal discountRate,
        boolean representative,
        Integer spiciness,
        boolean soldOut,
        boolean visible,
        Integer sort
    ) {
        ProductId productId = ProductId.of(id);
        ProductUpdateCommand command = ProductUpdateCommand.of(
            productCategoryId, name, description,
            originalPrice, discountPrice, discountRate,
            representative, spiciness, soldOut, visible, sort
        );
        productCommandService.updateProduct(productId, command);
    }

    public void markSoldOut(Long id) {
        ProductId productId = ProductId.of(id);
        productCommandService.markSoldOut(productId);
    }

    public void deactivateProduct(Long id) {
        ProductId productId = ProductId.of(id);
        productCommandService.deactivateProduct(productId);
    }

    public ProductOptionGroupsResponse getProductOptions(Long id) {
        ProductOptionsResult result = productQueryService.findProductOptions(id);
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
            options
        );
    }

    private ProductOptionResponse toOptionResponse(OptionResult dto) {
        return ProductOptionResponse.from(dto.id(), dto.name(), dto.additionalPrice(), dto.soldOut());
    }

    public Long createProductOptionGroup(
        Long id,
        String name,
        String description,
        boolean required,
        boolean multipleSelect,
        Integer minSelect,
        Integer maxSelect,
        Integer sort,
        boolean visible
    ) {
        SaveProductOptionGroupCommand command = SaveProductOptionGroupCommand.of(
            id, name, description, required, multipleSelect, minSelect, maxSelect, sort, visible
        );
        ProductOptionGroup optionGroup = productCommandService.saveProductOptionGroup(command);
        return optionGroup.getId();
    }

    public void createProductOption(
        Long groupId,
        String name,
        Integer additionalPrice,
        Integer sort,
        boolean soldOut,
        boolean visible
    ) {
        SaveProductOptionCommand command = SaveProductOptionCommand.of(groupId, name, additionalPrice, sort, soldOut, visible);
        productCommandService.saveProductOption(command);
    }

    public ProductImagesResponse getProductImages(Long id) {
        List<String> imageUrls = productQueryService.getAllImageFilePaths(id).stream()
            .map(fileService::getUrlByPath)
            .toList();
        return ProductImagesResponse.from(imageUrls);
    }

    public void createProductImage(Long id, Long imageFileId, Integer sort, boolean visible) {
        SaveProductImageCommand command = SaveProductImageCommand.of(id, imageFileId, sort, visible);
        productCommandService.saveProductImage(command);
    }

    public List<ProductCategoryResponse> getProductCategories(Long shopId) {
        return productQueryService.findProductCategoriesByShopId(shopId).stream()
            .map(this::toProductCategoryResponse)
            .toList();
    }

    private ProductCategoryResponse toProductCategoryResponse(ProductCategory category) {
        return ProductCategoryResponse.from(
            category.getId(),
            category.getShopId(),
            category.getName(),
            category.getSort(),
            category.isVisible()
        );
    }

    public Long createProductCategory(Long shopId, String name, Integer sort, boolean visible) {
        ProductCategoryCreateCommand command = ProductCategoryCreateCommand.of(shopId, name, sort, visible);
        ProductCategory category = productCommandService.createProductCategory(command);
        return category.getId();
    }
}
