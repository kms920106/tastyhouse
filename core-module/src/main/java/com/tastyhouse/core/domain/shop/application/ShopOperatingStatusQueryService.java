package com.tastyhouse.core.domain.shop.application;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.core.domain.shop.domain.model.Shop;
import com.tastyhouse.core.domain.shop.domain.model.ShopBreakTime;
import com.tastyhouse.core.domain.shop.domain.model.ShopBusinessHour;
import com.tastyhouse.core.domain.shop.domain.model.ShopClosedDay;
import com.tastyhouse.core.domain.shop.domain.model.ShopOperatingStatus;
import com.tastyhouse.core.domain.shop.domain.model.ShopSuspension;
import com.tastyhouse.core.domain.shop.domain.model.ShopTemporaryClosure;
import com.tastyhouse.core.domain.shop.domain.repository.ShopDetailRepository;
import com.tastyhouse.core.domain.shop.domain.repository.ShopRepository;
import com.tastyhouse.core.domain.shop.domain.repository.ShopSuspensionRepository;
import com.tastyhouse.core.domain.shop.domain.repository.ShopTemporaryClosureRepository;
import com.tastyhouse.core.domain.shop.domain.vo.ShopId;
import com.tastyhouse.core.exception.EntityNotFoundException;
import com.tastyhouse.core.exception.ErrorCode;

/**
 * 가게 실시간 영업 상태(영업중/준비중) 조회 서비스.
 *
 * <p>영업시간·휴게시간·정기휴무·임시휴무·임시중지를 조회해 {@link ShopOperatingStatusCalculator}에 위임한다.
 *
 * <p><b>공휴일 판정 한계</b>: 코드베이스에 공휴일 캘린더 소스가 없어 현재는 {@code publicHoliday=false}로
 * 고정 전달한다. 향후 공휴일 캘린더 도입 시 이 서비스의 {@code publicHoliday} 계산 지점만 교체하면 된다.
 */
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class ShopOperatingStatusQueryService {

    private static final boolean PUBLIC_HOLIDAY = false;

    private final ShopRepository shopRepository;
    private final ShopDetailRepository shopDetailRepository;
    private final ShopTemporaryClosureRepository shopTemporaryClosureRepository;
    private final ShopSuspensionRepository shopSuspensionRepository;
    private final ShopOperatingStatusCalculator shopOperatingStatusCalculator;

    public ShopOperatingStatus findOperatingStatus(Long shopId, LocalDateTime now) {
        Shop shop = shopRepository.findById(ShopId.of(shopId))
            .orElseThrow(() -> new EntityNotFoundException(ErrorCode.SHOP_NOT_FOUND));
        return calculate(shop, shopId, now);
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
                shopId -> shopRepository.findById(ShopId.of(shopId))
                    .map(shop -> calculate(shop, shopId, now))
                    .orElse(ShopOperatingStatus.PREPARING)
            ));
    }

    private ShopOperatingStatus calculate(Shop shop, Long shopId, LocalDateTime now) {
        List<ShopBusinessHour> businessHours = shopDetailRepository.findBusinessHoursByShopId(shopId);
        List<ShopBreakTime> breakTimes = shopDetailRepository.findBreakTimesByShopId(shopId);
        List<ShopClosedDay> closedDays = shopDetailRepository.findClosedDaysByShopId(shopId);
        List<ShopTemporaryClosure> temporaryClosures = shopTemporaryClosureRepository.findByShopId(shopId);
        List<ShopSuspension> suspensions = shopSuspensionRepository.findByShopId(shopId);

        return shopOperatingStatusCalculator.calculate(
            shop,
            businessHours,
            breakTimes,
            closedDays,
            temporaryClosures,
            suspensions,
            PUBLIC_HOLIDAY,
            now
        );
    }
}
