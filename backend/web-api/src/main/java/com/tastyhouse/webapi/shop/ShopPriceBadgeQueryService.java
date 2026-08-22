package com.tastyhouse.webapi.shop;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.domain.product.model.ProductPrice;
import com.tastyhouse.domain.product.service.StorePriceBadgePolicy;
import com.tastyhouse.domain.product.vo.ProductId;
import com.tastyhouse.domain.shop.model.ShopBusinessHour;
import com.tastyhouse.domain.shop.service.ShopOperatingStatusCalculator;
import com.tastyhouse.domain.shop.vo.ShopId;
import com.tastyhouse.infrastructure.product.query.ProductPriceResult;
import com.tastyhouse.infrastructure.product.query.ProductQueryDao;
import com.tastyhouse.infrastructure.shop.query.ShopBusinessHourResult;
import com.tastyhouse.infrastructure.shop.query.ShopClosedDayResult;
import com.tastyhouse.infrastructure.shop.query.ShopQueryDao;
import com.tastyhouse.webapi.shop.response.ShopPriceBadgeResponse;

/**
 * 손님용 가게 매장가격 뱃지 조회 서비스(CQRS query 측).
 *
 * <p>대형 {@code ShopQueryService}에 메서드를 얹지 않고 별도 서비스로 둔다 — 뱃지 판정은 그 클래스의
 * 다수 협력 빈을 하나도 쓰지 않고, 대신 가격·메뉴 수라는 product 쪽 입력을 읽는다
 * ({@code ShopOriginInfoQueryService}·{@code ShopOrderNoticeQueryService}가 같은 판단을 따른다).
 *
 * <p><b>판정 규칙은 이 서비스가 갖지 않는다.</b> 두 뱃지의 조건은 도메인 정책
 * {@link StorePriceBadgePolicy}가 소유하고, 이 서비스는 그 입력(가격 행·메뉴 수·지나간 영업일)을
 * 조회해 넘기기만 한다 — 같은 판정을 손님 화면·점주 화면·배치가 각자의 조회 경로로 재사용해야 한다.
 *
 * <p><b>write 포트를 주입하지 않는다.</b> 정책이 요구하는 가격 행과 메뉴 수는 write 포트
 * ({@code ProductPriceRepository#findAllByShopId}·{@code ProductRepository#countVisibleByShopId})에도
 * 있지만, 조회 서비스의 write 포트 주입은 ArchUnit {@code queryServicesShouldNotDependOnWritePorts}가
 * 금지하므로 같은 조건으로 투영하는 infra query DAO를 쓴다. 인증 플래그만은 DAO가 없어 읽기 전용
 * 협력 빈 {@link StorePriceVerificationReader}에 가둔다.
 */
@Service
@Transactional(readOnly = true)
public class ShopPriceBadgeQueryService {

    /**
     * 픽업가 설정일 이후 영업일을 찾을 최대 일수.
     *
     * <p>정책이 묻는 것은 "설정일 이후 영업일이 <b>한 번이라도</b> 지났는가"뿐이므로, 설정일부터 오늘까지
     * 전 구간을 훑을 필요가 없다. 다만 주 1회만 영업하는 가게도 판정되려면 한 주는 넘겨야 해서
     * {@code ShopNextOpenTimeCalculator}와 같은 7일 창을 쓴다. 설정일이 7일보다 오래됐으면 오늘로부터
     * 거꾸로 7일만 보면 되고, 그 사이 영업일이 하나도 없다면 실제로 영업하지 않는 가게다.
     */
    private static final int BUSINESS_DAY_WINDOW_DAYS = 7;

    /**
     * 공휴일 판정은 하지 않는다({@code false} 고정).
     *
     * <p>이 컨텍스트에는 공휴일 캘린더 소스가 없다 — {@code OrderProductValidationService}·
     * {@code ShopOperatingStatusService}가 같은 이유로 같은 단순화를 하고 있어 그것을 그대로 따른다.
     * 결과적으로 {@code DayType.HOLIDAY} 행만 등록한 가게는 그 날이 영업일로 세어지지 않아 뱃지가
     * 늦게 켜질 수 있는데, 뱃지를 <b>일찍</b> 켜는 방향이 아니므로 안전한 편의 오차다. 공휴일 캘린더가
     * 도입되면 위 두 곳과 <b>함께</b> 교체한다.
     */
    private static final boolean PUBLIC_HOLIDAY = false;

    private final ProductQueryDao productQueryDao;
    private final ShopQueryDao shopQueryDao;
    private final StorePriceVerificationReader storePriceVerificationReader;
    private final StorePriceBadgePolicy storePriceBadgePolicy;
    private final ShopOperatingStatusCalculator shopOperatingStatusCalculator;

    public ShopPriceBadgeQueryService(
        ProductQueryDao productQueryDao,
        ShopQueryDao shopQueryDao,
        StorePriceVerificationReader storePriceVerificationReader,
        StorePriceBadgePolicy storePriceBadgePolicy,
        ShopOperatingStatusCalculator shopOperatingStatusCalculator
    ) {
        this.productQueryDao = productQueryDao;
        this.shopQueryDao = shopQueryDao;
        this.storePriceVerificationReader = storePriceVerificationReader;
        this.storePriceBadgePolicy = storePriceBadgePolicy;
        this.shopOperatingStatusCalculator = shopOperatingStatusCalculator;
    }

    /**
     * 가게의 뱃지 2종 노출 여부를 판정한다.
     *
     * <p>가게가 없거나 메뉴·가격이 하나도 없어도 예외를 던지지 않고 두 플래그가 모두 {@code false}인
     * 응답을 준다 — 뱃지는 <b>부가 표시</b>라 판정 불가가 가게 화면 전체를 깨서는 안 된다.
     */
    public ShopPriceBadgeResponse getPriceBadges(Long shopId) {
        LocalDateTime now = LocalDateTime.now();

        boolean sameAsStorePrice = storePriceBadgePolicy.shouldExposeSameAsStorePriceBadge(
            storePriceVerificationReader.readVerified(shopId));

        List<ProductPrice> prices = productQueryDao.findShopProductPrices(shopId).stream()
            .map(ShopPriceBadgeQueryService::toProductPrice)
            .toList();
        boolean storePricePickup = storePriceBadgePolicy.shouldExposePickupBadge(
            prices,
            productQueryDao.countVisibleProducts(shopId),
            findPassedBusinessDates(shopId, now),
            now
        );

        return ShopPriceBadgeResponse.from(sameAsStorePrice, storePricePickup);
    }

    /**
     * 최근 {@value #BUSINESS_DAY_WINDOW_DAYS}일 중 <b>이미 지나간 영업일</b>을 산출한다 — 정책의
     * "픽업가 설정 익일(영업일)부터 노출" 판정 입력이다.
     *
     * <p><b>오늘은 포함하지 않는다.</b> 정책은 설정일 <em>이후</em>의 영업일이 시작됐는지를 묻는데,
     * 오늘을 넣으면 오늘 픽업가를 설정한 가게가 (오늘이 영업일이면) 곧바로 뱃지를 얻는다. 그러면
     * 손님이 뱃지를 보고 방문했을 때 가격이 아직 반영되지 않았을 수 있어 규정의 취지가 깨진다.
     *
     * <p><b>영업일 판정 규칙을 새로 짜지 않는다.</b> 요일별 영업시간 선택(개별요일 &gt; 주말/평일 &gt;
     * 공휴일 &gt; 매일의 구체성 우선)은 {@link ShopOperatingStatusCalculator#selectApplicableHour}가
     * 이미 소유하고 재사용 목적으로 {@code public}이므로 그것에 위임한다 — 복제하면 요일 구분을
     * 추가할 때 한쪽만 고쳐진다({@code ShopNextOpenTimeCalculator}가 같은 이유로 같은 위임을 한다).
     */
    private List<LocalDate> findPassedBusinessDates(Long shopId, LocalDateTime now) {
        List<ShopBusinessHour> businessHours = shopQueryDao.findBusinessHours(shopId).stream()
            .map(businessHour -> toShopBusinessHour(shopId, businessHour))
            .toList();
        if (businessHours.isEmpty()) {
            return List.of();
        }

        List<ShopClosedDayResult> closedDays = shopQueryDao.findClosedDays(shopId);
        LocalDate today = now.toLocalDate();
        List<LocalDate> businessDates = new ArrayList<>();
        for (int offset = BUSINESS_DAY_WINDOW_DAYS; offset >= 1; offset--) {
            LocalDate candidate = today.minusDays(offset);
            if (isBusinessDate(businessHours, closedDays, candidate)) {
                businessDates.add(candidate);
            }
        }
        return businessDates;
    }

    /**
     * 그 날에 가게가 영업했는지 판정한다 — 정기휴무에 걸리지 않고, 적용 영업시간 행이 휴무가 아니어야 한다.
     *
     * <p>{@code is24Hours}는 <b>영업일로 센다</b>. {@code ShopNextOpenTimeCalculator}가 24시간 영업을
     * 건너뛰는 것은 그쪽이 "오픈 <em>시각</em>"을 필요로 해서이고, 여기서 묻는 것은 "그 날 영업했는가"라
     * 24시간 영업은 당연히 영업일이다.
     */
    private boolean isBusinessDate(
        List<ShopBusinessHour> businessHours,
        List<ShopClosedDayResult> closedDays,
        LocalDate date
    ) {
        boolean closedDay = closedDays.stream()
            .anyMatch(closed -> closed.closedDayType() != null && closed.closedDayType().matches(date));
        if (closedDay) {
            return false;
        }

        ShopBusinessHour hour = shopOperatingStatusCalculator.selectApplicableHour(
            businessHours, date.getDayOfWeek(), PUBLIC_HOLIDAY);
        return hour != null && !hour.isClosed();
    }

    /**
     * 영업시간 read model을 도메인 모델로 되짚어 올린다 — {@code selectApplicableHour}가 도메인 모델
     * 목록을 받기 때문이다. 식별자 외 값은 그대로 옮기며 검증을 거치지 않는 {@code reconstitute}를 쓴다
     * (기존 데이터가 현행 규격을 위반해도 조회는 되어야 한다).
     */
    private static ShopBusinessHour toShopBusinessHour(Long shopId, ShopBusinessHourResult dto) {
        return ShopBusinessHour.reconstitute(
            dto.id(),
            ShopId.of(shopId),
            dto.dayType(),
            dto.openTime(),
            dto.closeTime(),
            dto.closed(),
            dto.allDay()
        );
    }

    /**
     * 가격 read model을 도메인 모델로 되짚어 올린다 — 정책이 매장가·픽업가·설정 시각을 도메인 모델의
     * 술어({@code hasStoreAndPickupPrice} 등)로 판정하기 때문이다. 그 술어를 이 서비스에서 다시 쓰면
     * 같은 조건이 두 곳에 생긴다.
     */
    private static ProductPrice toProductPrice(ProductPriceResult dto) {
        return ProductPrice.reconstitute(
            dto.id(),
            ProductId.of(dto.productId()),
            dto.priceName(),
            dto.deliveryPrice(),
            dto.storePrice(),
            dto.pickupPrice(),
            dto.sort(),
            dto.pickupPriceSetAt(),
            null,
            null
        );
    }
}
