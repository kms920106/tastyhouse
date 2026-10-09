package com.tastyhouse.application.product.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.domain.product.model.CupDepositPolicy;
import com.tastyhouse.domain.product.model.ProductOptionGroupType;
import com.tastyhouse.application.product.port.in.ProductOptionManagementListQueryUseCase;
import com.tastyhouse.application.product.port.out.ProductOptionQueryPort;
import com.tastyhouse.application.product.port.out.ProductOptionsResult;

@Service
@Transactional(readOnly = true)
class ProductOptionManagementListQueryService implements ProductOptionManagementListQueryUseCase {

    private final ProductOptionQueryPort productOptionQueryPort;
    private final CupDepositPolicy cupDepositPolicy;

    public ProductOptionManagementListQueryService(
        ProductOptionQueryPort productOptionQueryPort,
        CupDepositPolicy cupDepositPolicy
    ) {
        this.productOptionQueryPort = productOptionQueryPort;
        this.cupDepositPolicy = cupDepositPolicy;
    }

    @Override
    public ProductOptionsResult getProductOptions(Long id) {
        return ProductOptionDepositAmounts.of(
            productOptionQueryPort.findProductOptions(id, ProductOptionGroupType.NORMAL.name()),
            cupDepositPolicy
        );
    }
}
