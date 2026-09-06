package com.tastyhouse.application.crawling.bbq;

import com.tastyhouse.application.shared.marker.BatchApp;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.domain.file.vo.UploadedFileId;
import com.tastyhouse.domain.product.model.Product;
import com.tastyhouse.domain.product.model.ProductCategory;
import com.tastyhouse.domain.product.model.ProductOptionGroup;
import com.tastyhouse.domain.product.model.ProductOptionGroupType;
import com.tastyhouse.domain.product.repository.ProductCategoryRepository;
import com.tastyhouse.domain.product.service.ProductRegistrationService;
import com.tastyhouse.domain.product.vo.BbqCategoryId;
import com.tastyhouse.domain.product.vo.BbqMenuId;
import com.tastyhouse.domain.product.vo.ProductCategoryId;
import com.tastyhouse.domain.product.vo.ProductId;
import com.tastyhouse.domain.product.vo.ProductOptionGroupId;
import com.tastyhouse.domain.shop.vo.ShopId;
import com.tastyhouse.application.product.port.out.ProductBbqSyncQueryPort;
import com.tastyhouse.application.product.port.out.ProductBbqSyncTargetResult;

@Service
@BatchApp
@Transactional
public class BbqProductSyncService {

    private final ProductRegistrationService productRegistrationService;
    private final ProductCategoryRepository productCategoryRepository;
    private final ProductBbqSyncQueryPort productBbqSyncQueryPort;

    public BbqProductSyncService(
        ProductRegistrationService productRegistrationService,
        ProductCategoryRepository productCategoryRepository,
        ProductBbqSyncQueryPort productBbqSyncQueryPort
    ) {
        this.productRegistrationService = productRegistrationService;
        this.productCategoryRepository = productCategoryRepository;
        this.productBbqSyncQueryPort = productBbqSyncQueryPort;
    }

    public Long resolveCategoryId(Long shopId, String name, int sort) {
        ShopId targetShopId = ShopId.of(shopId);
        List<ProductCategory> existing = productCategoryRepository.findCategoriesByNameAndShopId(name, targetShopId);
        if (!existing.isEmpty()) {
            return existing.getFirst().getId();
        }
        return productRegistrationService.createProductCategory(targetShopId, name, null, sort, true).getId();
    }

    public Long createCrawledProduct(BbqProductRegistration registration) {
        Product product = productRegistrationService.createProduct(
            ShopId.of(registration.shopId()),
            ProductCategoryId.of(registration.productCategoryId()),
            registration.name(),
            registration.description(),
            registration.originalPrice(),
            null,
            null,
            null,
            0,
            false,
            null,
            registration.soldOut(),
            true,
            registration.sort(),
            false,
            null,
            false
        );

        if (registration.imageFileId() != null) {
            productRegistrationService.saveProductImage(
                product.getProductId(), UploadedFileId.of(registration.imageFileId()), 0, true
            );
        }

        productRegistrationService.saveProductBbq(
            product.getProductId(),
            BbqMenuId.of(registration.bbqMenuId()),
            BbqCategoryId.of(registration.bbqCategoryId()),
            false
        );

        return product.getId();
    }

    @Transactional(readOnly = true)
    public Optional<ProductBbqSyncTargetResult> findFirstOptionSyncTarget() {
        return productBbqSyncQueryPort.findFirstBbqSyncTarget();
    }

    public void syncOptions(Long productId, List<BbqOptionGroupRegistration> optionGroups) {
        for (BbqOptionGroupRegistration registration : optionGroups) {
            saveOptionGroupWithOptions(registration);
        }
        markOptionsSynced(productId);
    }

    private void saveOptionGroupWithOptions(BbqOptionGroupRegistration registration) {
        ProductOptionGroup optionGroup = productRegistrationService.saveProductOptionGroup(
            ProductId.of(registration.productId()),
            registration.name(),
            null,
            registration.required(),
            registration.multipleSelect(),
            registration.minSelect(),
            registration.maxSelect(),
            registration.sort(),
            true,

            ProductOptionGroupType.NORMAL
        );

        List<BbqOptionRegistration> options = registration.options();
        for (int i = 0; i < options.size(); i++) {
            BbqOptionRegistration option = options.get(i);
            productRegistrationService.saveProductOption(
                ProductOptionGroupId.of(optionGroup.getId()),
                option.name(),
                option.additionalPrice(),
                i,
                option.soldOut(),
                option.visible(),

                null,
                null
            );
        }
    }

    private void markOptionsSynced(Long productId) {
        ProductId targetProductId = ProductId.of(productId);
        productRegistrationService.markBbqOptionsSynced(targetProductId);
    }
}
