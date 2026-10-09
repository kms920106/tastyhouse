package com.tastyhouse.application.product.service;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.domain.ceo.vo.CeoId;
import com.tastyhouse.domain.product.model.ProductOption;
import com.tastyhouse.domain.product.model.ProductOptionGroup;
import com.tastyhouse.domain.product.model.ProductOptionGroupMergeExclusion;
import com.tastyhouse.domain.product.model.ProductOptionGroupSignature;
import com.tastyhouse.domain.shop.vo.ShopId;
import com.tastyhouse.application.product.port.in.ProductOptionGroupMergeExclusionCreateCommand;
import com.tastyhouse.application.product.port.in.ProductOptionGroupMergeExclusionCreateUseCase;
import com.tastyhouse.application.product.port.out.write.ProductOptionGroupMergeExclusionLoadPort;
import com.tastyhouse.application.product.port.out.write.ProductOptionGroupMergeExclusionSavePort;
import com.tastyhouse.application.product.port.out.write.ProductOptionLoadPort;
import com.tastyhouse.application.shared.exception.ApplicationException;
import com.tastyhouse.application.shared.exception.CeoErrorCode;
import com.tastyhouse.application.shop.service.ShopOwnershipValidator;

@Service
@Transactional
class ProductOptionGroupMergeExclusionCreateService implements ProductOptionGroupMergeExclusionCreateUseCase {

    private final ProductOptionGroupMergeExclusionLoadPort exclusionLoadPort;
    private final ProductOptionGroupMergeExclusionSavePort exclusionSavePort;
    private final ProductOptionLoadPort productOptionLoadPort;
    private final ShopOwnershipValidator shopOwnershipValidator;
    private final ProductOptionGroupOwnershipValidator productOptionGroupOwnershipValidator;

    public ProductOptionGroupMergeExclusionCreateService(
        ProductOptionGroupMergeExclusionLoadPort exclusionLoadPort,
        ProductOptionGroupMergeExclusionSavePort exclusionSavePort,
        ProductOptionLoadPort productOptionLoadPort,
        ShopOwnershipValidator shopOwnershipValidator,
        ProductOptionGroupOwnershipValidator productOptionGroupOwnershipValidator
    ) {
        this.exclusionLoadPort = exclusionLoadPort;
        this.exclusionSavePort = exclusionSavePort;
        this.productOptionLoadPort = productOptionLoadPort;
        this.shopOwnershipValidator = shopOwnershipValidator;
        this.productOptionGroupOwnershipValidator = productOptionGroupOwnershipValidator;
    }

    @Override
    public Long excludeMergeSuggestion(ProductOptionGroupMergeExclusionCreateCommand command) {
        Long ceoId = command.ceoId();
        Long shopId = command.shopId();
        String signature = command.signature();
        List<Long> optionGroupIds = command.optionGroupIds();

        shopOwnershipValidator.validateOwnership(ceoId, shopId);

        List<Long> targetIds = distinct(optionGroupIds);
        if (targetIds.isEmpty()) {
            throw new ApplicationException(CeoErrorCode.PRODUCT_OPTION_GROUP_MERGE_TARGET_EMPTY);
        }
        validateSignature(shopId, signature, targetIds);

        return exclusionLoadPort.findByShopIdAndGroupSignature(ShopId.of(shopId), signature)
            .map(ProductOptionGroupMergeExclusion::getId)
            .orElseGet(() -> exclusionSavePort.save(ProductOptionGroupMergeExclusion.of(
                ShopId.of(shopId),
                signature,
                CeoId.of(ceoId)
            )).getId());
    }

    private void validateSignature(Long shopId, String signature, List<Long> optionGroupIds) {
        for (Long optionGroupId : optionGroupIds) {
            ProductOptionGroup group =
                productOptionGroupOwnershipValidator.loadOwnedOptionGroup(shopId, optionGroupId);
            List<ProductOption> options =
                productOptionLoadPort.findAllByOptionGroupId(group.getProductOptionGroupId());

            if (!Objects.equals(signature, ProductOptionGroupSignature.of(group, options))) {
                throw new ApplicationException(CeoErrorCode.PRODUCT_OPTION_GROUP_MERGE_SIGNATURE_MISMATCH);
            }
        }
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
