package com.tastyhouse.application.product.service;

import com.tastyhouse.application.shared.marker.CeoApp;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.application.product.port.out.ProductAvailabilityChangeView;
import com.tastyhouse.application.product.port.in.ProductOwnerCreateCommand;
import com.tastyhouse.application.product.port.in.ProductOwnerCreateUseCase;
import com.tastyhouse.application.product.port.in.ProductDeleteCommand;
import com.tastyhouse.application.product.port.in.ProductDeleteUseCase;
import com.tastyhouse.application.product.port.in.ProductShopLinkItemCommand;
import com.tastyhouse.application.product.port.in.ProductOwnerUpdateCommand;
import com.tastyhouse.application.product.port.in.ProductOwnerUpdateUseCase;
import com.tastyhouse.application.shop.service.OwnedShopIdProvider;
import com.tastyhouse.application.shop.service.ShopOwnershipValidator;
import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.ErrorCode;
import com.tastyhouse.domain.exception.ResourceNotFoundException;
import com.tastyhouse.domain.product.model.Product;
import com.tastyhouse.domain.product.repository.ProductRepository;
import com.tastyhouse.domain.product.service.ProductAvailabilityChangeResult;
import com.tastyhouse.domain.product.service.ProductDeletionService;
import com.tastyhouse.domain.product.service.ProductRegistrationService;
import com.tastyhouse.domain.product.service.ProductShopLinkService;
import com.tastyhouse.domain.product.service.ProductShopLinkSpec;
import com.tastyhouse.domain.product.vo.ProductCategoryId;
import com.tastyhouse.domain.product.vo.ProductId;
import com.tastyhouse.domain.shop.service.ProhibitedWordValidator;
import com.tastyhouse.domain.shop.vo.ShopId;

@Service
@CeoApp
@Transactional
public class ProductOwnerCommandService implements ProductOwnerCreateUseCase, ProductOwnerUpdateUseCase, ProductDeleteUseCase {

    private static final boolean DEFAULT_VISIBLE = true;

    private final ProductRegistrationService productRegistrationService;
    private final ProductDeletionService productDeletionService;
    private final ProductRepository productRepository;
    private final ProhibitedWordValidator prohibitedWordValidator;
    private final ProductNameValidator productNameValidator;
    private final ProductShopLinkService productShopLinkService;
    private final ShopOwnershipValidator shopOwnershipValidator;
    private final OwnedShopIdProvider ownedShopIdProvider;

    public ProductOwnerCommandService(
        ProductRegistrationService productRegistrationService,
        ProductDeletionService productDeletionService,
        ProductRepository productRepository,
        ProhibitedWordValidator prohibitedWordValidator,
        ProductNameValidator productNameValidator,
        ProductShopLinkService productShopLinkService,
        ShopOwnershipValidator shopOwnershipValidator,
        OwnedShopIdProvider ownedShopIdProvider
    ) {
        this.productRegistrationService = productRegistrationService;
        this.productDeletionService = productDeletionService;
        this.productRepository = productRepository;
        this.prohibitedWordValidator = prohibitedWordValidator;
        this.productNameValidator = productNameValidator;
        this.productShopLinkService = productShopLinkService;
        this.shopOwnershipValidator = shopOwnershipValidator;
        this.ownedShopIdProvider = ownedShopIdProvider;
    }

    @Override
    public Long createProduct(ProductOwnerCreateCommand command) {
        Long ceoId = command.ceoId();
        Long shopId = command.shopId();
        Long productCategoryId = command.productCategoryId();
        String name = command.name();
        String composition = command.composition();
        String description = command.description();
        Integer originalPrice = command.originalPrice();
        Integer discountPrice = command.discountPrice();
        Boolean singleServing = command.singleServing();
        Integer spiciness = command.spiciness();
        Boolean representative = command.representative();
        Boolean ratingExcluded = command.ratingExcluded();
        List<ProductShopLinkItemCommand> links = command.links();

        shopOwnershipValidator.validateOwnership(ceoId, shopId);
        validateTexts(name, composition, description);
        productNameValidator.validateForCreate(shopId, name);

        ProductCategoryId categoryId = toProductCategoryId(productCategoryId);
        Product created = productRegistrationService.createProduct(
            ShopId.of(shopId),
            categoryId,
            name,
            description,
            originalPrice,
            discountPrice,
            null,
            null,
            null,
            Boolean.TRUE.equals(representative),
            spiciness,
            false,
            DEFAULT_VISIBLE,
            nextSort(shopId, categoryId),
            Boolean.TRUE.equals(ratingExcluded),
            composition,
            Boolean.TRUE.equals(singleServing)
        );

        productShopLinkService.createInitialLinks(
            created.getProductId(),
            toProductShopLinkSpecs(links),
            ownedShopIdProvider.findOwnedShopIds(ceoId)
        );
        return created.getId();
    }

    private List<ProductShopLinkSpec> toProductShopLinkSpecs(List<ProductShopLinkItemCommand> links) {
        if (links == null) {
            return List.of();
        }
        return links.stream()
            .map(link -> ProductShopLinkSpec.of(link.shopId(), link.productCategoryId()))
            .toList();
    }

    @Override
    public void updateProduct(ProductOwnerUpdateCommand command) {
        Long ceoId = command.ceoId();
        Long productId = command.productId();
        Long shopId = command.shopId();
        Long productCategoryId = command.productCategoryId();
        String name = command.name();
        String composition = command.composition();
        String description = command.description();
        Integer originalPrice = command.originalPrice();
        Integer discountPrice = command.discountPrice();
        Boolean singleServing = command.singleServing();
        Integer spiciness = command.spiciness();
        Boolean representative = command.representative();
        Boolean ratingExcluded = command.ratingExcluded();
        String weightText = command.weightText();

        shopOwnershipValidator.validateOwnership(ceoId, shopId);
        validateTexts(name, composition, description);
        productNameValidator.validateForUpdate(shopId, productId, name);

        Product product = loadOwnedProduct(shopId, productId);
        ProductCategoryId categoryId = toProductCategoryId(productCategoryId);
        boolean categoryChanged = !isSameCategory(product.getProductCategoryId(), categoryId);

        product.changeDetails(
            categoryId,
            name,
            composition,
            description,
            originalPrice,
            discountPrice,
            null,
            Boolean.TRUE.equals(singleServing),
            spiciness,
            Boolean.TRUE.equals(representative),
            Boolean.TRUE.equals(ratingExcluded),
            weightText
        );
        if (categoryChanged) {
            product.relocate(categoryId, nextSort(shopId, categoryId));
        }
        productRepository.save(product);
    }

    @Override
    public ProductAvailabilityChangeView deleteProducts(ProductDeleteCommand command) {
        Long ceoId = command.ceoId();
        Long shopId = command.shopId();
        List<Long> productIds = command.productIds();

        shopOwnershipValidator.validateOwnership(ceoId, shopId);
        return toChangeView(productDeletionService.deleteProducts(ShopId.of(shopId), toProductIds(productIds)));
    }

    private void validateTexts(String name, String composition, String description) {
        prohibitedWordValidator.validate(name);
        prohibitedWordValidator.validate(composition);
        prohibitedWordValidator.validate(description);
    }

    private Product loadOwnedProduct(Long shopId, Long productId) {
        Product product = productRepository.findById(ProductId.of(productId))
            .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.PRODUCT_NOT_FOUND));
        if (!product.getShopId().equals(ShopId.of(shopId))) {
            throw new ResourceNotFoundException(ErrorCode.PRODUCT_NOT_FOUND);
        }
        return product;
    }

    private Integer nextSort(Long shopId, ProductCategoryId productCategoryId) {
        return productRepository.findAllByShopIdAndCategoryId(ShopId.of(shopId), productCategoryId).size();
    }

    private boolean isSameCategory(ProductCategoryId current, ProductCategoryId requested) {
        if (current == null || requested == null) {
            return current == requested;
        }
        return current.equals(requested);
    }

    private ProductCategoryId toProductCategoryId(Long productCategoryId) {
        return productCategoryId != null ? ProductCategoryId.of(productCategoryId) : null;
    }

    private List<ProductId> toProductIds(List<Long> productIds) {
        if (productIds == null || productIds.isEmpty()) {
            throw new BusinessException(ErrorCode.PRODUCT_AVAILABILITY_TARGET_EMPTY);
        }
        return productIds.stream().map(ProductId::of).toList();
    }

    private ProductAvailabilityChangeView toChangeView(ProductAvailabilityChangeResult result) {
        return new ProductAvailabilityChangeView(
            result.succeeded(),
            result.failed().stream()
                .map(failure -> new ProductAvailabilityChangeView.Failure(
                    failure.id(),
                    failure.name(),
                    failure.errorCode()
                ))
                .toList()
        );
    }

}
