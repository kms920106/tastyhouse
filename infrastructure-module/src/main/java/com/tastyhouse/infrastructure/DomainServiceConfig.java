package com.tastyhouse.infrastructure;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.tastyhouse.core.domain.bug.domain.repository.BugReportImageRepository;
import com.tastyhouse.core.domain.bug.domain.repository.BugReportRepository;
import com.tastyhouse.core.domain.bug.domain.service.BugReportRegistrationService;
import com.tastyhouse.core.domain.coupon.domain.repository.CouponRepository;
import com.tastyhouse.core.domain.coupon.domain.repository.MemberCouponRepository;
import com.tastyhouse.core.domain.coupon.domain.service.CouponIssueService;
import com.tastyhouse.core.domain.faq.domain.repository.FaqCategoryRepository;
import com.tastyhouse.core.domain.member.domain.repository.MemberRepository;
import com.tastyhouse.core.domain.member.domain.repository.MemberWithdrawalRepository;
import com.tastyhouse.core.domain.member.follow.domain.repository.MemberFollowRepository;
import com.tastyhouse.core.domain.member.referral.domain.repository.MemberReferralRepository;
import com.tastyhouse.core.domain.order.domain.repository.OrderProductOptionRepository;
import com.tastyhouse.core.domain.order.domain.repository.OrderProductRepository;
import com.tastyhouse.core.domain.order.domain.repository.OrderRepository;
import com.tastyhouse.core.domain.order.domain.service.OrderPlacementService;
import com.tastyhouse.core.domain.order.domain.service.OrderTransitionService;
import com.tastyhouse.core.domain.payment.domain.port.PgPaymentGateway;
import com.tastyhouse.core.domain.payment.domain.repository.PaymentRefundRepository;
import com.tastyhouse.core.domain.payment.domain.repository.PaymentRepository;
import com.tastyhouse.core.domain.payment.domain.repository.TossPaymentRecordRepository;
import com.tastyhouse.core.domain.payment.domain.service.PaymentCancellationService;
import com.tastyhouse.core.domain.payment.domain.service.PaymentConfirmationService;
import com.tastyhouse.core.domain.point.domain.repository.PointHistoryRepository;
import com.tastyhouse.core.domain.point.domain.repository.PointRepository;
import com.tastyhouse.core.domain.faq.domain.service.FaqCategoryDeletionPolicy;
import com.tastyhouse.core.domain.member.domain.service.MemberRegistrationService;
import com.tastyhouse.core.domain.member.domain.service.MemberWithdrawalService;
import com.tastyhouse.core.domain.member.follow.domain.service.MemberFollowService;
import com.tastyhouse.core.domain.member.referral.domain.service.ReferralRegistrationService;
import com.tastyhouse.core.domain.point.domain.service.PointLedgerService;
import com.tastyhouse.core.domain.policy.domain.repository.PolicyDocumentRepository;
import com.tastyhouse.core.domain.product.domain.port.ProductReviewStatisticsPort;
import com.tastyhouse.core.domain.product.domain.repository.ProductBbqRepository;
import com.tastyhouse.core.domain.product.domain.repository.ProductCategoryRepository;
import com.tastyhouse.core.domain.product.domain.repository.ProductImageRepository;
import com.tastyhouse.core.domain.product.domain.repository.ProductOptionGroupRepository;
import com.tastyhouse.core.domain.product.domain.repository.ProductOptionRepository;
import com.tastyhouse.core.domain.product.domain.repository.ProductRepository;
import com.tastyhouse.core.domain.product.domain.service.ProductRegistrationService;
import com.tastyhouse.core.domain.product.domain.service.ProductReviewStatsService;
import com.tastyhouse.core.domain.policy.domain.service.PolicyActivationService;
import com.tastyhouse.core.domain.rank.domain.port.MemberReviewCountPort;
import com.tastyhouse.core.domain.rank.domain.repository.MemberReviewRankRepository;
import com.tastyhouse.core.domain.rank.domain.service.RankSettlementService;
import com.tastyhouse.core.domain.reservation.domain.repository.ReservationRepository;
import com.tastyhouse.core.domain.review.domain.repository.ReviewImageRepository;
import com.tastyhouse.core.domain.review.domain.repository.ReviewLikeRepository;
import com.tastyhouse.core.domain.review.domain.repository.ReviewRepository;
import com.tastyhouse.core.domain.review.domain.repository.ReviewTagRepository;
import com.tastyhouse.core.domain.review.domain.service.ReviewLifecycleService;
import com.tastyhouse.core.domain.reservation.domain.repository.ReservationSlotRepository;
import com.tastyhouse.core.domain.reservation.domain.service.ReservationBookingService;
import com.tastyhouse.core.domain.shop.domain.repository.ProhibitedWordRepository;
import com.tastyhouse.core.domain.shop.domain.repository.ShopBookmarkRepository;
import com.tastyhouse.core.domain.shop.domain.repository.ShopConvenienceInfoRepository;
import com.tastyhouse.core.domain.shop.domain.repository.ShopDetailRepository;
import com.tastyhouse.core.domain.shop.domain.repository.ShopImageChangeRequestRepository;
import com.tastyhouse.core.domain.shop.domain.repository.ShopPhoneNumberRepository;
import com.tastyhouse.core.domain.shop.domain.repository.ShopRepository;
import com.tastyhouse.core.domain.shop.domain.repository.ShopSuspensionRepository;
import com.tastyhouse.core.domain.shop.domain.repository.ShopTemporaryClosureRepository;
import com.tastyhouse.core.domain.shop.domain.repository.StationRepository;
import com.tastyhouse.core.domain.shop.domain.repository.TagRepository;
import com.tastyhouse.core.domain.shop.domain.service.ProhibitedWordValidator;
import com.tastyhouse.core.domain.shop.domain.service.ShopBusinessHourService;
import com.tastyhouse.core.domain.shop.domain.service.ShopConvenienceInfoService;
import com.tastyhouse.core.domain.shop.domain.service.ShopImageApprovalService;
import com.tastyhouse.core.domain.shop.domain.service.ShopLifecycleService;
import com.tastyhouse.core.domain.shop.domain.service.ShopOperatingStatusCalculator;
import com.tastyhouse.core.domain.shop.domain.service.ShopOperatingStatusService;
import com.tastyhouse.core.domain.shop.domain.service.ShopPhoneNumberRegistryService;
import com.tastyhouse.core.domain.search.domain.repository.PopularKeywordRepository;
import com.tastyhouse.core.domain.search.domain.repository.SearchKeywordLogRepository;
import com.tastyhouse.core.domain.search.domain.service.PopularKeywordRefreshService;
import com.tastyhouse.core.domain.verification.domain.repository.EmailVerificationRepository;
import com.tastyhouse.core.domain.verification.domain.repository.PhoneVerificationRepository;
import com.tastyhouse.core.domain.verification.domain.service.EmailVerificationService;
import com.tastyhouse.core.domain.verification.domain.service.PhoneVerificationService;
import com.tastyhouse.core.shared.event.DomainEventPublisher;

/**
 * 하강된 도메인 서비스(POJO) 빈 등록 설정.
 *
 * <p>core-module → domain-module 전환 과정에서, 분류 (C) 불변식 오케스트레이션 / (D) 무상태 정책에
 * 해당하는 도메인 서비스는 {@code @Service}/{@code @Transactional} 없는 순수 POJO로 하강한다
 * (공통 지침의 패턴 1). Spring이 이들을 스캔할 수 없으므로, 각 도메인 작업에서 하강시킨 POJO를
 * 이 클래스의 {@code @Bean} 메서드로 등록한다.
 *
 * <p>초기에는 빈 클래스로 시작하며, 도메인 작업이 진행되며 {@code @Bean} 정의가 채워진다.
 */
@Configuration
public class DomainServiceConfig {

    /**
     * 버그 제보 등록 — 제보 애그리거트와 첨부 이미지 애그리거트를 한 트랜잭션에서 함께 저장하는 오케스트레이션.
     */
    @Bean
    public BugReportRegistrationService bugReportRegistrationService(
        BugReportRepository bugReportRepository,
        BugReportImageRepository bugReportImageRepository
    ) {
        return new BugReportRegistrationService(bugReportRepository, bugReportImageRepository);
    }

    /**
     * 쿠폰 발급·사용 — 쿠폰 원본 정책과 회원 보유분 두 애그리거트를 한 트랜잭션에서 함께 다루는 오케스트레이션.
     */
    @Bean
    public CouponIssueService couponIssueService(
        CouponRepository couponRepository,
        MemberCouponRepository memberCouponRepository,
        DomainEventPublisher domainEventPublisher
    ) {
        return new CouponIssueService(couponRepository, memberCouponRepository, domainEventPublisher);
    }

    /**
     * FAQ 카테고리 삭제 규칙 — 소속된 활성 FAQ 항목이 남아 있으면 삭제를 막는 크로스 애그리거트 규칙.
     */
    @Bean
    public FaqCategoryDeletionPolicy faqCategoryDeletionPolicy(FaqCategoryRepository faqCategoryRepository) {
        return new FaqCategoryDeletionPolicy(faqCategoryRepository);
    }

    /**
     * 정책 활성화 규칙 — 같은 유형의 기존 현행 정책을 함께 비활성화하는 크로스 인스턴스 불변식.
     */
    @Bean
    public PolicyActivationService policyActivationService(
        PolicyDocumentRepository policyDocumentRepository,
        DomainEventPublisher domainEventPublisher
    ) {
        return new PolicyActivationService(policyDocumentRepository, domainEventPublisher);
    }

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
        DomainEventPublisher domainEventPublisher
    ) {
        return new ProductRegistrationService(
            productRepository,
            productCategoryRepository,
            productOptionGroupRepository,
            productOptionRepository,
            productImageRepository,
            productBbqRepository,
            domainEventPublisher
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
     * 랭킹 확정 — 기준일의 기존 랭킹 일괄 삭제와 신규 순위 일괄 적재를 한 트랜잭션에서 함께 처리하는 오케스트레이션.
     */
    @Bean
    public RankSettlementService rankSettlementService(
        MemberReviewRankRepository memberReviewRankRepository,
        MemberReviewCountPort memberReviewCountPort
    ) {
        return new RankSettlementService(memberReviewRankRepository, memberReviewCountPort);
    }

    /**
     * 예약 예약/취소 — 예약 애그리거트의 생성·상태전이와 슬롯 정원 차감·반납을 한 트랜잭션에서 함께
     * 처리하는 오케스트레이션. 슬롯 정원 차감은 낙관적 락으로 보호되며, 충돌 시 재시도는 트랜잭션 경계
     * 바깥(web-api {@code ReservationCommandService})이 담당한다.
     */
    @Bean
    public ReservationBookingService reservationBookingService(
        ReservationRepository reservationRepository,
        ReservationSlotRepository reservationSlotRepository,
        ShopRepository shopRepository,
        MemberRepository memberRepository
    ) {
        return new ReservationBookingService(
            reservationRepository,
            reservationSlotRepository,
            shopRepository,
            memberRepository
        );
    }

    /**
     * 리뷰 생애주기 — 리뷰 본문과 첨부 이미지·태그를 한 트랜잭션에서 함께 저장·정리하고, 좋아요 토글과
     * 통계 갱신 이벤트 발행을 함께 처리하는 오케스트레이션.
     */
    @Bean
    public ReviewLifecycleService reviewLifecycleService(
        ReviewRepository reviewRepository,
        ReviewImageRepository reviewImageRepository,
        ReviewTagRepository reviewTagRepository,
        ReviewLikeRepository reviewLikeRepository,
        TagRepository tagRepository,
        DomainEventPublisher domainEventPublisher
    ) {
        return new ReviewLifecycleService(
            reviewRepository,
            reviewImageRepository,
            reviewTagRepository,
            reviewLikeRepository,
            tagRepository,
            domainEventPublisher
        );
    }

    /**
     * 인기 검색어 갱신 규칙 — 기존 목록 전체를 읽어 신규 여부를 판정하고 통째로 교체하는 크로스 인스턴스 오케스트레이션.
     */
    @Bean
    public PopularKeywordRefreshService popularKeywordRefreshService(
        SearchKeywordLogRepository searchKeywordLogRepository,
        PopularKeywordRepository popularKeywordRepository
    ) {
        return new PopularKeywordRefreshService(searchKeywordLogRepository, popularKeywordRepository);
    }

    /**
     * 이메일 인증 발급·검증 규칙 — 같은 이메일의 기존 미완료 인증을 함께 만료시키는 크로스 인스턴스 불변식.
     */
    @Bean
    public EmailVerificationService emailVerificationService(
        MemberRepository memberRepository,
        EmailVerificationRepository emailVerificationRepository,
        DomainEventPublisher domainEventPublisher
    ) {
        return new EmailVerificationService(memberRepository, emailVerificationRepository, domainEventPublisher);
    }

    /**
     * 회원 등록 — 회원 애그리거트를 저장하고, 추천인이 지정되면 추천 관계까지 함께 만드는 오케스트레이션.
     */
    @Bean
    public MemberRegistrationService memberRegistrationService(
        MemberRepository memberRepository,
        ReferralRegistrationService referralRegistrationService,
        DomainEventPublisher domainEventPublisher
    ) {
        return new MemberRegistrationService(memberRepository, referralRegistrationService, domainEventPublisher);
    }

    /**
     * 회원 탈퇴 — 회원 상태 전이와 탈퇴 사유 기록을 한 트랜잭션에서 함께 처리하는 오케스트레이션.
     */
    @Bean
    public MemberWithdrawalService memberWithdrawalService(
        MemberRepository memberRepository,
        MemberWithdrawalRepository memberWithdrawalRepository,
        DomainEventPublisher domainEventPublisher
    ) {
        return new MemberWithdrawalService(memberRepository, memberWithdrawalRepository, domainEventPublisher);
    }

    /**
     * 팔로우 등록·해제 — 팔로우 대상 회원의 존재를 확인해야 하는 크로스 애그리거트 규칙.
     */
    @Bean
    public MemberFollowService memberFollowService(
        MemberFollowRepository memberFollowRepository,
        MemberRepository memberRepository
    ) {
        return new MemberFollowService(memberFollowRepository, memberRepository);
    }

    /**
     * 추천인 등록 — 추천 관계 생성과 추천인·피추천인 양쪽 포인트 보상 적립을 함께 처리하는 오케스트레이션.
     */
    @Bean
    public ReferralRegistrationService referralRegistrationService(
        MemberReferralRepository memberReferralRepository,
        PointRepository pointRepository,
        PointHistoryRepository pointHistoryRepository,
        DomainEventPublisher domainEventPublisher
    ) {
        return new ReferralRegistrationService(
            memberReferralRepository,
            pointRepository,
            pointHistoryRepository,
            domainEventPublisher
        );
    }

    /**
     * 주문 접수 — 주문 헤더·상품 라인·라인 옵션 세 애그리거트를 한 트랜잭션에서 함께 만들고, 금액 계산과
     * 쿠폰 사용·포인트 차감까지 원자로 묶는 오케스트레이션.
     */
    @Bean
    public OrderPlacementService orderPlacementService(
        OrderRepository orderRepository,
        OrderProductRepository orderProductRepository,
        OrderProductOptionRepository orderProductOptionRepository,
        ShopRepository shopRepository,
        MemberRepository memberRepository,
        ProductRepository productRepository,
        ProductOptionGroupRepository productOptionGroupRepository,
        ProductOptionRepository productOptionRepository,
        ProductImageRepository productImageRepository,
        CouponIssueService couponIssueService,
        PointLedgerService pointLedgerService,
        DomainEventPublisher domainEventPublisher
    ) {
        return new OrderPlacementService(
            orderRepository,
            orderProductRepository,
            orderProductOptionRepository,
            shopRepository,
            memberRepository,
            productRepository,
            productOptionGroupRepository,
            productOptionRepository,
            productImageRepository,
            couponIssueService,
            pointLedgerService,
            domainEventPublisher
        );
    }

    /**
     * 주문 상태전이 — 결제·포인트 연쇄의 진입점. 주문 로드·전이·저장을 원자로 묶어, 트리거 액터
     * (회원·관리자·결제 콜백)가 여러 개여도 전이 규칙이 갈리지 않게 한다.
     */
    @Bean
    public OrderTransitionService orderTransitionService(OrderRepository orderRepository) {
        return new OrderTransitionService(orderRepository);
    }

    /**
     * 결제 개시·승인 — 결제 상태 전이와 주문 확정 전이를 한 트랜잭션에서 원자로 묶는 오케스트레이션.
     * 승인 경로(PG 콜백·토스 승인·현장결제 완료)가 여러 개여도 "결제 완료와 주문 확정은 항상 함께"라는
     * 규칙이 이 한 곳에만 존재한다. 주문 전이는 {@link OrderTransitionService}에 위임한다.
     */
    @Bean
    public PaymentConfirmationService paymentConfirmationService(
        PaymentRepository paymentRepository,
        TossPaymentRecordRepository tossPaymentRecordRepository,
        PgPaymentGateway pgPaymentGateway,
        OrderTransitionService orderTransitionService,
        DomainEventPublisher domainEventPublisher
    ) {
        return new PaymentConfirmationService(
            paymentRepository,
            tossPaymentRecordRepository,
            pgPaymentGateway,
            orderTransitionService,
            domainEventPublisher
        );
    }

    /**
     * 결제 취소·환불 — 결제 취소 전이와 주문 취소 전이를 한 트랜잭션에서 원자로 묶고, PG 취소 요청과
     * 포인트 원복(이벤트 경유)까지 함께 조율하는 오케스트레이션.
     */
    @Bean
    public PaymentCancellationService paymentCancellationService(
        PaymentRepository paymentRepository,
        PaymentRefundRepository paymentRefundRepository,
        PgPaymentGateway pgPaymentGateway,
        OrderTransitionService orderTransitionService,
        DomainEventPublisher domainEventPublisher
    ) {
        return new PaymentCancellationService(
            paymentRepository,
            paymentRefundRepository,
            pgPaymentGateway,
            orderTransitionService,
            domainEventPublisher
        );
    }

    /**
     * 포인트 원장 — 잔액 변경과 변동 이력 기록을 한 트랜잭션에서 함께 처리하는 오케스트레이션.
     */
    @Bean
    public PointLedgerService pointLedgerService(
        PointRepository pointRepository,
        PointHistoryRepository pointHistoryRepository,
        DomainEventPublisher domainEventPublisher
    ) {
        return new PointLedgerService(pointRepository, pointHistoryRepository, domainEventPublisher);
    }

    /**
     * 휴대폰 인증 발급·검증 규칙 — 같은 번호의 기존 미완료 인증을 함께 만료시키는 크로스 인스턴스 불변식.
     */
    @Bean
    public PhoneVerificationService phoneVerificationService(
        PhoneVerificationRepository phoneVerificationRepository,
        DomainEventPublisher domainEventPublisher
    ) {
        return new PhoneVerificationService(phoneVerificationRepository, domainEventPublisher);
    }

    /**
     * 금칙어 검수 정책 — 점주 입력 텍스트(가게소개·찾아오는길)에 액터 무관하게 적용되는 무상태 정책.
     */
    @Bean
    public ProhibitedWordValidator prohibitedWordValidator(ProhibitedWordRepository prohibitedWordRepository) {
        return new ProhibitedWordValidator(prohibitedWordRepository);
    }

    /**
     * 가게 영업 상태 계산기 — 리포지토리에 의존하지 않는 순수 판정 로직.
     */
    @Bean
    public ShopOperatingStatusCalculator shopOperatingStatusCalculator() {
        return new ShopOperatingStatusCalculator();
    }

    /**
     * 가게 영업 상태 판정 — 가게·영업시간·휴게시간·정기휴무·임시휴무·임시중지 여섯 애그리거트를 읽어
     * 계산기에 위임하는 오케스트레이션.
     */
    @Bean
    public ShopOperatingStatusService shopOperatingStatusService(
        ShopRepository shopRepository,
        ShopDetailRepository shopDetailRepository,
        ShopTemporaryClosureRepository shopTemporaryClosureRepository,
        ShopSuspensionRepository shopSuspensionRepository,
        ShopOperatingStatusCalculator shopOperatingStatusCalculator
    ) {
        return new ShopOperatingStatusService(
            shopRepository,
            shopDetailRepository,
            shopTemporaryClosureRepository,
            shopSuspensionRepository,
            shopOperatingStatusCalculator
        );
    }

    /**
     * 가게 이미지 변경 승인 워크플로 — 요청 승인과 가게 이미지 반영을 한 트랜잭션에서 함께 처리하는
     * 원자 연산(요청자 ceo·검수자 admin 양쪽이 공유하는 액터 무관 규칙).
     */
    @Bean
    public ShopImageApprovalService shopImageApprovalService(
        ShopImageChangeRequestRepository shopImageChangeRequestRepository,
        ShopRepository shopRepository
    ) {
        return new ShopImageApprovalService(shopImageChangeRequestRepository, shopRepository);
    }

    /**
     * 가게 전화번호 목록 불변식 — 대표번호와 가게 애그리거트의 대표 전화번호를 항상 함께 갱신한다.
     */
    @Bean
    public ShopPhoneNumberRegistryService shopPhoneNumberRegistryService(
        ShopPhoneNumberRepository shopPhoneNumberRepository,
        ShopRepository shopRepository
    ) {
        return new ShopPhoneNumberRegistryService(shopPhoneNumberRepository, shopRepository);
    }

    /**
     * 가게 영업시간·휴게시간·정기휴무 규격 불변식 — 휴게시간이 같은 요일 영업시간 범위 안인지 등을 검증한다.
     */
    @Bean
    public ShopBusinessHourService shopBusinessHourService(ShopDetailRepository shopDetailRepository) {
        return new ShopBusinessHourService(shopDetailRepository);
    }

    /**
     * 가게 생애주기 불변식 — 역 존재 확인·노출정지 차단(진행 중 이미지 요청)·가게소개 검수를 담당한다.
     */
    @Bean
    public ShopLifecycleService shopLifecycleService(
        ShopRepository shopRepository,
        ShopDetailRepository shopDetailRepository,
        ShopBookmarkRepository shopBookmarkRepository,
        StationRepository stationRepository,
        ShopImageApprovalService shopImageApprovalService,
        ProhibitedWordValidator prohibitedWordValidator
    ) {
        return new ShopLifecycleService(
            shopRepository,
            shopDetailRepository,
            shopBookmarkRepository,
            stationRepository,
            shopImageApprovalService,
            prohibitedWordValidator
        );
    }

    /**
     * 가게 편의정보 불변식 — 찾아오는길 금칙어 검수와 표시 위치 반경(1km) 검증을 담당한다.
     */
    @Bean
    public ShopConvenienceInfoService shopConvenienceInfoService(
        ShopConvenienceInfoRepository shopConvenienceInfoRepository,
        ShopRepository shopRepository,
        ProhibitedWordValidator prohibitedWordValidator
    ) {
        return new ShopConvenienceInfoService(shopConvenienceInfoRepository, shopRepository, prohibitedWordValidator);
    }
}
