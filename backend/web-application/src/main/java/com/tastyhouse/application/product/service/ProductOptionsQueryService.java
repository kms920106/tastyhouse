package com.tastyhouse.application.product.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.domain.product.model.CupDepositPolicy;
import com.tastyhouse.domain.product.model.ProductOptionGroupType;
import com.tastyhouse.application.product.port.in.ProductOptionsQueryUseCase;
import com.tastyhouse.application.product.port.out.ProductOptionsResult;
import com.tastyhouse.application.product.port.out.ProductQueryPort;

@Service
@Transactional(readOnly = true)
class ProductOptionsQueryService implements ProductOptionsQueryUseCase {

    private final ProductQueryPort productQueryPort;
    private final CupDepositPolicy cupDepositPolicy;
    private final ProductDetailReader productDetailReader;

    public ProductOptionsQueryService(
        ProductQueryPort productQueryPort,
        CupDepositPolicy cupDepositPolicy,
        ProductDetailReader productDetailReader
    ) {
        this.productQueryPort = productQueryPort;
        this.cupDepositPolicy = cupDepositPolicy;
        this.productDetailReader = productDetailReader;
    }

    @Override
    public ProductOptionsResult findProductOptions(Long productId) {
        productDetailReader.read(productId);
        return ProductOptionDepositAmounts.of(
            productQueryPort.findProductOptions(productId, ProductOptionGroupType.NORMAL.name()),
            cupDepositPolicy
        );
    }
}
