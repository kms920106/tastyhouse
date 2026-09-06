package com.tastyhouse.application.productsoldout.service;

import com.tastyhouse.application.shared.marker.BatchApp;
import com.tastyhouse.application.productsoldout.port.in.ReleaseExpiredSoldOutUseCase;
import java.time.LocalDateTime;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.tastyhouse.domain.product.model.Product;
import com.tastyhouse.domain.product.model.ProductCommonOption;
import com.tastyhouse.domain.product.model.ProductOption;
import com.tastyhouse.domain.product.repository.ProductCommonOptionRepository;
import com.tastyhouse.domain.product.repository.ProductOptionRepository;
import com.tastyhouse.domain.product.repository.ProductRepository;

@Service
@BatchApp
public class ProductSoldOutReleaseSchedulerService implements ReleaseExpiredSoldOutUseCase {

    private static final Logger log = LoggerFactory.getLogger(ProductSoldOutReleaseSchedulerService.class);

    private final ProductRepository productRepository;
    private final ProductOptionRepository productOptionRepository;
    private final ProductCommonOptionRepository productCommonOptionRepository;
    private final ProductSoldOutReleaseExecutor productSoldOutReleaseExecutor;

    public ProductSoldOutReleaseSchedulerService(
        ProductRepository productRepository,
        ProductOptionRepository productOptionRepository,
        ProductCommonOptionRepository productCommonOptionRepository,
        ProductSoldOutReleaseExecutor productSoldOutReleaseExecutor
    ) {
        this.productRepository = productRepository;
        this.productOptionRepository = productOptionRepository;
        this.productCommonOptionRepository = productCommonOptionRepository;
        this.productSoldOutReleaseExecutor = productSoldOutReleaseExecutor;
    }

    @Override
    public void releaseExpiredSoldOut() {
        LocalDateTime now = LocalDateTime.now();

        List<Product> products = productRepository.findAllSoldOutExpiredBefore(now);
        List<ProductOption> options = productOptionRepository.findAllSoldOutExpiredBefore(now);
        List<ProductCommonOption> commonOptions = productCommonOptionRepository.findAllSoldOutExpiredBefore(now);

        int total = products.size() + options.size() + commonOptions.size();
        if (total == 0) {
            log.info("품절 자동해제 대상 없음: now={}", now);
            return;
        }

        int succeeded = 0;
        int failed = 0;

        for (Product product : products) {
            if (productSoldOutReleaseExecutor.releaseProduct(product)) {
                succeeded++;
            } else {
                failed++;
            }
        }
        for (ProductOption option : options) {
            if (productSoldOutReleaseExecutor.releaseOption(option)) {
                succeeded++;
            } else {
                failed++;
            }
        }
        for (ProductCommonOption option : commonOptions) {
            if (productSoldOutReleaseExecutor.releaseCommonOption(option)) {
                succeeded++;
            } else {
                failed++;
            }
        }

        log.info("품절 자동해제 완료: now={}, 메뉴 {} 건, 옵션 {} 건, 공통옵션 {} 건, 성공 {} 건, 실패 {} 건",
            now, products.size(), options.size(), commonOptions.size(), succeeded, failed);
    }
}
