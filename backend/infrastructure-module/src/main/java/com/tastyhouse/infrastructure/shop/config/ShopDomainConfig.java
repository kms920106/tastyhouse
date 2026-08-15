package com.tastyhouse.infrastructure.shop.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.tastyhouse.domain.ceo.repository.CeoRepository;
import com.tastyhouse.domain.region.repository.AdminDongRepository;
import com.tastyhouse.domain.review.repository.ReviewBlindRequestRepository;
import com.tastyhouse.domain.shop.repository.ProhibitedWordRepository;
import com.tastyhouse.domain.shop.repository.ShopBookmarkRepository;
import com.tastyhouse.domain.shop.repository.ShopCeoAssignmentHistoryRepository;
import com.tastyhouse.domain.shop.repository.ShopChangeHistoryRepository;
import com.tastyhouse.domain.shop.repository.ShopConvenienceInfoRepository;
import com.tastyhouse.domain.shop.repository.ShopDeliveryAreaAdjustmentRequestRepository;
import com.tastyhouse.domain.shop.repository.ShopDeliveryAreaPolygonRepository;
import com.tastyhouse.domain.shop.repository.ShopDeliveryAreaRepository;
import com.tastyhouse.domain.shop.repository.ShopDeliveryTipRegionLookup;
import com.tastyhouse.domain.shop.repository.ShopDeliveryTipRepository;
import com.tastyhouse.domain.shop.repository.ShopDetailRepository;
import com.tastyhouse.domain.shop.repository.ShopImageChangeRequestRepository;
import com.tastyhouse.domain.shop.repository.ShopNoticeRepository;
import com.tastyhouse.domain.shop.repository.ShopPhoneNumberRepository;
import com.tastyhouse.domain.shop.repository.ShopRepository;
import com.tastyhouse.domain.shop.repository.ShopRequestCommentRepository;
import com.tastyhouse.domain.shop.repository.ShopRequestIndexRepository;
import com.tastyhouse.domain.shop.repository.ShopRiderGuideRepository;
import com.tastyhouse.domain.shop.repository.ShopSuspensionRepository;
import com.tastyhouse.domain.shop.repository.ShopTemporaryClosureRepository;
import com.tastyhouse.domain.shop.repository.StationRepository;
import com.tastyhouse.domain.shop.service.ProhibitedWordValidator;
import com.tastyhouse.domain.shop.service.ScheduledOrderSlotCalculator;
import com.tastyhouse.domain.shop.service.ScheduledOrderSlotService;
import com.tastyhouse.domain.shop.service.ShopBusinessHourService;
import com.tastyhouse.domain.shop.service.ShopCeoAssignmentRecorder;
import com.tastyhouse.domain.shop.service.ShopCeoAssignmentService;
import com.tastyhouse.domain.shop.service.ShopChangeHistoryRecorder;
import com.tastyhouse.domain.shop.service.ShopConvenienceInfoService;
import com.tastyhouse.domain.shop.service.ShopDeliveryAreaAdjustmentService;
import com.tastyhouse.domain.shop.service.ShopDeliveryAreaPolygonService;
import com.tastyhouse.domain.shop.service.ShopDeliveryAreaRadiusService;
import com.tastyhouse.domain.shop.service.ShopDeliveryAreaService;
import com.tastyhouse.domain.shop.service.ShopDeliveryTipCalculator;
import com.tastyhouse.domain.shop.service.ShopDeliveryTipService;
import com.tastyhouse.domain.shop.service.ShopImageApprovalService;
import com.tastyhouse.domain.shop.service.ShopLifecycleService;
import com.tastyhouse.domain.shop.service.ShopNoticeExposureService;
import com.tastyhouse.domain.shop.service.ShopOperatingStatusCalculator;
import com.tastyhouse.domain.shop.service.ShopOperatingStatusService;
import com.tastyhouse.domain.shop.service.ShopOrderAvailabilityService;
import com.tastyhouse.domain.shop.service.ShopPhoneNumberRegistryService;
import com.tastyhouse.domain.shop.service.ShopRequestCancelService;
import com.tastyhouse.domain.shop.service.ShopRequestCommentService;
import com.tastyhouse.domain.shop.service.ShopRequestIndexRecorder;
import com.tastyhouse.domain.shop.service.ShopRiderGuideService;
import com.tastyhouse.domain.shop.service.ShopRiderGuideValidator;
import com.tastyhouse.infrastructure.shop.persistence.CachingProhibitedWordRepository;

/**
 * shop 컨텍스트의 도메인 서비스(POJO) 빈 등록 설정.
 *
 * <p>도메인 서비스는 {@code @Service} 없는 순수 POJO라 Spring이 스캔할 수 없으므로,
 * 이 컨텍스트에 새 POJO 도메인 서비스를 추가하면 여기에 {@code @Bean}을 추가한다.
 */
@Configuration(proxyBeanMethods = false)
public class ShopDomainConfig {

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
     * 점주 공지 앱 노출 불변식 — "가게당 노출 공지는 최대 1건". 이 공지를 켜면서 기존 노출 공지를 함께
     * 내리는 집합 연산이라 단일 애그리거트 연산이 아니므로 도메인 서비스가 소유한다.
     */
    @Bean
    public ShopNoticeExposureService shopNoticeExposureService(ShopNoticeRepository shopNoticeRepository) {
        return new ShopNoticeExposureService(shopNoticeRepository);
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
     * 주문 접수 게이트 — 영업상태·주문유형 배정·유형별 임시중지 검증. 주문 접수
     * ({@code OrderPlacementService})와 예약 생성({@code ReservationBookingService})이 같은 규칙을
     * 쓰도록 검증을 이 서비스 하나에 모았다.
     */
    @Bean
    public ShopOrderAvailabilityService shopOrderAvailabilityService(
        ShopOperatingStatusService shopOperatingStatusService,
        ShopDetailRepository shopDetailRepository
    ) {
        return new ShopOrderAvailabilityService(
            shopOperatingStatusService,
            shopDetailRepository
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
     * 계산기에 위임하는 오케스트레이션. 주문 접수({@code OrderPlacementService})는 이 서비스 하나만
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
        ShopRepository shopRepository,
        ShopChangeHistoryRecorder shopChangeHistoryRecorder,
        ShopRequestIndexRecorder shopRequestIndexRecorder
    ) {
        return new ShopImageApprovalService(
            shopImageChangeRequestRepository,
            shopRepository,
            shopChangeHistoryRecorder,
            shopRequestIndexRecorder
        );
    }

    /**
     * 가게 전화번호 목록 불변식 — 대표번호와 가게 애그리거트의 대표 전화번호를 항상 함께 갱신한다.
     */
    @Bean
    public ShopPhoneNumberRegistryService shopPhoneNumberRegistryService(
        ShopPhoneNumberRepository shopPhoneNumberRepository,
        ShopRepository shopRepository,
        ShopChangeHistoryRecorder shopChangeHistoryRecorder
    ) {
        return new ShopPhoneNumberRegistryService(
            shopPhoneNumberRepository,
            shopRepository,
            shopChangeHistoryRecorder
        );
    }

    /**
     * 가게 영업시간·휴게시간·정기휴무 규격 불변식 — 휴게시간이 같은 요일 영업시간 범위 안인지 등을 검증한다.
     */
    @Bean
    public ShopBusinessHourService shopBusinessHourService(
        ShopDetailRepository shopDetailRepository,
        ShopChangeHistoryRecorder shopChangeHistoryRecorder
    ) {
        return new ShopBusinessHourService(shopDetailRepository, shopChangeHistoryRecorder);
    }

    /**
     * 가게 배달팁 컬렉션 불변식 — 구간 개수·정렬·단조성, 거리별↔지역별 상호 배타, 지역별 팁의 행정동이
     * 배달가능지역에 속하는지, 같은 요일 시간대 겹침을 검증한다. 컬렉션 3종은 replace-all로 교체한다.
     */
    @Bean
    public ShopDeliveryTipService shopDeliveryTipService(
        ShopDeliveryTipRepository shopDeliveryTipRepository,
        ShopDeliveryAreaRepository shopDeliveryAreaRepository,
        AdminDongRepository adminDongRepository,
        ShopChangeHistoryRecorder shopChangeHistoryRecorder
    ) {
        return new ShopDeliveryTipService(
            shopDeliveryTipRepository,
            shopDeliveryAreaRepository,
            adminDongRepository,
            shopChangeHistoryRecorder
        );
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
        ShopDeliveryTipRegionLookup shopDeliveryTipRegionLookup,
        ShopChangeHistoryRecorder shopChangeHistoryRecorder
    ) {
        return new ShopDeliveryAreaService(
            shopDeliveryAreaRepository, adminDongRepository, shopDeliveryTipRegionLookup, shopChangeHistoryRecorder
        );
    }

    /**
     * 배달지역 도형 저장·삭제 — 도형 원본과 그것을 환산한 행정동 집합이 같은 트랜잭션에서 항상 일치하도록
     * 순서와 검증을 한 곳에 모은다. 환산을 비동기로 미루면 "저장은 됐는데 주문은 거절되는" 창이 생기고,
     * 그 사이 등록 건수가 0이 되면 주문 접수의 지역 검사가 통째로 비활성된다.
     */
    @Bean
    public ShopDeliveryAreaPolygonService shopDeliveryAreaPolygonService(
        ShopDeliveryAreaPolygonRepository shopDeliveryAreaPolygonRepository,
        ShopDeliveryAreaRepository shopDeliveryAreaRepository,
        AdminDongRepository adminDongRepository,
        ShopDeliveryTipRegionLookup shopDeliveryTipRegionLookup,
        ShopChangeHistoryRecorder shopChangeHistoryRecorder
    ) {
        return new ShopDeliveryAreaPolygonService(
            shopDeliveryAreaPolygonRepository,
            shopDeliveryAreaRepository,
            adminDongRepository,
            shopDeliveryTipRegionLookup,
            shopChangeHistoryRecorder
        );
    }

    /**
     * 반경 일괄 적용 — 후보 행정동을 write 포트로 읽는다(명령 경로가 infra query DAO를 주입하면 CQRS 교차
     * 주입 금지 규칙에 걸린다). 거리 판정은 원 근사 다각형이 아니라 하버사인 직선거리로 한다.
     */
    @Bean
    public ShopDeliveryAreaRadiusService shopDeliveryAreaRadiusService(
        ShopDeliveryAreaRepository shopDeliveryAreaRepository,
        AdminDongRepository adminDongRepository,
        ShopDeliveryAreaService shopDeliveryAreaService,
        ShopChangeHistoryRecorder shopChangeHistoryRecorder
    ) {
        return new ShopDeliveryAreaRadiusService(
            shopDeliveryAreaRepository, adminDongRepository, shopDeliveryAreaService, shopChangeHistoryRecorder
        );
    }

    /**
     * 프랜차이즈 배달지역 조정 신청 불변식 — 같은 가게에 진행 중(PENDING·IN_PROGRESS) 신청이 있으면 새
     * 접수를 막고(행 하나만 보고는 판정할 수 없는 집합 차원 규칙), 상태 전이 후 명시적으로 저장한다.
     * 신청 접수는 ceo, 검수는 admin이 수행하는 액터 공유 규칙이라 도메인 계층에 하나만 둔다.
     */
    @Bean
    public ShopDeliveryAreaAdjustmentService shopDeliveryAreaAdjustmentService(
        ShopDeliveryAreaAdjustmentRequestRepository shopDeliveryAreaAdjustmentRequestRepository,
        ShopChangeHistoryRecorder shopChangeHistoryRecorder,
        ShopRequestIndexRecorder shopRequestIndexRecorder
    ) {
        return new ShopDeliveryAreaAdjustmentService(
            shopDeliveryAreaAdjustmentRequestRepository,
            shopChangeHistoryRecorder,
            shopRequestIndexRecorder
        );
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
        ProhibitedWordValidator prohibitedWordValidator,
        ShopChangeHistoryRecorder shopChangeHistoryRecorder,
        ShopCeoAssignmentRecorder shopCeoAssignmentRecorder
    ) {
        return new ShopLifecycleService(
            shopRepository,
            shopDetailRepository,
            shopBookmarkRepository,
            stationRepository,
            shopImageApprovalService,
            prohibitedWordValidator,
            shopChangeHistoryRecorder,
            shopCeoAssignmentRecorder
        );
    }

    /**
     * 가게 편의정보 불변식 — 찾아오는길 금칙어 검수와 표시 위치 반경(1km) 검증, 그리고 편의정보·편의시설
     * 변경이력 기록을 담당한다.
     */
    @Bean
    public ShopConvenienceInfoService shopConvenienceInfoService(
        ShopConvenienceInfoRepository shopConvenienceInfoRepository,
        ShopRepository shopRepository,
        ShopDetailRepository shopDetailRepository,
        ProhibitedWordValidator prohibitedWordValidator,
        ShopChangeHistoryRecorder shopChangeHistoryRecorder
    ) {
        return new ShopConvenienceInfoService(
            shopConvenienceInfoRepository,
            shopRepository,
            shopDetailRepository,
            prohibitedWordValidator,
            shopChangeHistoryRecorder
        );
    }

    /**
     * 라이더 안내 문구 등록 기준 검증 정책 — 배민 가이드의 "작성 불가 3유형"(금칙어·가게 실주소 재기재·
     * 배차 특정)을 액터 무관하게 적용하는 무상태 정책.
     */
    @Bean
    public ShopRiderGuideValidator shopRiderGuideValidator(ProhibitedWordValidator prohibitedWordValidator) {
        return new ShopRiderGuideValidator(prohibitedWordValidator);
    }

    /**
     * 라이더 안내 불변식 — 폐업 가게 차단·문구 등록 기준 검증·변경 이력 기록을 원자적으로 묶는 오케스트레이션.
     */
    @Bean
    public ShopRiderGuideService shopRiderGuideService(
        ShopRiderGuideRepository shopRiderGuideRepository,
        ShopRepository shopRepository,
        ShopRiderGuideValidator shopRiderGuideValidator,
        ShopChangeHistoryRecorder shopChangeHistoryRecorder
    ) {
        return new ShopRiderGuideService(
            shopRiderGuideRepository,
            shopRepository,
            shopRiderGuideValidator,
            shopChangeHistoryRecorder
        );
    }

    /**
     * 가게 변경이력 기록 — 변경을 수행하는 도메인 서비스들이 같은 트랜잭션에서 동기 호출한다.
     *
     * <p>{@code ShopChangeValueFormatter}는 상태 없는 static 유틸이라 빈으로 등록하지 않는다.
     */
    @Bean
    public ShopChangeHistoryRecorder shopChangeHistoryRecorder(
        ShopChangeHistoryRepository shopChangeHistoryRepository
    ) {
        return new ShopChangeHistoryRecorder(shopChangeHistoryRepository);
    }

    /**
     * 가게-점주 접근권한 이력 기록 — 배정·해제를 수행하는 도메인 서비스가 같은 트랜잭션에서 동기 호출한다.
     * 새 배정 경로를 만들면 그 도메인 서비스에 이 Recorder를 배선해야 한다.
     */
    @Bean
    public ShopCeoAssignmentRecorder shopCeoAssignmentRecorder(
        ShopCeoAssignmentHistoryRepository shopCeoAssignmentHistoryRepository
    ) {
        return new ShopCeoAssignmentRecorder(shopCeoAssignmentHistoryRepository);
    }

    /**
     * 가게 담당 점주 배정·해제 불변식 — {@code SHOP.ceo_id} 갱신과 접근권한 이력 기록을 원자적으로
     * 수행하고, 재배정을 {@code REVOKE}+{@code GRANT} 2행으로 남긴다.
     */
    @Bean
    public ShopCeoAssignmentService shopCeoAssignmentService(
        ShopRepository shopRepository,
        CeoRepository ceoRepository,
        ShopCeoAssignmentRecorder shopCeoAssignmentRecorder
    ) {
        return new ShopCeoAssignmentService(
            shopRepository,
            ceoRepository,
            shopCeoAssignmentRecorder
        );
    }

    /**
     * 요청처리 현황 인덱스 기록·동기화 — 원본 상태 전이와 같은 트랜잭션에서 파생 읽기모델을 갱신한다.
     * 요청 성격의 애그리거트를 새로 만들면 그 도메인 서비스에 이 Recorder를 배선해야 한다.
     */
    @Bean
    public ShopRequestIndexRecorder shopRequestIndexRecorder(
        ShopRequestIndexRepository shopRequestIndexRepository
    ) {
        return new ShopRequestIndexRecorder(shopRequestIndexRepository);
    }

    /**
     * 요청 취소 — 유형별 원본 애그리거트의 {@code cancel()}(PENDING만 허용)과 인덱스 동기화를 함께 수행한다.
     */
    @Bean
    public ShopRequestCancelService shopRequestCancelService(
        ShopImageChangeRequestRepository shopImageChangeRequestRepository,
        ShopDeliveryAreaAdjustmentRequestRepository shopDeliveryAreaAdjustmentRequestRepository,
        ReviewBlindRequestRepository reviewBlindRequestRepository,
        ShopRequestIndexRecorder shopRequestIndexRecorder
    ) {
        return new ShopRequestCancelService(
            shopImageChangeRequestRepository,
            shopDeliveryAreaAdjustmentRequestRepository,
            reviewBlindRequestRepository,
            shopRequestIndexRecorder
        );
    }

    /**
     * 요청건 문의 스레드 작성 — 실재하는 요청에만 댓글이 달리도록 인덱스 행을 확인한다(점주 경로는 가게
     * 일치까지 재검증).
     */
    @Bean
    public ShopRequestCommentService shopRequestCommentService(
        ShopRequestCommentRepository shopRequestCommentRepository,
        ShopRequestIndexRecorder shopRequestIndexRecorder
    ) {
        return new ShopRequestCommentService(shopRequestCommentRepository, shopRequestIndexRecorder);
    }
}
