package com.tastyhouse.application.productsoldout.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.domain.product.model.Product;
import com.tastyhouse.domain.product.model.ProductCommonOption;
import com.tastyhouse.domain.product.model.ProductOption;
import com.tastyhouse.application.product.port.out.write.ProductCommonOptionPersistencePort;
import com.tastyhouse.application.product.port.out.write.ProductOptionPersistencePort;
import com.tastyhouse.application.product.port.out.write.ProductPersistencePort;
import com.tastyhouse.application.shared.marker.BatchApp;

@Component
@BatchApp
public class ProductSoldOutReleaseExecutor {

    private static final Logger log = LoggerFactory.getLogger(ProductSoldOutReleaseExecutor.class);

    private final ProductPersistencePort productPersistencePort;
    private final ProductOptionPersistencePort productOptionPersistencePort;
    private final ProductCommonOptionPersistencePort productCommonOptionPersistencePort;

    public ProductSoldOutReleaseExecutor(
        ProductPersistencePort productPersistencePort,
        ProductOptionPersistencePort productOptionPersistencePort,
        ProductCommonOptionPersistencePort productCommonOptionPersistencePort
    ) {
        this.productPersistencePort = productPersistencePort;
        this.productOptionPersistencePort = productOptionPersistencePort;
        this.productCommonOptionPersistencePort = productCommonOptionPersistencePort;
    }

    @Transactional
    public boolean releaseProduct(Product product) {
        try {
            product.releaseSoldOut();
            productPersistencePort.save(product);
            return true;
        } catch (Exception e) {
            log.error("메뉴 품절 자동해제 실패: productId={}, soldOutUntil={}",
                product.getId(), product.getSoldOutUntil(), e);
            return false;
        }
    }

    @Transactional
    public boolean releaseOption(ProductOption option) {
        try {
            option.releaseSoldOut();
            productOptionPersistencePort.save(option);
            return true;
        } catch (Exception e) {
            log.error("옵션 품절 자동해제 실패: optionId={}, soldOutUntil={}",
                option.getId(), option.getSoldOutUntil(), e);
            return false;
        }
    }

    @Transactional
    public boolean releaseCommonOption(ProductCommonOption option) {
        try {
            option.releaseSoldOut();
            productCommonOptionPersistencePort.save(option);
            return true;
        } catch (Exception e) {
            log.error("공통 옵션 품절 자동해제 실패: commonOptionId={}, soldOutUntil={}",
                option.getId(), option.getSoldOutUntil(), e);
            return false;
        }
    }
}
