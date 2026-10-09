package com.tastyhouse.application.product.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.domain.product.model.CupDepositPolicy;
import com.tastyhouse.domain.product.model.ProductOptionGroupType;
import com.tastyhouse.application.product.port.in.ProductOptionsQueryUseCase;
import com.tastyhouse.application.product.port.out.ProductOptionQueryPort;
import com.tastyhouse.application.product.port.out.ProductOptionsResult;

@Service
@Transactional(readOnly = true)
class ProductOptionsQueryService implements ProductOptionsQueryUseCase {

    private final ProductOptionQueryPort productOptionQueryPort;
    private final CupDepositPolicy cupDepositPolicy;
    private final ProductDetailReader productDetailReader;

    public ProductOptionsQueryService(
        ProductOptionQueryPort productOptionQueryPort,
        CupDepositPolicy cupDepositPolicy,
        ProductDetailReader productDetailReader
    ) {
        this.productOptionQueryPort = productOptionQueryPort;
        this.cupDepositPolicy = cupDepositPolicy;
        this.productDetailReader = productDetailReader;
    }

    @Override
    public ProductOptionsResult findProductOptions(Long productId) {
        productDetailReader.read(productId);
        return ProductOptionDepositAmounts.of(
            productOptionQueryPort.findProductOptions(productId, ProductOptionGroupType.NORMAL.name()),
            cupDepositPolicy
        );
    }
}
