package com.tastyhouse.application.productsoldout.service;

import java.time.LocalDateTime;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.tastyhouse.domain.product.model.Product;
import com.tastyhouse.domain.product.model.ProductCommonOption;
import com.tastyhouse.domain.product.model.ProductOption;
import com.tastyhouse.application.product.port.out.write.ProductCommonOptionPersistencePort;
import com.tastyhouse.application.product.port.out.write.ProductOptionPersistencePort;
import com.tastyhouse.application.product.port.out.write.ProductPersistencePort;
import com.tastyhouse.application.productsoldout.port.in.ReleaseExpiredSoldOutUseCase;

@Service
public class ProductSoldOutReleaseSchedulerService implements ReleaseExpiredSoldOutUseCase {

    private static final Logger log = LoggerFactory.getLogger(ProductSoldOutReleaseSchedulerService.class);

    private final ProductPersistencePort productPersistencePort;
    private final ProductOptionPersistencePort productOptionPersistencePort;
    private final ProductCommonOptionPersistencePort productCommonOptionPersistencePort;
    private final ProductSoldOutReleaseExecutor productSoldOutReleaseExecutor;

    public ProductSoldOutReleaseSchedulerService(
        ProductPersistencePort productPersistencePort,
        ProductOptionPersistencePort productOptionPersistencePort,
        ProductCommonOptionPersistencePort productCommonOptionPersistencePort,
        ProductSoldOutReleaseExecutor productSoldOutReleaseExecutor
    ) {
        this.productPersistencePort = productPersistencePort;
        this.productOptionPersistencePort = productOptionPersistencePort;
        this.productCommonOptionPersistencePort = productCommonOptionPersistencePort;
        this.productSoldOutReleaseExecutor = productSoldOutReleaseExecutor;
    }

    @Override
    public void releaseExpiredSoldOut() {
        LocalDateTime now = LocalDateTime.now();

        List<Product> products = productPersistencePort.findAllSoldOutExpiredBefore(now);
        List<ProductOption> options = productOptionPersistencePort.findAllSoldOutExpiredBefore(now);
        List<ProductCommonOption> commonOptions = productCommonOptionPersistencePort.findAllSoldOutExpiredBefore(now);

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
