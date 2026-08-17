package com.tastyhouse.batch.scheduler;

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

/**
 * 품절 자동해제 <b>1건</b>의 트랜잭션 경계를 담당하는 얇은 빈.
 *
 * <p><b>별도 빈으로 분리한 이유(핵심)</b>: 건별 격리는 각 건이 독립 트랜잭션이어야 성립하는데, 같은 빈의
 * 메서드를 호출하면 Spring 프록시를 거치지 않아(self-invocation) {@code @Transactional}이 적용되지
 * 않는다. 그러면 한 건이 실패했을 때 롤백 경계가 없어 앞서 성공한 건들까지 함께 말려 들어간다.
 * {@code ReviewBlindExpirationExecutor}가 같은 이유로 분리된 선례다.
 *
 * <p><b>해제는 {@code releaseSoldOut()}만 경유한다</b> — {@code soldOut = false}와
 * {@code soldOutUntil = null}을 함께 정리해야 다음 주기에 같은 행이 또 잡히지 않는다.
 */
@Component
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

    /**
     * 메뉴 한 건을 독립 트랜잭션에서 품절 해제한다.
     *
     * <p>실패해도 예외를 밖으로 던지지 않아 다음 건 처리가 이어진다 — 한 건의 실패가 전체 잡을 멈추지
     * 않게 하는 것이 이 배치의 요구사항이다.
     *
     * @return 성공 여부
     */
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

    /**
     * 일반 옵션 한 건을 독립 트랜잭션에서 품절 해제한다.
     *
     * @return 성공 여부
     */
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

    /**
     * 공통 옵션 한 건을 독립 트랜잭션에서 품절 해제한다.
     *
     * @return 성공 여부
     */
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
