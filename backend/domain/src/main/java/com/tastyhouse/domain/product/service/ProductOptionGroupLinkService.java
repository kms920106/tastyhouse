package com.tastyhouse.domain.product.service;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.ErrorCode;
import com.tastyhouse.domain.product.model.Product;
import com.tastyhouse.domain.product.model.ProductOptionGroupLink;
import com.tastyhouse.domain.product.repository.ProductOptionGroupLinkRepository;
import com.tastyhouse.domain.product.repository.ProductRepository;
import com.tastyhouse.domain.product.vo.ProductId;
import com.tastyhouse.domain.product.vo.ProductOptionGroupId;
import com.tastyhouse.domain.shop.vo.ShopId;

public class ProductOptionGroupLinkService {
    private final ProductOptionGroupLinkRepository linkRepository;
    private final ProductRepository productRepository;

    public ProductOptionGroupLinkService(
        ProductOptionGroupLinkRepository linkRepository,
        ProductRepository productRepository
    ) {
        this.linkRepository = linkRepository;
        this.productRepository = productRepository;
    }

    public void link(ProductId productId, ProductOptionGroupId optionGroupId) {
        if (linkRepository.existsByProductIdAndOptionGroupId(productId, optionGroupId)) {
            return;
        }
        validateSameShop(productId, optionGroupId);

        int nextSort = linkRepository.findAllByProductId(productId).size();
        linkRepository.save(ProductOptionGroupLink.of(productId, optionGroupId, nextSort));
    }

    public void unlink(ProductId productId, ProductOptionGroupId optionGroupId) {
        ProductOptionGroupLink link = linkRepository
            .findByProductIdAndOptionGroupId(productId, optionGroupId)
            .orElseThrow(() -> new BusinessException(ErrorCode.PRODUCT_OPTION_GROUP_NOT_FOUND));

        if (linkRepository.findAllByOptionGroupId(optionGroupId).size() <= 1) {
            throw new BusinessException(ErrorCode.PRODUCT_OPTION_GROUP_LAST_LINK_CANNOT_UNLINK);
        }

        linkRepository.delete(link);
        renumber(productId);
    }

    public void reorder(ProductId productId, List<ProductOptionGroupId> orderedGroupIds) {
        List<ProductOptionGroupLink> current = linkRepository.findAllByProductId(productId);
        Map<Long, ProductOptionGroupLink> byGroupId = current.stream()
            .collect(Collectors.toMap(link -> link.getOptionGroupId().value(), Function.identity()));

        List<Long> requested = distinctRawIds(orderedGroupIds);
        if (byGroupId.size() != requested.size() || !byGroupId.keySet().containsAll(requested)) {
            throw new BusinessException(ErrorCode.PRODUCT_ORDER_TARGET_MISMATCH);
        }

        for (int index = 0; index < requested.size(); index++) {
            ProductOptionGroupLink link = byGroupId.get(requested.get(index));
            link.changeSort(index);
            linkRepository.save(link);
        }
    }

    public void relink(
        List<ProductId> productIds,
        ProductOptionGroupId fromOptionGroupId,
        ProductOptionGroupId toOptionGroupId
    ) {
        Set<Long> affectedProductIds = new LinkedHashSet<>();

        for (ProductId productId : productIds) {
            ProductOptionGroupLink link = linkRepository
                .findByProductIdAndOptionGroupId(productId, fromOptionGroupId)
                .orElse(null);
            if (link == null) {
                continue;
            }
            affectedProductIds.add(productId.value());

            Integer preservedSort = link.getSort();
            linkRepository.delete(link);

            if (!linkRepository.existsByProductIdAndOptionGroupId(productId, toOptionGroupId)) {
                linkRepository.save(ProductOptionGroupLink.of(productId, toOptionGroupId, preservedSort));
            }
        }

        affectedProductIds.forEach(productId -> renumber(ProductId.of(productId)));
    }

    public ShopId findOwningShopId(ProductOptionGroupId optionGroupId) {
        return linkRepository.findAllByOptionGroupId(optionGroupId).stream()
            .map(ProductOptionGroupLink::getProductId)
            .map(productRepository::findById)
            .filter(java.util.Optional::isPresent)
            .map(java.util.Optional::get)
            .map(Product::getShopId)
            .findFirst()
            .orElse(null);
    }

    private void validateSameShop(ProductId productId, ProductOptionGroupId optionGroupId) {
        Product product = productRepository.findById(productId)
            .orElseThrow(() -> new BusinessException(ErrorCode.PRODUCT_NOT_FOUND));

        ShopId owner = findOwningShopId(optionGroupId);
        if (owner != null && !owner.equals(product.getShopId())) {
            throw new BusinessException(ErrorCode.PRODUCT_OPTION_GROUP_SHOP_MISMATCH);
        }
    }

    private void renumber(ProductId productId) {
        List<ProductOptionGroupLink> remaining = linkRepository.findAllByProductId(productId);
        for (int index = 0; index < remaining.size(); index++) {
            ProductOptionGroupLink link = remaining.get(index);
            link.changeSort(index);
            linkRepository.save(link);
        }
    }

    private List<Long> distinctRawIds(List<ProductOptionGroupId> ids) {
        if (ids == null) {
            return List.of();
        }
        Set<Long> raw = new LinkedHashSet<>();
        ids.stream().filter(Objects::nonNull).forEach(id -> raw.add(id.value()));
        return new ArrayList<>(raw);
    }
}
