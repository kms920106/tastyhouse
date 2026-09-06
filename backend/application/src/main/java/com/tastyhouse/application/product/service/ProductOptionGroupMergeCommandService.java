package com.tastyhouse.application.product.service;

import com.tastyhouse.application.shared.marker.CeoApp;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.application.product.port.in.ProductOptionGroupMergeCommand;
import com.tastyhouse.application.product.port.in.ProductOptionGroupMergeCommandUseCase;
import com.tastyhouse.application.product.port.in.ProductOptionGroupMergeExclusionCreateCommand;
import com.tastyhouse.application.shop.service.ShopOwnershipValidator;
import com.tastyhouse.domain.ceo.vo.CeoId;
import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.ErrorCode;
import com.tastyhouse.domain.product.model.ProductOption;
import com.tastyhouse.domain.product.model.ProductOptionGroup;
import com.tastyhouse.domain.product.model.ProductOptionGroupMergeEntryType;
import com.tastyhouse.domain.product.model.ProductOptionGroupMergeExclusion;
import com.tastyhouse.domain.product.repository.ProductOptionGroupMergeExclusionRepository;
import com.tastyhouse.domain.product.repository.ProductOptionRepository;
import com.tastyhouse.domain.product.service.ProductOptionGroupMergeService;
import com.tastyhouse.domain.product.service.ProductOptionGroupSignature;
import com.tastyhouse.domain.product.vo.ProductOptionGroupId;
import com.tastyhouse.domain.shop.vo.ShopId;

@Service
@CeoApp
@Transactional
public class ProductOptionGroupMergeCommandService implements ProductOptionGroupMergeCommandUseCase {

    private final ProductOptionGroupMergeService productOptionGroupMergeService;
    private final ProductOptionGroupMergeExclusionRepository exclusionRepository;
    private final ProductOptionRepository productOptionRepository;
    private final ShopOwnershipValidator shopOwnershipValidator;
    private final ProductOptionGroupOwnershipValidator productOptionGroupOwnershipValidator;

    public ProductOptionGroupMergeCommandService(
        ProductOptionGroupMergeService productOptionGroupMergeService,
        ProductOptionGroupMergeExclusionRepository exclusionRepository,
        ProductOptionRepository productOptionRepository,
        ShopOwnershipValidator shopOwnershipValidator,
        ProductOptionGroupOwnershipValidator productOptionGroupOwnershipValidator
    ) {
        this.productOptionGroupMergeService = productOptionGroupMergeService;
        this.exclusionRepository = exclusionRepository;
        this.productOptionRepository = productOptionRepository;
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

    @Override
    public Long excludeMergeSuggestion(ProductOptionGroupMergeExclusionCreateCommand command) {
        Long ceoId = command.ceoId();
        Long shopId = command.shopId();
        String signature = command.signature();
        List<Long> optionGroupIds = command.optionGroupIds();

        shopOwnershipValidator.validateOwnership(ceoId, shopId);

        List<Long> targetIds = distinct(optionGroupIds);
        if (targetIds.isEmpty()) {
            throw new BusinessException(ErrorCode.PRODUCT_OPTION_GROUP_MERGE_TARGET_EMPTY);
        }
        validateSignature(shopId, signature, targetIds);

        return exclusionRepository.findByShopIdAndGroupSignature(ShopId.of(shopId), signature)
            .map(ProductOptionGroupMergeExclusion::getId)
            .orElseGet(() -> exclusionRepository.save(ProductOptionGroupMergeExclusion.of(
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
                productOptionRepository.findAllByOptionGroupId(group.getProductOptionGroupId());

            if (!Objects.equals(signature, ProductOptionGroupSignature.of(group, options))) {
                throw new BusinessException(ErrorCode.PRODUCT_OPTION_GROUP_MERGE_SIGNATURE_MISMATCH);
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
