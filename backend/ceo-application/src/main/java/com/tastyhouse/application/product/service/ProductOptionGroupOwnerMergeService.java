package com.tastyhouse.application.product.service;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.domain.ceo.vo.CeoId;
import com.tastyhouse.domain.product.model.ProductOptionGroupMergeEntryType;
import com.tastyhouse.domain.product.vo.ProductOptionGroupId;
import com.tastyhouse.domain.shop.vo.ShopId;
import com.tastyhouse.application.product.port.in.ProductOptionGroupMergeCommand;
import com.tastyhouse.application.product.port.in.ProductOptionGroupOwnerMergeUseCase;
import com.tastyhouse.application.shop.service.ShopOwnershipValidator;

@Service
@Transactional
class ProductOptionGroupOwnerMergeService implements ProductOptionGroupOwnerMergeUseCase {

    private final ProductOptionGroupMergeService productOptionGroupMergeService;
    private final ShopOwnershipValidator shopOwnershipValidator;
    private final ProductOptionGroupOwnershipValidator productOptionGroupOwnershipValidator;

    public ProductOptionGroupOwnerMergeService(
        ProductOptionGroupMergeService productOptionGroupMergeService,
        ShopOwnershipValidator shopOwnershipValidator,
        ProductOptionGroupOwnershipValidator productOptionGroupOwnershipValidator
    ) {
        this.productOptionGroupMergeService = productOptionGroupMergeService;
        this.shopOwnershipValidator = shopOwnershipValidator;
        this.productOptionGroupOwnershipValidator = productOptionGroupOwnershipValidator;
    }

    @Override
    public Long mergeProductOptionGroups(ProductOptionGroupMergeCommand command) {
        Long ceoId = command.ceoId();
        Long shopId = command.shopId();
        Long baseOptionGroupId = command.baseOptionGroupId();
        List<Long> optionGroupIds = command.optionGroupIds();
        String entryType = command.entryType();

        shopOwnershipValidator.validateOwnership(ceoId, shopId);

        productOptionGroupOwnershipValidator.validateOptionGroupShop(shopId, baseOptionGroupId);

        return productOptionGroupMergeService.merge(
            ShopId.of(shopId),
            ProductOptionGroupId.of(baseOptionGroupId),
            distinct(optionGroupIds).stream().map(ProductOptionGroupId::of).toList(),
            ProductOptionGroupMergeEntryType.from(entryType),
            CeoId.of(ceoId)
        );
    }

    private List<Long> distinct(List<Long> ids) {
        if (ids == null) {
            return List.of();
        }
        Set<Long> unique = new LinkedHashSet<>();
        ids.stream().filter(Objects::nonNull).forEach(unique::add);
        return List.copyOf(unique);
    }
}
