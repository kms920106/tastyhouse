package com.tastyhouse.ceoapi.shop;

import java.util.List;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.core.domain.shop.domain.model.Shop;
import com.tastyhouse.core.domain.shop.domain.model.ShopClosedDay;
import com.tastyhouse.core.domain.shop.domain.repository.ShopDetailRepository;
import com.tastyhouse.infrastructure.shop.query.ShopQueryDao;
import com.tastyhouse.infrastructure.shop.query.ShopTemporaryClosureResult;
import com.tastyhouse.ceoapi.shop.response.ShopClosedDaysResponse;
import com.tastyhouse.ceoapi.shop.response.ShopRegularClosedDayResponse;
import com.tastyhouse.ceoapi.shop.response.ShopTemporaryClosureResponse;

/**
 * 점주용 휴무(공휴일 토글·정기 휴무·임시 휴무) 조회 서비스(CQRS query 측).
 *
 * <p>공휴일 휴무 여부는 소유권 검증이 반환한 가게 도메인에서, 정기휴무는 write 포트에 잔류한 목록
 * 조회(등록 개수 제한 검증에 쓰임)에서, 임시휴무는 infra query DAO에서 각각 얻어 함께 조립한다.
 */
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class ShopClosedDayQueryService {

    private final ShopDetailRepository shopDetailRepository;
    private final ShopQueryDao shopQueryDao;
    private final ShopOwnershipValidator shopOwnershipValidator;

    public ShopClosedDaysResponse getClosedDays(Long ceoId, Long shopId) {
        Shop shop = shopOwnershipValidator.validateOwnership(ceoId, shopId);

        List<ShopRegularClosedDayResponse> regularClosedDays = shopDetailRepository.findClosedDaysByShopId(shopId).stream()
            .map(this::toShopRegularClosedDayResponse)
            .toList();
        List<ShopTemporaryClosureResponse> temporaryClosures = shopQueryDao.findTemporaryClosures(shopId).stream()
            .map(this::toShopTemporaryClosureResponse)
            .toList();

        return ShopClosedDaysResponse.from(shop.isClosedOnPublicHolidays(), regularClosedDays, temporaryClosures);
    }

    private ShopRegularClosedDayResponse toShopRegularClosedDayResponse(ShopClosedDay closedDay) {
        return ShopRegularClosedDayResponse.from(
            closedDay.getId(),
            closedDay.getClosedDayType().name(),
            closedDay.getClosedDayType().getDescription()
        );
    }

    private ShopTemporaryClosureResponse toShopTemporaryClosureResponse(ShopTemporaryClosureResult dto) {
        return ShopTemporaryClosureResponse.from(
            dto.id(),
            dto.startDate(),
            dto.endDate()
        );
    }
}
