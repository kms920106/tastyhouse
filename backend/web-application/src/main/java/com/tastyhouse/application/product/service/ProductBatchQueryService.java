package com.tastyhouse.application.product.service;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.domain.product.model.CupDepositPolicy;
import com.tastyhouse.domain.product.model.ProductPrice;
import com.tastyhouse.domain.product.vo.ProductId;
import com.tastyhouse.domain.shared.model.OrderMethod;
import com.tastyhouse.application.product.port.in.ProductBatchQuery;
import com.tastyhouse.application.product.port.in.ProductBatchQueryUseCase;
import com.tastyhouse.application.product.port.out.ProductBatchItem;
import com.tastyhouse.application.product.port.out.ProductBatchItemView;
import com.tastyhouse.application.product.port.out.ProductBatchQueryPort;
import com.tastyhouse.application.product.port.out.ProductBatchResult;
import com.tastyhouse.application.product.port.out.ProductPriceResult;
import com.tastyhouse.application.product.port.out.ProductPriceView;
import com.tastyhouse.application.product.port.out.ProductQueryPort;

@Service
@Transactional(readOnly = true)
class ProductBatchQueryService implements ProductBatchQueryUseCase {

    private final ProductBatchQueryPort productBatchQueryPort;
    private final ProductQueryPort productQueryPort;
    private final CupDepositPolicy cupDepositPolicy;

    public ProductBatchQueryService(
        ProductBatchQueryPort productBatchQueryPort,
        ProductQueryPort productQueryPort,
        CupDepositPolicy cupDepositPolicy
    ) {
        this.productBatchQueryPort = productBatchQueryPort;
        this.productQueryPort = productQueryPort;
        this.cupDepositPolicy = cupDepositPolicy;
    }

    @Override
    public List<ProductBatchItemView> findProductsBatch(ProductBatchQuery query) {
        List<ProductBatchItem> items = query.items().stream()
            .map(item -> ProductBatchItem.of(item.productId(), item.optionId()))
            .toList();

        OrderMethod orderMethod = OrderMethod.from(query.orderMethod());
        List<ProductBatchResult> results =
            ProductOptionDepositAmounts.of(productBatchQueryPort.findProductsBatch(items), cupDepositPolicy);
        Map<Long, List<ProductPriceView>> pricesByProductId =
            findBatchPricesByProductId(results, orderMethod);

        return results.stream()
            .map(result -> toProductBatchItemView(
                result,
                pricesByProductId.getOrDefault(result.id(), List.of())
            ))
            .toList();
    }

    private Map<Long, List<ProductPriceView>> findBatchPricesByProductId(
        List<ProductBatchResult> results,
        OrderMethod orderMethod
    ) {
        List<Long> productIds = results.stream()
            .filter(ProductBatchResult::available)
            .map(ProductBatchResult::id)
            .distinct()
            .toList();

        return productQueryPort.findProductPricesByProductIds(productIds).stream()
            .collect(Collectors.groupingBy(
                ProductPriceResult::productId,
                LinkedHashMap::new,
                Collectors.mapping(price -> toProductPriceView(price, orderMethod), Collectors.toList())
            ));
    }

    private ProductPriceView toProductPriceView(ProductPriceResult dto, OrderMethod orderMethod) {
        ProductPrice price = ProductPrice.reconstitute(
            dto.id(),
            ProductId.of(dto.productId()),
            dto.priceName(),
            dto.deliveryPrice(),
            dto.storePrice(),
            dto.pickupPrice(),
            dto.sort(),
            dto.pickupPriceSetAt(),
            null,
            null
        );
        return new ProductPriceView(dto.id(), price.getPriceName(), price.resolvePrice(orderMethod));
    }

    private ProductBatchItemView toProductBatchItemView(
        ProductBatchResult result,
        List<ProductPriceView> prices
    ) {
        return new ProductBatchItemView(
            result.id(),
            result.available(),
            result.name(),
            result.imageUrl(),
            result.originalPrice(),
            result.discountPrice(),
            result.options(),
            prices
        );
    }
}
