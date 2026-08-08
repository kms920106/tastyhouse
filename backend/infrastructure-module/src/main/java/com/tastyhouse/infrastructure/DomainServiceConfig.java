package com.tastyhouse.infrastructure;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.tastyhouse.domain.bug.repository.BugReportImageRepository;
import com.tastyhouse.domain.bug.repository.BugReportRepository;
import com.tastyhouse.domain.bug.service.BugReportRegistrationService;
import com.tastyhouse.domain.coupon.repository.CouponRepository;
import com.tastyhouse.domain.coupon.repository.MemberCouponRepository;
import com.tastyhouse.domain.coupon.service.CouponIssueService;
import com.tastyhouse.domain.faq.repository.FaqCategoryRepository;
import com.tastyhouse.domain.faq.service.FaqCategoryDeletionPolicy;
import com.tastyhouse.domain.file.port.FileStoragePort;
import com.tastyhouse.domain.file.repository.UploadedFileRepository;
import com.tastyhouse.domain.file.service.FileUploadService;
import com.tastyhouse.domain.holiday.repository.PublicHolidayRepository;
import com.tastyhouse.domain.holiday.service.PublicHolidayCalendar;
import com.tastyhouse.domain.mail.port.MailSender;
import com.tastyhouse.domain.mail.repository.MailVerificationRepository;
import com.tastyhouse.domain.mail.service.MailVerificationService;
import com.tastyhouse.domain.member.follow.domain.repository.MemberFollowRepository;
import com.tastyhouse.domain.member.follow.domain.service.MemberFollowService;
import com.tastyhouse.domain.member.referral.domain.repository.MemberReferralRepository;
import com.tastyhouse.domain.member.referral.domain.service.ReferralRegistrationService;
import com.tastyhouse.domain.member.repository.MemberDeliveryAddressRepository;
import com.tastyhouse.domain.member.repository.MemberRepository;
import com.tastyhouse.domain.member.repository.MemberWithdrawalRepository;
import com.tastyhouse.domain.member.service.MemberDeliveryAddressService;
import com.tastyhouse.domain.member.service.MemberRegistrationService;
import com.tastyhouse.domain.member.service.MemberWithdrawalService;
import com.tastyhouse.domain.order.repository.OrderProductOptionRepository;
import com.tastyhouse.domain.order.repository.OrderProductRepository;
import com.tastyhouse.domain.order.repository.OrderRepository;
import com.tastyhouse.domain.order.service.OrderPlacementService;
import com.tastyhouse.domain.order.service.OrderTransitionService;
import com.tastyhouse.domain.payment.repository.PaymentRefundRepository;
import com.tastyhouse.domain.payment.repository.PaymentRepository;
import com.tastyhouse.domain.payment.repository.TossPaymentRecordRepository;
import com.tastyhouse.domain.payment.service.PaymentCancellationService;
import com.tastyhouse.domain.payment.service.PaymentConfirmationService;
import com.tastyhouse.domain.point.repository.PointHistoryRepository;
import com.tastyhouse.domain.point.repository.PointRepository;
import com.tastyhouse.domain.point.service.PointLedgerService;
import com.tastyhouse.domain.policy.repository.PolicyDocumentRepository;
import com.tastyhouse.domain.policy.service.PolicyActivationService;
import com.tastyhouse.domain.product.port.ProductReviewStatisticsPort;
import com.tastyhouse.domain.product.repository.ProductBbqRepository;
import com.tastyhouse.domain.product.repository.ProductCategoryRepository;
import com.tastyhouse.domain.product.repository.ProductImageRepository;
import com.tastyhouse.domain.product.repository.ProductOptionGroupRepository;
import com.tastyhouse.domain.product.repository.ProductOptionRepository;
import com.tastyhouse.domain.product.repository.ProductRepository;
import com.tastyhouse.domain.product.service.ProductRegistrationService;
import com.tastyhouse.domain.product.service.ProductReviewStatsService;
import com.tastyhouse.domain.rank.port.MemberReviewCountPort;
import com.tastyhouse.domain.rank.repository.MemberReviewRankRepository;
import com.tastyhouse.domain.rank.service.RankSettlementService;
import com.tastyhouse.domain.region.repository.AdminDongRepository;
import com.tastyhouse.domain.reservation.repository.ReservationRepository;
import com.tastyhouse.domain.reservation.repository.ReservationSlotRepository;
import com.tastyhouse.domain.reservation.service.ReservationBookingService;
import com.tastyhouse.domain.review.repository.ReviewImageRepository;
import com.tastyhouse.domain.review.repository.ReviewLikeRepository;
import com.tastyhouse.domain.review.repository.ReviewRepository;
import com.tastyhouse.domain.review.repository.ReviewTagRepository;
import com.tastyhouse.domain.review.service.ReviewLifecycleService;
import com.tastyhouse.domain.search.port.KeywordCountPort;
import com.tastyhouse.domain.search.repository.PopularKeywordRepository;
import com.tastyhouse.domain.search.repository.SearchKeywordLogRepository;
import com.tastyhouse.domain.search.service.PopularKeywordRefreshService;
import com.tastyhouse.domain.shared.event.DomainEventPublisher;
import com.tastyhouse.domain.shop.repository.ProhibitedWordRepository;
import com.tastyhouse.domain.shop.repository.ShopBookmarkRepository;
import com.tastyhouse.domain.shop.repository.ShopConvenienceInfoRepository;
import com.tastyhouse.domain.shop.repository.ShopDeliveryAreaRepository;
import com.tastyhouse.domain.shop.repository.ShopDeliveryTipRegionLookup;
import com.tastyhouse.domain.shop.repository.ShopDeliveryTipRepository;
import com.tastyhouse.domain.shop.repository.ShopDetailRepository;
import com.tastyhouse.domain.shop.repository.ShopImageChangeRequestRepository;
import com.tastyhouse.domain.shop.repository.ShopPhoneNumberRepository;
import com.tastyhouse.domain.shop.repository.ShopRepository;
import com.tastyhouse.domain.shop.repository.ShopSuspensionRepository;
import com.tastyhouse.domain.shop.repository.ShopTemporaryClosureRepository;
import com.tastyhouse.domain.shop.repository.StationRepository;
import com.tastyhouse.domain.shop.repository.TagRepository;
import com.tastyhouse.domain.shop.service.ProhibitedWordValidator;
import com.tastyhouse.domain.shop.service.ScheduledOrderSlotCalculator;
import com.tastyhouse.domain.shop.service.ScheduledOrderSlotService;
import com.tastyhouse.domain.shop.service.ShopBusinessHourService;
import com.tastyhouse.domain.shop.service.ShopConvenienceInfoService;
import com.tastyhouse.domain.shop.service.ShopDeliveryAreaService;
import com.tastyhouse.domain.shop.service.ShopDeliveryTipCalculator;
import com.tastyhouse.domain.shop.service.ShopDeliveryTipService;
import com.tastyhouse.domain.shop.service.ShopImageApprovalService;
import com.tastyhouse.domain.shop.service.ShopLifecycleService;
import com.tastyhouse.domain.shop.service.ShopOperatingStatusCalculator;
import com.tastyhouse.domain.shop.service.ShopOperatingStatusService;
import com.tastyhouse.domain.shop.service.ShopPhoneNumberRegistryService;
import com.tastyhouse.domain.sms.port.SmsSender;
import com.tastyhouse.domain.sms.repository.SmsVerificationRepository;
import com.tastyhouse.domain.sms.service.SmsVerificationService;
import com.tastyhouse.infrastructure.shop.persistence.CachingProhibitedWordRepository;

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
     * 파일 업로드 규칙 — 규격 검증·스토리지 저장·메타 저장·이벤트 발행을 한 트랜잭션에서 원자로 묶는
     * 액터 무관 연산(web·admin·ceo 업로드와 batch 외부 이미지 다운로드가 공유).
     */
    @Bean
    public FileUploadService fileUploadService(
        UploadedFileRepository uploadedFileRepository,
        FileStoragePort fileStoragePort,
        DomainEventPublisher domainEventPublisher
    ) {
        return new FileUploadService(uploadedFileRepository, fileStoragePort, domainEventPublisher);
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
        KeywordCountPort keywordCountPort,
        PopularKeywordRepository popularKeywordRepository
    ) {
        return new PopularKeywordRefreshService(searchKeywordLogRepository, keywordCountPort, popularKeywordRepository);
    }

    /**
     * 메일 인증 발급·검증 규칙 — 같은 이메일의 기존 미완료 인증을 함께 만료시키는 크로스 인스턴스 불변식.
     *
     * <p>{@link MailSender}는 external-api의 어댑터가 구현한다. 발급이 발송까지 원자적으로 수행하도록
     * 도메인 서비스에 주입한다(발송 누락 방지 — 상세는 {@code MailVerificationService} Javadoc).
     */
    @Bean
    public MailVerificationService mailVerificationService(
        MemberRepository memberRepository,
        MailVerificationRepository mailVerificationRepository,
        MailSender mailSender,
        DomainEventPublisher domainEventPublisher
    ) {
        return new MailVerificationService(memberRepository, mailVerificationRepository, mailSender, domainEventPublisher);
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
        ShopDeliveryTipRepository shopDeliveryTipRepository,
        ShopDeliveryAreaRepository shopDeliveryAreaRepository,
        MemberDeliveryAddressRepository memberDeliveryAddressRepository,
        ShopDeliveryTipCalculator shopDeliveryTipCalculator,
        PublicHolidayCalendar publicHolidayCalendar,
        ScheduledOrderSlotService scheduledOrderSlotService
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
            shopDeliveryTipRepository,
            shopDeliveryAreaRepository,
            memberDeliveryAddressRepository,
            shopDeliveryTipCalculator,
            publicHolidayCalendar,
            scheduledOrderSlotService
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
        OrderTransitionService orderTransitionService,
        DomainEventPublisher domainEventPublisher
    ) {
        return new PaymentConfirmationService(
            paymentRepository,
            tossPaymentRecordRepository,
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
        OrderTransitionService orderTransitionService,
        DomainEventPublisher domainEventPublisher
    ) {
        return new PaymentCancellationService(
            paymentRepository,
            paymentRefundRepository,
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
     * SMS 인증 발급·검증 규칙 — 같은 번호의 기존 미완료 인증을 함께 만료시키는 크로스 인스턴스 불변식.
     *
     * <p>{@link SmsSender}는 external-api의 어댑터가 구현한다. 발급이 발송까지 원자적으로 수행하도록
     * 도메인 서비스에 주입한다(발송 누락 방지 — 상세는 {@code SmsVerificationService} Javadoc).
     */
    @Bean
    public SmsVerificationService smsVerificationService(
        SmsVerificationRepository smsVerificationRepository,
        SmsSender smsSender,
        DomainEventPublisher domainEventPublisher
    ) {
        return new SmsVerificationService(smsVerificationRepository, smsSender, domainEventPublisher);
    }

    /**
     * 금칙어 검수 정책 — 점주 입력 텍스트(가게소개·찾아오는길)에 액터 무관하게 적용되는 무상태 정책.
     */
    @Bean
    public ProhibitedWordValidator prohibitedWordValidator(ProhibitedWordRepository prohibitedWordRepository) {
        // 검증기는 텍스트 검증마다 findAll()을 호출하므로, 전량 로드가 매번 DB로 나가지 않도록 캐싱
        // 데코레이터로 감싼 포트를 주입한다. 금칙어는 SQL 시드 read-only 데이터라 정합성 리스크가 낮고,
        // 캐싱을 어댑터 쪽에 두어 domain-module의 순수 POJO 검증기는 그대로 둔다.
        return new ProhibitedWordValidator(new CachingProhibitedWordRepository(prohibitedWordRepository));
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
     * 예약주문 슬롯 계산기 — 리포지토리 주입 0개의 순수 판정 로직.
     * 영업 판정을 새로 짜지 않고 {@link ShopOperatingStatusCalculator}에 미래 시각을 넘겨 재사용한다.
     */
    @Bean
    public ScheduledOrderSlotCalculator scheduledOrderSlotCalculator(
        ShopOperatingStatusCalculator shopOperatingStatusCalculator
    ) {
        return new ScheduledOrderSlotCalculator(shopOperatingStatusCalculator);
    }

    /**
     * 예약주문 슬롯 조회·확정 — 가게·영업시간·휴게시간·정기휴무·임시휴무·임시중지 여섯 애그리거트를 읽어
     * 계산기에 위임하는 오케스트레이션. 주문 접수({@link OrderPlacementService})는 이 서비스 하나만
     * 주입받아 클라이언트가 보낸 수령 시각을 재계산·대조한다.
     */
    @Bean
    public ScheduledOrderSlotService scheduledOrderSlotService(
        ShopRepository shopRepository,
        ShopDetailRepository shopDetailRepository,
        ShopTemporaryClosureRepository shopTemporaryClosureRepository,
        ShopSuspensionRepository shopSuspensionRepository,
        ScheduledOrderSlotCalculator scheduledOrderSlotCalculator
    ) {
        return new ScheduledOrderSlotService(
            shopRepository,
            shopDetailRepository,
            shopTemporaryClosureRepository,
            shopSuspensionRepository,
            scheduledOrderSlotCalculator
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
     * 가게 배달팁 컬렉션 불변식 — 구간 개수·정렬·단조성, 거리별↔지역별 상호 배타, 지역별 팁의 행정동이
     * 배달가능지역에 속하는지, 같은 요일 시간대 겹침을 검증한다. 컬렉션 3종은 replace-all로 교체한다.
     */
    @Bean
    public ShopDeliveryTipService shopDeliveryTipService(
        ShopDeliveryTipRepository shopDeliveryTipRepository,
        ShopDeliveryAreaRepository shopDeliveryAreaRepository,
        AdminDongRepository adminDongRepository
    ) {
        return new ShopDeliveryTipService(shopDeliveryTipRepository, shopDeliveryAreaRepository, adminDongRepository);
    }

    /**
     * 배달팁 산출 — 리포지토리 주입 0개·인스턴스 상태 0개의 순수 계산기.
     * 좌표→거리, 날짜→공휴일 변환은 호출부가 끝내고 이미 해석된 값으로 넘긴다.
     */
    @Bean
    public ShopDeliveryTipCalculator shopDeliveryTipCalculator() {
        return new ShopDeliveryTipCalculator();
    }

    /**
     * 가게 배달가능지역 불변식 — 행정동 존재·중복 등록을 검증하고, 지역별 배달팁이 참조 중인 지역의
     * 삭제를 차단한다(지역별 팁이 배달불가 지역을 가리키는 상태 방지).
     */
    @Bean
    public ShopDeliveryAreaService shopDeliveryAreaService(
        ShopDeliveryAreaRepository shopDeliveryAreaRepository,
        AdminDongRepository adminDongRepository,
        ShopDeliveryTipRegionLookup shopDeliveryTipRegionLookup
    ) {
        return new ShopDeliveryAreaService(
            shopDeliveryAreaRepository, adminDongRepository, shopDeliveryTipRegionLookup
        );
    }

    /**
     * 법정 공휴일 판정 — 배달팁 공휴일 부과 여부를 캘린더 테이블로 답한다.
     * 영업상태 판정({@code ShopOperatingStatusService})에는 아직 연결하지 않는다(파급 격리).
     */
    @Bean
    public PublicHolidayCalendar publicHolidayCalendar(PublicHolidayRepository publicHolidayRepository) {
        return new PublicHolidayCalendar(publicHolidayRepository);
    }

    /**
     * 회원 배달 주소록 불변식 — 기본 배송지 유일성, 회원당 10건 한도, 소유권 검증,
     * 주소 문자열의 행정동 매칭을 담당한다.
     */
    @Bean
    public MemberDeliveryAddressService memberDeliveryAddressService(
        MemberDeliveryAddressRepository memberDeliveryAddressRepository,
        AdminDongRepository adminDongRepository
    ) {
        return new MemberDeliveryAddressService(memberDeliveryAddressRepository, adminDongRepository);
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
