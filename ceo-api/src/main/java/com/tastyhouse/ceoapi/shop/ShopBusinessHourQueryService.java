package com.tastyhouse.ceoapi.shop;

import java.util.List;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.domain.shop.domain.model.ShopBreakTime;
import com.tastyhouse.domain.shop.domain.model.ShopBusinessHour;
import com.tastyhouse.domain.shop.domain.repository.ShopDetailRepository;
import com.tastyhouse.ceoapi.shop.response.ShopBreakTimeResponse;
import com.tastyhouse.ceoapi.shop.response.ShopBusinessHourResponse;

/**
 * 점주용 영업시간·휴게시간 조회 서비스(CQRS query 측).
 *
 * <p>영업시간·휴게시간은 도메인 서비스가 불변식 검증(휴게시간이 영업시간 범위 내인지)에 쓰는 조회라
 * write 포트({@link ShopDetailRepository})에 잔류했으므로, 조회도 그 포트를 그대로 쓴다. 모든 조회는
 * 로그인 점주(ceoId)의 소유 가게로 한정하며, 소유권 검증은 {@link ShopOwnershipValidator}에 위임한다.
 */
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class ShopBusinessHourQueryService {

    private final ShopDetailRepository shopDetailRepository;
    private final ShopOwnershipValidator shopOwnershipValidator;

    public List<ShopBusinessHourResponse> getBusinessHours(Long ceoId, Long shopId) {
        shopOwnershipValidator.validateOwnership(ceoId, shopId);
        return shopDetailRepository.findBusinessHoursByShopId(shopId).stream()
            .map(this::toShopBusinessHourResponse)
            .toList();
    }

    public List<ShopBreakTimeResponse> getBreakTimes(Long ceoId, Long shopId) {
        shopOwnershipValidator.validateOwnership(ceoId, shopId);
        return shopDetailRepository.findBreakTimesByShopId(shopId).stream()
            .map(this::toShopBreakTimeResponse)
            .toList();
    }

    private ShopBusinessHourResponse toShopBusinessHourResponse(ShopBusinessHour businessHour) {
        return ShopBusinessHourResponse.from(
            businessHour.getId(),
            businessHour.getDayType().name(),
            businessHour.getDayType().getDescription(),
            businessHour.getOpenTime(),
            businessHour.getCloseTime(),
            businessHour.getIsClosed(),
            businessHour.getIs24Hours()
        );
    }

    private ShopBreakTimeResponse toShopBreakTimeResponse(ShopBreakTime breakTime) {
        return ShopBreakTimeResponse.from(
            breakTime.getId(),
            breakTime.getDayType().name(),
            breakTime.getDayType().getDescription(),
            breakTime.getStartTime(),
            breakTime.getEndTime()
        );
    }
}
