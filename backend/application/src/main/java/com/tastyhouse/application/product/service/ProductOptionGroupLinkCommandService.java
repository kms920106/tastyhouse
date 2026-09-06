package com.tastyhouse.application.product.service;

import com.tastyhouse.application.shared.marker.CeoApp;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.application.product.port.in.ProductOptionGroupLinkCommand;
import com.tastyhouse.application.product.port.in.ProductOptionGroupLinkCommandUseCase;
import com.tastyhouse.application.product.port.in.ProductOptionGroupOrderChangeCommand;
import com.tastyhouse.application.product.port.in.ProductOptionGroupUnlinkCommand;
import com.tastyhouse.application.shop.service.ShopOwnershipValidator;
import com.tastyhouse.domain.exception.ErrorCode;
import com.tastyhouse.domain.exception.ResourceNotFoundException;
import com.tastyhouse.domain.product.model.Product;
import com.tastyhouse.domain.product.repository.ProductRepository;
import com.tastyhouse.domain.product.service.ProductOptionGroupLinkService;
import com.tastyhouse.domain.product.vo.ProductId;
import com.tastyhouse.domain.product.vo.ProductOptionGroupId;
import com.tastyhouse.domain.shop.vo.ShopId;

@Service
@CeoApp
@Transactional
public class ProductOptionGroupLinkCommandService implements ProductOptionGroupLinkCommandUseCase {

    private final ProductOptionGroupLinkService productOptionGroupLinkService;
    private final ProductRepository productRepository;
    private final ShopOwnershipValidator shopOwnershipValidator;
    private final ProductOptionGroupOwnershipValidator productOptionGroupOwnershipValidator;

    public ProductOptionGroupLinkCommandService(
        ProductOptionGroupLinkService productOptionGroupLinkService,
        ProductRepository productRepository,
        ShopOwnershipValidator shopOwnershipValidator,
        ProductOptionGroupOwnershipValidator productOptionGroupOwnershipValidator
    ) {
        this.productOptionGroupLinkService = productOptionGroupLinkService;
        this.productRepository = productRepository;
        this.shopOwnershipValidator = shopOwnershipValidator;
        this.productOptionGroupOwnershipValidator = productOptionGroupOwnershipValidator;
    }

    @Override
    public void linkOptionGroup(ProductOptionGroupLinkCommand command) {
        Long ceoId = command.ceoId();
        Long shopId = command.shopId();
        Long productId = command.productId();
        Long optionGroupId = command.optionGroupId();

        shopOwnershipValidator.validateOwnership(ceoId, shopId);
        loadOwnedProduct(shopId, productId);
        productOptionGroupOwnershipValidator.validateOptionGroupShop(shopId, optionGroupId);

        productOptionGroupLinkService.link(ProductId.of(productId), ProductOptionGroupId.of(optionGroupId));
    }

    @Override
    public void unlinkOptionGroup(ProductOptionGroupUnlinkCommand command) {
        Long ceoId = command.ceoId();
        Long shopId = command.shopId();
        Long productId = command.productId();
        Long optionGroupId = command.optionGroupId();

        shopOwnershipValidator.validateOwnership(ceoId, shopId);
        loadOwnedProduct(shopId, productId);
        productOptionGroupOwnershipValidator.validateOptionGroupShop(shopId, optionGroupId);

        productOptionGroupLinkService.unlink(ProductId.of(productId), ProductOptionGroupId.of(optionGroupId));
    }

    @Override
    public void changeOptionGroupOrder(ProductOptionGroupOrderChangeCommand command) {
        Long ceoId = command.ceoId();
        Long shopId = command.shopId();
        Long productId = command.productId();
        List<Long> optionGroupIds = command.optionGroupIds();

        shopOwnershipValidator.validateOwnership(ceoId, shopId);
        loadOwnedProduct(shopId, productId);

        productOptionGroupLinkService.reorder(ProductId.of(productId), toOptionGroupIds(optionGroupIds));
    }

    private void loadOwnedProduct(Long shopId, Long productId) {
        Product product = productRepository.findById(ProductId.of(productId))
            .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.PRODUCT_NOT_FOUND));
        if (!product.getShopId().equals(ShopId.of(shopId))) {
            throw new ResourceNotFoundException(ErrorCode.PRODUCT_NOT_FOUND);
        }
    }

    private List<ProductOptionGroupId> toOptionGroupIds(List<Long> optionGroupIds) {
        return optionGroupIds.stream().map(ProductOptionGroupId::of).toList();
    }
}
