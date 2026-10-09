package com.tastyhouse.application.product.service;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.tastyhouse.domain.product.model.Product;
import com.tastyhouse.domain.product.model.ProductOptionGroupLink;
import com.tastyhouse.domain.product.vo.ProductId;
import com.tastyhouse.domain.product.vo.ProductOptionGroupId;
import com.tastyhouse.domain.shop.vo.ShopId;
import com.tastyhouse.application.product.port.out.write.ProductLoadPort;
import com.tastyhouse.application.product.port.out.write.ProductOptionGroupLinkLoadPort;
import com.tastyhouse.application.product.port.out.write.ProductOptionGroupLinkSavePort;
import com.tastyhouse.application.shared.exception.ApplicationErrorCode;
import com.tastyhouse.application.shared.exception.ApplicationException;
import com.tastyhouse.application.shared.exception.CeoErrorCode;

@Service
public class ProductOptionGroupLinkService {

    private final ProductOptionGroupLinkLoadPort linkLoadPort;
    private final ProductOptionGroupLinkSavePort linkSavePort;
    private final ProductLoadPort productLoadPort;

    public ProductOptionGroupLinkService(
        ProductOptionGroupLinkLoadPort linkLoadPort,
        ProductOptionGroupLinkSavePort linkSavePort,
        ProductLoadPort productLoadPort
    ) {
        this.linkLoadPort = linkLoadPort;
        this.linkSavePort = linkSavePort;
        this.productLoadPort = productLoadPort;
    }

    public void link(ProductId productId, ProductOptionGroupId optionGroupId) {
        if (linkLoadPort.existsByProductIdAndOptionGroupId(productId, optionGroupId)) {
            return;
        }
        validateSameShop(productId, optionGroupId);

        int nextSort = linkLoadPort.findAllByProductId(productId).size();
        linkSavePort.save(ProductOptionGroupLink.of(productId, optionGroupId, nextSort));
    }

    public void unlink(ProductId productId, ProductOptionGroupId optionGroupId) {
        ProductOptionGroupLink link = linkLoadPort
            .findByProductIdAndOptionGroupId(productId, optionGroupId)
            .orElseThrow(() -> new ApplicationException(ApplicationErrorCode.PRODUCT_OPTION_GROUP_NOT_FOUND));

        if (linkLoadPort.findAllByOptionGroupId(optionGroupId).size() <= 1) {
            throw new ApplicationException(CeoErrorCode.PRODUCT_OPTION_GROUP_LAST_LINK_CANNOT_UNLINK);
        }

        linkSavePort.delete(link);
        renumber(productId);
    }

    public void reorder(ProductId productId, List<ProductOptionGroupId> orderedGroupIds) {
        List<ProductOptionGroupLink> current = linkLoadPort.findAllByProductId(productId);
        Map<Long, ProductOptionGroupLink> byGroupId = current.stream()
            .collect(Collectors.toMap(link -> link.getOptionGroupId().value(), Function.identity()));

        List<Long> requested = distinctRawIds(orderedGroupIds);
        if (byGroupId.size() != requested.size() || !byGroupId.keySet().containsAll(requested)) {
            throw new ApplicationException(ApplicationErrorCode.PRODUCT_ORDER_TARGET_MISMATCH);
        }

        for (int index = 0; index < requested.size(); index++) {
            ProductOptionGroupLink link = byGroupId.get(requested.get(index));
            link.changeSort(index);
            linkSavePort.save(link);
        }
    }

    public void relink(
        List<ProductId> productIds,
        ProductOptionGroupId fromOptionGroupId,
        ProductOptionGroupId toOptionGroupId
    ) {
        Set<Long> affectedProductIds = new LinkedHashSet<>();

        for (ProductId productId : productIds) {
            ProductOptionGroupLink link = linkLoadPort
                .findByProductIdAndOptionGroupId(productId, fromOptionGroupId)
                .orElse(null);
            if (link == null) {
                continue;
            }
            affectedProductIds.add(productId.value());

            Integer preservedSort = link.getSort();
            linkSavePort.delete(link);

            if (!linkLoadPort.existsByProductIdAndOptionGroupId(productId, toOptionGroupId)) {
                linkSavePort.save(ProductOptionGroupLink.of(productId, toOptionGroupId, preservedSort));
            }
        }

        affectedProductIds.forEach(productId -> renumber(ProductId.of(productId)));
    }

    public ShopId findOwningShopId(ProductOptionGroupId optionGroupId) {
        return linkLoadPort.findAllByOptionGroupId(optionGroupId).stream()
            .map(ProductOptionGroupLink::getProductId)
            .map(productLoadPort::findActiveById)
            .filter(java.util.Optional::isPresent)
            .map(java.util.Optional::get)
            .map(Product::getShopId)
            .findFirst()
            .orElse(null);
    }

    private void validateSameShop(ProductId productId, ProductOptionGroupId optionGroupId) {
        Product product = productLoadPort.findActiveById(productId)
            .orElseThrow(() -> new ApplicationException(ApplicationErrorCode.PRODUCT_NOT_FOUND));

        ShopId owner = findOwningShopId(optionGroupId);
        if (owner != null && !owner.equals(product.getShopId())) {
            throw new ApplicationException(CeoErrorCode.PRODUCT_OPTION_GROUP_SHOP_MISMATCH);
        }
    }

    private void renumber(ProductId productId) {
        List<ProductOptionGroupLink> remaining = linkLoadPort.findAllByProductId(productId);
        for (int index = 0; index < remaining.size(); index++) {
            ProductOptionGroupLink link = remaining.get(index);
            link.changeSort(index);
            linkSavePort.save(link);
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
