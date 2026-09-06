package com.tastyhouse.application.productsoldout.service;

import com.tastyhouse.application.shared.marker.BatchApp;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.domain.product.model.Product;
import com.tastyhouse.domain.product.model.ProductCommonOption;
import com.tastyhouse.domain.product.model.ProductOption;
import com.tastyhouse.domain.product.repository.ProductCommonOptionRepository;
import com.tastyhouse.domain.product.repository.ProductOptionRepository;
import com.tastyhouse.domain.product.repository.ProductRepository;

@Component
@BatchApp
public class ProductSoldOutReleaseExecutor {

    private static final Logger log = LoggerFactory.getLogger(ProductSoldOutReleaseExecutor.class);

    private final ProductRepository productRepository;
    private final ProductOptionRepository productOptionRepository;
    private final ProductCommonOptionRepository productCommonOptionRepository;

    public ProductSoldOutReleaseExecutor(
        ProductRepository productRepository,
        ProductOptionRepository productOptionRepository,
        ProductCommonOptionRepository productCommonOptionRepository
    ) {
        this.productRepository = productRepository;
        this.productOptionRepository = productOptionRepository;
        this.productCommonOptionRepository = productCommonOptionRepository;
    }

    @Transactional
    public boolean releaseProduct(Product product) {
        try {
            product.releaseSoldOut();
            productRepository.save(product);
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
            productOptionRepository.save(option);
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
            productCommonOptionRepository.save(option);
            return true;
        } catch (Exception e) {
            log.error("공통 옵션 품절 자동해제 실패: commonOptionId={}, soldOutUntil={}",
                option.getId(), option.getSoldOutUntil(), e);
            return false;
        }
    }
}
