package com.tastyhouse.application.product.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.domain.product.model.ProductPrice;
import com.tastyhouse.domain.product.vo.ProductId;
import com.tastyhouse.domain.shared.model.OrderMethod;
import com.tastyhouse.application.menureview.port.out.MenuReviewStatisticsQueryPort;
import com.tastyhouse.application.product.port.in.ProductDetailQueryUseCase;
import com.tastyhouse.application.product.port.out.ProductDetailResult;
import com.tastyhouse.application.product.port.out.ProductDetailView;
import com.tastyhouse.application.product.port.out.ProductPriceResult;
import com.tastyhouse.application.product.port.out.ProductPriceView;
import com.tastyhouse.application.product.port.out.ProductQueryPort;

@Service
@Transactional(readOnly = true)
class ProductDetailQueryService implements ProductDetailQueryUseCase {

    private final ProductQueryPort productQueryPort;
    private final MenuReviewStatisticsQueryPort menuReviewStatisticsQueryPort;
    private final ProductDetailReader productDetailReader;

    public ProductDetailQueryService(
        ProductQueryPort productQueryPort,
        MenuReviewStatisticsQueryPort menuReviewStatisticsQueryPort,
        ProductDetailReader productDetailReader
    ) {
        this.productQueryPort = productQueryPort;
        this.menuReviewStatisticsQueryPort = menuReviewStatisticsQueryPort;
        this.productDetailReader = productDetailReader;
    }

    @Override
    public ProductDetailView findProductById(Long productId, String orderMethod) {
        ProductDetailResult dto = productDetailReader.read(productId);
        Long menuReviewCount = menuReviewStatisticsQueryPort.countVisibleByProductId(productId);
        OrderMethod resolvedOrderMethod = OrderMethod.from(orderMethod);
        List<ProductPriceView> prices = productQueryPort.findProductPrices(productId).stream()
            .map(price -> toProductPriceView(price, resolvedOrderMethod))
            .toList();
        return new ProductDetailView(
            dto.id(),
            dto.name(),
            dto.description(),
            dto.originalPrice(),
            dto.discountPrice(),
            dto.discountRate(),
            dto.soldOut(),
            dto.weightText(),
            menuReviewCount != null ? menuReviewCount : 0L,
            prices
        );
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
}
