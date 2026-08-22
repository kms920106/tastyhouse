package com.tastyhouse.infrastructure.product.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.tastyhouse.domain.product.port.ProductReviewStatisticsPort;
import com.tastyhouse.domain.product.repository.ProductBbqRepository;
import com.tastyhouse.domain.product.repository.ProductCategoryRepository;
import com.tastyhouse.domain.product.repository.ProductCommonOptionGroupLinkRepository;
import com.tastyhouse.domain.product.repository.ProductExposureHourRepository;
import com.tastyhouse.domain.product.repository.ProductImageChangeRequestRepository;
import com.tastyhouse.domain.product.repository.ProductRepresentativeRequestRepository;
import com.tastyhouse.domain.product.repository.ProductVegetarianRequestRepository;
import com.tastyhouse.domain.product.repository.ProductCommonOptionGroupRepository;
import com.tastyhouse.domain.product.repository.ProductCommonOptionRepository;
import com.tastyhouse.domain.product.repository.ProductImageRepository;
import com.tastyhouse.domain.product.repository.ProductOptionGroupLinkRepository;
import com.tastyhouse.domain.product.repository.ProductOptionGroupRepository;
import com.tastyhouse.domain.product.repository.ProductOptionRepository;
import com.tastyhouse.domain.product.repository.ProductRepository;
import com.tastyhouse.domain.product.service.CupDepositPolicy;
import com.tastyhouse.domain.product.service.OrderProductValidationService;
import com.tastyhouse.domain.product.service.ProductAvailabilityService;
import com.tastyhouse.domain.product.service.ProductDeletionService;
import com.tastyhouse.domain.product.service.ProductExposureCalculator;
import com.tastyhouse.domain.product.service.ProductExposureService;
import com.tastyhouse.domain.product.service.ProductImageApprovalService;
import com.tastyhouse.domain.product.service.ProductRepresentativeApprovalService;
import com.tastyhouse.domain.product.service.ProductVegetarianApprovalService;
import com.tastyhouse.domain.product.repository.ProductOptionGroupMergeHistoryRepository;
import com.tastyhouse.domain.product.service.ProductOptionGroupLinkService;
import com.tastyhouse.domain.product.service.ProductOptionGroupMergeService;
import com.tastyhouse.domain.product.service.ProductSortService;
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
        ProductBbqRepository productBbqRepository,
        ProductOptionGroupLinkRepository productOptionGroupLinkRepository
    ) {
        return new ProductRegistrationService(
            productRepository,
            productCategoryRepository,
            productOptionGroupRepository,
            productOptionRepository,
            productImageRepository,
            productBbqRepository,
            productOptionGroupLinkRepository
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
        ProductImageRepository productImageRepository,
        ProductOptionGroupLinkRepository productOptionGroupLinkRepository,
        ProductExposureHourRepository productExposureHourRepository,
        ProductExposureCalculator productExposureCalculator,
        CupDepositPolicy cupDepositPolicy
    ) {
        return new OrderProductValidationService(
            productRepository,
            productOptionGroupRepository,
            productOptionRepository,
            productImageRepository,
            productOptionGroupLinkRepository,
            productExposureHourRepository,
            productExposureCalculator,
            cupDepositPolicy
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
        ProductCommonOptionGroupRepository productCommonOptionGroupRepository,
        ProductOptionGroupLinkRepository productOptionGroupLinkRepository,
        ProductCommonOptionGroupLinkRepository productCommonOptionGroupLinkRepository
    ) {
        return new ProductAvailabilityService(
            productRepository,
            productOptionRepository,
            productCommonOptionRepository,
            productOptionGroupRepository,
            productCommonOptionGroupRepository,
            productOptionGroupLinkRepository,
            productCommonOptionGroupLinkRepository
        );
    }

    /**
     * 메뉴 일괄 삭제(소프트)와 부분실패 제약 — 숨김과 같은 불변식을 적용한다. 숨김만 막고 삭제를
     * 열어두면 점주가 삭제로 우회해 빈 메뉴판을 만들 수 있다.
     */
    @Bean
    public ProductDeletionService productDeletionService(ProductRepository productRepository) {
        return new ProductDeletionService(productRepository);
    }

    /**
     * 메뉴그룹·메뉴 정렬과 그룹 이동. sort 값을 클라이언트에서 받지 않고 순서 있는 id 배열만 받아
     * 서버가 0..N-1로 정규화한다.
     */
    @Bean
    public ProductSortService productSortService(
        ProductRepository productRepository,
        ProductCategoryRepository productCategoryRepository
    ) {
        return new ProductSortService(productRepository, productCategoryRepository);
    }

    /**
     * 메뉴 ↔ 옵션그룹 연결(N:M). <b>옵션그룹은 단일 가게에만 속한다</b>는 불변식을 강제해,
     * 소유권 판정에서 ANY/ALL 구분이 사라지게 한다.
     */
    @Bean
    public ProductOptionGroupLinkService productOptionGroupLinkService(
        ProductOptionGroupLinkRepository productOptionGroupLinkRepository,
        ProductRepository productRepository
    ) {
        return new ProductOptionGroupLinkService(productOptionGroupLinkRepository, productRepository);
    }

    /**
     * 옵션그룹 합치기. 다중 애그리거트(그룹·옵션·링크·이력)에 걸친 불변식이라 도메인이 제자리다.
     *
     * <p>링크 재배치는 {@link ProductOptionGroupLinkService#relink}에 위임한다 — UNIQUE 충돌 처리와
     * sort 불변식이 그 클래스 소유로 남아야 {@code renumber}를 공개하지 않아도 된다.
     */
    @Bean
    public ProductOptionGroupMergeService productOptionGroupMergeService(
        ProductOptionGroupRepository productOptionGroupRepository,
        ProductOptionRepository productOptionRepository,
        ProductOptionGroupLinkRepository productOptionGroupLinkRepository,
        ProductOptionGroupLinkService productOptionGroupLinkService,
        ProductOptionGroupMergeHistoryRepository productOptionGroupMergeHistoryRepository
    ) {
        return new ProductOptionGroupMergeService(
            productOptionGroupRepository,
            productOptionRepository,
            productOptionGroupLinkRepository,
            productOptionGroupLinkService,
            productOptionGroupMergeHistoryRepository
        );
    }

    /**
     * 메뉴 노출 판정 계산기 — 리포지토리도 시계도 갖지 않는 순수 함수라 의존이 없다.
     */
    @Bean
    public ProductExposureCalculator productExposureCalculator() {
        return new ProductExposureCalculator();
    }

    /**
     * 일회용컵 보증금 요율·계산. 리포지토리도 시계도 갖지 않는 순수 계산기라 의존이 없다.
     *
     * <p>빈으로 두는 이유는 요율을 <b>단 한 곳</b>에 두기 위함이다 — 점주 설정(ceo)·손님 메뉴판(web)·
     * 주문 금액 확정(order) 세 경로가 같은 인스턴스를 주입받아야 "화면 금액과 결제 금액이 다른" 사고가
     * 구조적으로 불가능해진다.
     */
    @Bean
    public CupDepositPolicy cupDepositPolicy() {
        return new CupDepositPolicy();
    }

    /**
     * 노출기간 설정의 조회·조립·저장. 요일·시간대는 replace-all로 교체하며, 요일 묶음과 개별 요일의
     * 혼용을 금지한다 — 그 조합을 저장할 수 없게 하면 SQL 술어와 계산기가 갈릴 여지가 없다.
     */
    @Bean
    public ProductExposureService productExposureService(
        ProductRepository productRepository,
        ProductExposureHourRepository productExposureHourRepository,
        ProductExposureCalculator productExposureCalculator
    ) {
        return new ProductExposureService(
            productRepository,
            productExposureHourRepository,
            productExposureCalculator
        );
    }

    /**
     * 메뉴 이미지 등록 승인 워크플로. 검수 대상은 "새 이미지의 내용"이므로 등록만 승인을 거치고
     * 순서 변경·삭제는 즉시 반영한다.
     */
    @Bean
    public ProductImageApprovalService productImageApprovalService(
        ProductImageChangeRequestRepository productImageChangeRequestRepository,
        ProductImageRepository productImageRepository,
        ProductRepository productRepository
    ) {
        return new ProductImageApprovalService(
            productImageChangeRequestRepository,
            productImageRepository,
            productRepository
        );
    }

    /**
     * 메뉴 채식 설정 승인 워크플로. 점주는 재료를 근거로 신청만 하고, 관리자 승인 시에만 반영된다 —
     * 채식 표기는 알레르기·신념과 직결돼 잘못된 표기의 대가가 크기 때문이다.
     */
    @Bean
    public ProductVegetarianApprovalService productVegetarianApprovalService(
        ProductVegetarianRequestRepository productVegetarianRequestRepository,
        ProductRepository productRepository
    ) {
        return new ProductVegetarianApprovalService(
            productVegetarianRequestRepository,
            productRepository
        );
    }

    /**
     * 사장님 추천(대표 메뉴) 승인 워크플로. <b>지정은 승인을 거치고 해제는 즉시 반영된다</b> —
     * 검수의 목적이 부적합한 메뉴의 상단 노출을 막는 데 있어 해제 방향에는 그 위험이 없다.
     *
     * <p>가게당 최대 6개·이미지 필수·최소 1개 유지 세 제약을 이 서비스가 단독으로 소유한다.
     * 세 번째 제약은 일괄 숨김({@link ProductAvailabilityService})이 이미 쓰는
     * {@code PRODUCT_LAST_REPRESENTATIVE_CANNOT_HIDE}를 재사용하므로, 두 경로가 같은 하한을 공유한다.
     */
    @Bean
    public ProductRepresentativeApprovalService productRepresentativeApprovalService(
        ProductRepresentativeRequestRepository productRepresentativeRequestRepository,
        ProductRepository productRepository,
        ProductImageRepository productImageRepository
    ) {
        return new ProductRepresentativeApprovalService(
            productRepresentativeRequestRepository,
            productRepository,
            productImageRepository
        );
    }
}
