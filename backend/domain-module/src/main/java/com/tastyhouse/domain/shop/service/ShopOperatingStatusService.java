package com.tastyhouse.domain.shop.service;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.tastyhouse.domain.shared.model.OrderMethod;
import com.tastyhouse.domain.shop.model.Shop;
import com.tastyhouse.domain.shop.model.ShopOperatingStatus;
import com.tastyhouse.domain.shop.model.ShopOrderMethod;
import com.tastyhouse.domain.shop.repository.ShopDetailRepository;
import com.tastyhouse.domain.shop.repository.ShopRepository;
import com.tastyhouse.domain.shop.repository.ShopSuspensionRepository;
import com.tastyhouse.domain.shop.repository.ShopTemporaryClosureRepository;
import com.tastyhouse.domain.shop.vo.ShopId;
import com.tastyhouse.domain.exception.ErrorCode;
import com.tastyhouse.domain.exception.ResourceNotFoundException;

/**
 * 가게 실시간 영업 상태(영업중/준비중) 판정 오케스트레이션(도메인 서비스).
 *
 * <p>영업 상태는 가게·영업시간·휴게시간·정기휴무·임시휴무·임시중지 <b>여섯 애그리거트</b>를 모두 읽어야
 * 판정할 수 있다. 이 서비스는 그 조회·조립만 담당하고 판정 규칙 자체는 순수 계산기
 * {@link ShopOperatingStatusCalculator}에 위임한다. 상태 판정 규칙은 소비 액터(web 목록·상세, ceo 설정
 * 화면)가 달라도 동일해야 하므로 도메인 계층에 둔다(분류 C).
 *
 * <p><b>공휴일 판정 한계</b>: 코드베이스에 공휴일 캘린더 소스가 없어 현재는 {@code publicHoliday=false}로
 * 고정 전달한다. 향후 공휴일 캘린더 도입 시 이 서비스의 {@code publicHoliday} 계산 지점만 교체하면 된다
 * ({@code ScheduledOrderSlotCalculator}의 같은 상수와 <b>함께</b> 교체한다).
 *
 * <p><b>가게 전체 상태와 주문유형별 상태를 구분한다</b>: 전자({@link #findOrderAvailability})는
 * 전체 대상 임시중지만 보고, 후자({@link #findOrderMethodAvailabilities})는 그 유형에 걸린 중지도 함께
 * 본다. 배달만 중지한 가게는 가게 상태가 영업중이고 배달 유형만 불가가 된다.
 *
 * <p><b>식별자를 받는 메서드는 {@code findById}로 가게를 읽는다</b>(폐업·노출정지 가게도 조회되며,
 * 그 사유가 담긴 결과를 돌려준다). 회원 노출 경로는 호출 전에 {@code findVisibleById}로 걸러 404를
 * 내보내야 한다 — web-api의 가게 상세·주문수단 조회가 그렇게 하고 있다. 반면 주문 접수 게이트처럼
 * 이 전제를 규약이 아니라 <b>구조로</b> 보장해야 하는 곳은 가게를 파라미터로 받는
 * {@link #findOrderAvailability(Shop, OrderMethod, LocalDateTime)}를 쓴다.
 *
 * <p>{@code @Service}/{@code @Transactional} 없는 순수 POJO이며(공통 지침 패턴 1), 빈 등록은
 * infrastructure-module의 {@code ShopDomainConfig}가 담당한다.
 */
public class ShopOperatingStatusService {

    private static final boolean PUBLIC_HOLIDAY = false;

    private final ShopRepository shopRepository;
    private final ShopDetailRepository shopDetailRepository;
    private final ShopTemporaryClosureRepository shopTemporaryClosureRepository;
    private final ShopSuspensionRepository shopSuspensionRepository;
    private final ShopOperatingStatusCalculator shopOperatingStatusCalculator;

    public ShopOperatingStatusService(
        ShopRepository shopRepository,
        ShopDetailRepository shopDetailRepository,
        ShopTemporaryClosureRepository shopTemporaryClosureRepository,
        ShopSuspensionRepository shopSuspensionRepository,
        ShopOperatingStatusCalculator shopOperatingStatusCalculator
    ) {
        this.shopRepository = shopRepository;
        this.shopDetailRepository = shopDetailRepository;
        this.shopTemporaryClosureRepository = shopTemporaryClosureRepository;
        this.shopSuspensionRepository = shopSuspensionRepository;
        this.shopOperatingStatusCalculator = shopOperatingStatusCalculator;
    }

    /**
     * 가게 전체의 주문가능 상태를 사유와 함께 조회한다({@code orderMethod = null} 판정).
     *
     * <p>유형별 임시중지는 이 판정에 걸리지 않는다 — 배달만 중지한 가게의 <b>가게 상태는 영업중</b>이고
     * 배달 유형만 불가가 된다.
     *
     * @throws ResourceNotFoundException 가게가 없는 경우
     */
    public ShopOperatingStatusResult findOrderAvailability(Long shopId, LocalDateTime now) {
        return findOrderAvailability(shopId, null, now);
    }

    /**
     * 특정 주문유형 기준의 주문가능 상태를 사유와 함께 조회한다.
     *
     * <p>{@code orderMethod}가 null이면 가게 전체 판정이다. 유형을 넘기면 그 유형에 걸린 임시중지와
     * 전체 대상 임시중지를 함께 본다. <b>배정 여부는 판정하지 않는다</b> — 배정 검증은 주문 접수 게이트
     * ({@code ShopOrderAvailabilityService})가 별도 사유로 처리한다.
     *
     * @throws ResourceNotFoundException 가게가 없는 경우
     */
    public ShopOperatingStatusResult findOrderAvailability(Long shopId, OrderMethod orderMethod, LocalDateTime now) {
        Shop shop = shopRepository.findById(ShopId.of(shopId))
            .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.SHOP_NOT_FOUND));
        return calculate(shop, shopId, orderMethod, now);
    }

    /**
     * <b>이미 로드한 가게</b>로 가게 전체·주문유형 두 판정을 한 번의 애그리거트 조회로 함께 계산한다.
     *
     * <p>주문 접수 게이트({@link ShopOrderAvailabilityService})처럼 두 판정이 모두 필요한 호출부를 위한
     * 것이다. {@code findOrderAvailability}를 두 번 부르면 가게와 다섯 자식 애그리거트를 두 벌 읽게 되는데,
     * 판정 입력 중 유형과 무관한 부분은 완전히 같으므로 한 번만 읽으면 된다.
     *
     * <p>가게를 <b>파라미터로 받는 것</b>이 핵심이다 — 호출부가 이미 회원 노출용
     * ({@code findVisibleById})으로 로드한 가게를 그대로 넘기므로, 이 메서드가 {@code findById}로 다시
     * 읽어 폐업·노출정지 가게를 되살리는 일이 구조적으로 불가능하다.
     *
     * @param shop        판정 대상 가게(호출부가 로드 책임을 진다)
     * @param orderMethod 유형별 판정에 쓸 주문유형
     * @param now         판정 기준 시각
     */
    public ShopOrderMethodAvailability findOrderAvailability(Shop shop, OrderMethod orderMethod, LocalDateTime now) {
        ShopOperatingStatusAggregates aggregates = loadAggregates(shop.getId());

        return new ShopOrderMethodAvailability(
            shopOperatingStatusCalculator.calculate(aggregates.toContext(shop, null, PUBLIC_HOLIDAY, now)),
            shopOperatingStatusCalculator.calculate(aggregates.toContext(shop, orderMethod, PUBLIC_HOLIDAY, now))
        );
    }

    /**
     * 가게에 <b>배정된 주문유형</b>별 주문가능 상태를 조회한다. 배정이 0건이면 빈 맵이다(에러 아님).
     *
     * <p>여섯 애그리거트를 <b>한 번만</b> 조회한 뒤 배정된 유형 수만큼 계산기를 재호출한다 — 계산기는
     * 리포지토리를 모르는 순수 함수라 재호출 비용이 DB 왕복이 아니다.
     *
     * <p>유형별 상태는 세 조건의 AND다: (1) 가게 주문가능, (2) 그 유형이 배정됨, (3) 그 유형에 활성
     * 임시중지 없음. 배정된 유형만 담으므로 (2)는 여기서 항상 참이고, {@code ORDER_METHOD_NOT_SUPPORTED}는
     * 주문 접수 검증에서만 쓰인다. (1)과 (3)은 계산기 한 번의 호출로 함께 판정된다.
     *
     * @return 배정 순서를 보존한 맵(유형 → 판정 결과)
     * @throws ResourceNotFoundException 가게가 없는 경우
     */
    public Map<OrderMethod, ShopOperatingStatusResult> findOrderMethodAvailabilities(Long shopId, LocalDateTime now) {
        Shop shop = shopRepository.findById(ShopId.of(shopId))
            .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.SHOP_NOT_FOUND));

        ShopOperatingStatusAggregates aggregates = loadAggregates(shopId);

        Map<OrderMethod, ShopOperatingStatusResult> availabilities = new LinkedHashMap<>();
        for (ShopOrderMethod assigned : shopDetailRepository.findOrderMethodsByShopId(shopId)) {
            OrderMethod orderMethod = assigned.getOrderMethod();
            availabilities.put(orderMethod, shopOperatingStatusCalculator.calculate(
                aggregates.toContext(shop, orderMethod, PUBLIC_HOLIDAY, now)
            ));
        }
        return availabilities;
    }

    /**
     * 여러 가게의 영업 상태를 한 번에 계산한다. 목록 API에서 사용한다.
     *
     * <p>가게별로 영업시간·휴게시간 등을 각각 조회하는 단순 루프 구현이다. 목록 페이지 크기가 작아(≤20)
     * 현재는 허용하나, 대량 조회 시 N+1이 발생하므로 필요 시 shopId 일괄 조회로 최적화한다.
     */
    public Map<Long, ShopOperatingStatus> findOperatingStatuses(List<Long> shopIds, LocalDateTime now) {
        return shopIds.stream()
            .distinct()
            .collect(Collectors.toMap(
                Function.identity(),
                // 알려진 비대칭: 단건 조회는 없는 가게에 SHOP_NOT_FOUND를 던지는데 목록은 PREPARING으로
                // 삼킨다. 목록 한 건이 실패해 전체 응답이 깨지지 않게 하려는 기존 동작이라 유지하되,
                // 유형별 조회(findOrderMethodAvailabilities)에는 이 비대칭을 복제하지 않았다.
                shopId -> shopRepository.findById(ShopId.of(shopId))
                    .map(shop -> calculate(shop, shopId, null, now).status())
                    .orElse(ShopOperatingStatus.PREPARING)
            ));
    }

    private ShopOperatingStatusResult calculate(
        Shop shop,
        Long shopId,
        OrderMethod orderMethod,
        LocalDateTime now
    ) {
        return shopOperatingStatusCalculator.calculate(
            loadAggregates(shopId).toContext(shop, orderMethod, PUBLIC_HOLIDAY, now)
        );
    }

    /**
     * 판정에 필요한 다섯 자식 애그리거트를 한 번에 읽는다. 유형별 판정에서 이 조회 결과를 유형 수만큼
     * 재사용하기 위해 Context 조립과 분리했다.
     */
    private ShopOperatingStatusAggregates loadAggregates(Long shopId) {
        return ShopOperatingStatusAggregates.of(
            shopDetailRepository.findBusinessHoursByShopId(shopId),
            shopDetailRepository.findBreakTimesByShopId(shopId),
            shopDetailRepository.findClosedDaysByShopId(shopId),
            shopTemporaryClosureRepository.findByShopId(shopId),
            shopSuspensionRepository.findByShopId(shopId)
        );
    }
}
