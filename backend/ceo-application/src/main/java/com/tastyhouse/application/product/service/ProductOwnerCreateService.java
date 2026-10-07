package com.tastyhouse.application.product.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.domain.product.model.Product;
import com.tastyhouse.domain.product.model.ProductShopLinkSpec;
import com.tastyhouse.domain.product.vo.ProductCategoryId;
import com.tastyhouse.domain.shop.vo.ShopId;
import com.tastyhouse.application.product.port.in.ProductOwnerCreateCommand;
import com.tastyhouse.application.product.port.in.ProductOwnerCreateUseCase;
import com.tastyhouse.application.product.port.in.ProductShopLinkItemCommand;
import com.tastyhouse.application.product.port.out.write.ProductPersistencePort;
import com.tastyhouse.application.shop.service.OwnedShopIdProvider;
import com.tastyhouse.application.shop.service.ProhibitedWordValidator;
import com.tastyhouse.application.shop.service.ShopOwnershipValidator;

@Service
@Transactional
class ProductOwnerCreateService implements ProductOwnerCreateUseCase {

    private static final boolean DEFAULT_VISIBLE = true;

    private final ProductRegistrationService productRegistrationService;
    private final ProductPersistencePort productPersistencePort;
    private final ProhibitedWordValidator prohibitedWordValidator;
    private final ProductNameValidator productNameValidator;
    private final ProductShopLinkService productShopLinkService;
    private final ShopOwnershipValidator shopOwnershipValidator;
    private final OwnedShopIdProvider ownedShopIdProvider;

    public ProductOwnerCreateService(
        ProductRegistrationService productRegistrationService,
        ProductPersistencePort productPersistencePort,
        ProhibitedWordValidator prohibitedWordValidator,
        ProductNameValidator productNameValidator,
        ProductShopLinkService productShopLinkService,
        ShopOwnershipValidator shopOwnershipValidator,
        OwnedShopIdProvider ownedShopIdProvider
    ) {
        this.productRegistrationService = productRegistrationService;
        this.productPersistencePort = productPersistencePort;
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

    private void validateTexts(String name, String composition, String description) {
        prohibitedWordValidator.validate(name);
        prohibitedWordValidator.validate(composition);
        prohibitedWordValidator.validate(description);
    }

    private Integer nextSort(Long shopId, ProductCategoryId productCategoryId) {
        return productPersistencePort.findAllByShopIdAndCategoryId(ShopId.of(shopId), productCategoryId).size();
    }

    private ProductCategoryId toProductCategoryId(Long productCategoryId) {
        return productCategoryId != null ? ProductCategoryId.of(productCategoryId) : null;
    }
}
