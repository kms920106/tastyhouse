package com.tastyhouse.application.productsoldout.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.domain.product.model.Product;
import com.tastyhouse.domain.product.model.ProductCommonOption;
import com.tastyhouse.domain.product.model.ProductOption;
import com.tastyhouse.application.product.port.out.write.ProductCommonOptionSavePort;
import com.tastyhouse.application.product.port.out.write.ProductOptionSavePort;
import com.tastyhouse.application.product.port.out.write.ProductSavePort;

@Component
public class ProductSoldOutReleaseExecutor {

    private static final Logger log = LoggerFactory.getLogger(ProductSoldOutReleaseExecutor.class);

    private final ProductSavePort productSavePort;
    private final ProductOptionSavePort productOptionSavePort;
    private final ProductCommonOptionSavePort productCommonOptionSavePort;

    public ProductSoldOutReleaseExecutor(
        ProductSavePort productSavePort,
        ProductOptionSavePort productOptionSavePort,
        ProductCommonOptionSavePort productCommonOptionSavePort
    ) {
        this.productSavePort = productSavePort;
        this.productOptionSavePort = productOptionSavePort;
        this.productCommonOptionSavePort = productCommonOptionSavePort;
    }

    @Transactional
    public boolean releaseProduct(Product product) {
        try {
            product.releaseSoldOut();
            productSavePort.save(product);
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
            productOptionSavePort.save(option);
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
            productCommonOptionSavePort.save(option);
            return true;
        } catch (Exception e) {
            log.error("공통 옵션 품절 자동해제 실패: commonOptionId={}, soldOutUntil={}",
                option.getId(), option.getSoldOutUntil(), e);
            return false;
        }
    }
}
