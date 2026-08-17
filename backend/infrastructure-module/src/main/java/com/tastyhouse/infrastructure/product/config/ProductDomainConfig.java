package com.tastyhouse.infrastructure.product.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.tastyhouse.domain.product.port.ProductReviewStatisticsPort;
import com.tastyhouse.domain.product.repository.ProductBbqRepository;
import com.tastyhouse.domain.product.repository.ProductCategoryRepository;
import com.tastyhouse.domain.product.repository.ProductCommonOptionGroupRepository;
import com.tastyhouse.domain.product.repository.ProductCommonOptionRepository;
import com.tastyhouse.domain.product.repository.ProductImageRepository;
import com.tastyhouse.domain.product.repository.ProductOptionGroupRepository;
import com.tastyhouse.domain.product.repository.ProductOptionRepository;
import com.tastyhouse.domain.product.repository.ProductRepository;
import com.tastyhouse.domain.product.service.OrderProductValidationService;
import com.tastyhouse.domain.product.service.ProductAvailabilityService;
import com.tastyhouse.domain.product.service.ProductRegistrationService;
import com.tastyhouse.domain.product.service.ProductReviewStatsService;

/**
 * product 컨텍스트의 도메인 서비스(POJO) 빈 등록 설정.
 *
 * <p>도메인 서비스는 {@code @Service} 없는 순수 POJO라 Spring이 스캔할 수 없으므로,
 * 이 컨텍스트에 새 POJO 도메인 서비스를 추가하면 여기에 {@code @Bean}을 추가한다.
 */
@Configuration(proxyBeanMethods = false)
public class ProductDomainConfig {

    /**
     * 상품 등록·구성 — 상품 본체와 카테고리·옵션그룹·옵션·이미지·BBQ 매핑을 한 트랜잭션에서 함께
     * 저장하는 오케스트레이션(admin CRUD·batch 크롤링이 공유하는 액터 무관 규칙).
     */
    @Bean
    public ProductRegistrationService productRegistrationService(
        ProductRepository productRepository,
        ProductCategoryRepository productCategoryRepository,
        ProductOptionGroupRepository productOptionGroupRepository,
        ProductOptionRepository productOptionRepository,
        ProductImageRepository productImageRepository,
        ProductBbqRepository productBbqRepository
    ) {
        return new ProductRegistrationService(
            productRepository,
            productCategoryRepository,
            productOptionGroupRepository,
            productOptionRepository,
            productImageRepository,
            productBbqRepository
        );
    }

    /**
     * 상품 리뷰 통계 갱신 — 리뷰 집계(출력 포트)를 읽어 상품 애그리거트의 평점·리뷰 수에 반영하는
     * 크로스 애그리거트 오케스트레이션.
     */
    @Bean
    public ProductReviewStatsService productReviewStatsService(
        ProductRepository productRepository,
        ProductReviewStatisticsPort productReviewStatisticsPort
    ) {
        return new ProductReviewStatsService(productRepository, productReviewStatisticsPort);
    }

    /**
     * 주문 라인의 상품·옵션 검증 — 상품 존재·판매중지·옵션 존재를 판정하고 주문 라인에 박제할 스냅샷을
     * 돌려준다. 주문 접수가 product의 모델·리포지토리를 직접 쓰지 않도록 이 컨텍스트가 소유한다.
     */
    @Bean
    public OrderProductValidationService orderProductValidationService(
        ProductRepository productRepository,
        ProductOptionGroupRepository productOptionGroupRepository,
        ProductOptionRepository productOptionRepository,
        ProductImageRepository productImageRepository
    ) {
        return new OrderProductValidationService(
            productRepository,
            productOptionGroupRepository,
            productOptionRepository,
            productImageRepository
        );
    }

    /**
     * 메뉴·옵션 품절·숨김 전이와 부분실패 제약 — 노출 메뉴 ≥1 · 추천 메뉴 ≥1 · 옵션그룹별
     * {@code minSelect} 잔여 개수의 단일 소유자. 제약이 애그리거트 불변식이라 ceo/admin 두 모듈에
     * 흩어지지 않도록 도메인에 둔다.
     */
    @Bean
    public ProductAvailabilityService productAvailabilityService(
        ProductRepository productRepository,
        ProductOptionRepository productOptionRepository,
        ProductCommonOptionRepository productCommonOptionRepository,
        ProductOptionGroupRepository productOptionGroupRepository,
        ProductCommonOptionGroupRepository productCommonOptionGroupRepository
    ) {
        return new ProductAvailabilityService(
            productRepository,
            productOptionRepository,
            productCommonOptionRepository,
            productOptionGroupRepository,
            productCommonOptionGroupRepository
        );
    }
}
