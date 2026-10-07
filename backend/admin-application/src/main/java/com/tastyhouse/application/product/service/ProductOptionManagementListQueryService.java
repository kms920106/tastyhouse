package com.tastyhouse.application.product.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.domain.product.model.CupDepositPolicy;
import com.tastyhouse.domain.product.model.ProductOptionGroupType;
import com.tastyhouse.application.product.port.in.ProductOptionManagementListQueryUseCase;
import com.tastyhouse.application.product.port.out.ProductManagementQueryPort;
import com.tastyhouse.application.product.port.out.ProductOptionsResult;

@Service
@Transactional(readOnly = true)
class ProductOptionManagementListQueryService implements ProductOptionManagementListQueryUseCase {

    private final ProductManagementQueryPort productManagementQueryPort;
    private final CupDepositPolicy cupDepositPolicy;

    public ProductOptionManagementListQueryService(
        ProductManagementQueryPort productManagementQueryPort,
        CupDepositPolicy cupDepositPolicy
    ) {
        this.productManagementQueryPort = productManagementQueryPort;
        this.cupDepositPolicy = cupDepositPolicy;
    }

    @Override
    public ProductOptionsResult getProductOptions(Long id) {
        return ProductOptionDepositAmounts.of(
            productManagementQueryPort.findProductOptions(id, ProductOptionGroupType.NORMAL.name()),
            cupDepositPolicy
        );
    }
}
