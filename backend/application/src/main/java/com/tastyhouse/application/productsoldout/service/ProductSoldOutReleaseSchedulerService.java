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

/**
 * 품절 자동해제 배치 서비스.
 *
 * <p>대상 조회와 건별 집계만 담당한다. 해제 자체는 도메인 모델의 {@code releaseSoldOut()}이 소유하고,
 * 건별 트랜잭션 경계는 {@link ProductSoldOutReleaseExecutor}가 갖는다.
 *
 * <p><b>이 클래스에는 {@code @Transactional}이 없다</b> — 여기에 걸면 전체가 한 트랜잭션이 되어 한 건의
 * 실패가 전체를 되돌리므로 건별 격리라는 요구사항과 어긋난다.
 *
 * <p>실패 요약은 예외가 아니라 <b>로그</b>로 남긴다 — 예외를 던지면 스케줄러가 잡아 삼키므로 성공 건수까지
 * 함께 잃는다. 건별 실패는 executor가 이미 개별적으로 로깅한다.
 */
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

    /**
     * 자동해제 시각이 지난 품절 메뉴·옵션·공통옵션을 모두 판매중으로 되돌린다.
     *
     * <p>세 종류를 각각 조회해 처리하며, 조회는 종류별로 한 번만 하고 전이는 executor를 통해 건별
     * 독립 트랜잭션으로 처리한다.
     */
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
