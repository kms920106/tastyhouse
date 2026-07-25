package com.tastyhouse.ceoapi.shop;

import java.time.LocalDate;
import java.util.List;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import com.tastyhouse.core.domain.shop.domain.model.ClosedDayType;
import com.tastyhouse.core.domain.shop.domain.model.Shop;
import com.tastyhouse.core.domain.shop.domain.model.ShopClosedDay;
import com.tastyhouse.core.domain.shop.domain.vo.ShopId;
import com.tastyhouse.core.domain.shop.application.ShopCommandService;
import com.tastyhouse.core.domain.shop.application.ShopQueryService;
import com.tastyhouse.core.domain.shop.application.ShopTemporaryClosureCommandService;
import com.tastyhouse.core.domain.shop.application.ShopTemporaryClosureQueryService;
import com.tastyhouse.core.domain.shop.application.dto.command.ShopClosedDaySaveCommand;
import com.tastyhouse.core.domain.shop.application.dto.command.ShopTemporaryClosureCreateCommand;
import com.tastyhouse.core.domain.shop.application.dto.result.ShopTemporaryClosureResult;
import com.tastyhouse.ceoapi.shop.response.ShopClosedDaysResponse;
import com.tastyhouse.ceoapi.shop.response.ShopRegularClosedDayResponse;
import com.tastyhouse.ceoapi.shop.response.ShopTemporaryClosureResponse;

/**
 * 점주용 휴무(공휴일 토글·정기 휴무·임시 휴무) 관리 중개 서비스.
 * 모든 조회·수정은 로그인 점주(ceoId)의 소유 가게로 한정한다.
 */
@Service
@RequiredArgsConstructor
public class ShopClosedDayService {

    private final ShopQueryService shopQueryService;
    private final ShopCommandService shopCommandService;
    private final ShopTemporaryClosureCommandService shopTemporaryClosureCommandService;
    private final ShopTemporaryClosureQueryService shopTemporaryClosureQueryService;
    private final ShopOwnershipValidator shopOwnershipValidator;

    public ShopClosedDaysResponse getClosedDays(Long ceoId, Long shopId) {
        Shop shop = shopOwnershipValidator.validateOwnership(ceoId, shopId);

        List<ShopRegularClosedDayResponse> regularClosedDays = shopQueryService.findShopClosedDays(shopId).stream()
            .map(this::toShopRegularClosedDayResponse)
            .toList();
        List<ShopTemporaryClosureResponse> temporaryClosures = shopTemporaryClosureQueryService.findTemporaryClosures(shopId).stream()
            .map(this::toShopTemporaryClosureResponse)
            .toList();

        return ShopClosedDaysResponse.from(shop.isClosedOnPublicHolidays(), regularClosedDays, temporaryClosures);
    }

    public void updateHolidayClosure(Long ceoId, Long shopId, boolean closedOnPublicHolidays) {
        shopOwnershipValidator.validateOwnership(ceoId, shopId);
        ShopId targetShopId = ShopId.of(shopId);
        shopCommandService.updateHolidayClosure(targetShopId, closedOnPublicHolidays);
    }

    public Long createClosedDay(Long ceoId, Long shopId, String closedDayType) {
        shopOwnershipValidator.validateOwnership(ceoId, shopId);
        ShopClosedDaySaveCommand command = ShopClosedDaySaveCommand.of(ClosedDayType.from(closedDayType));
        ShopClosedDay closedDay = shopCommandService.createClosedDay(shopId, command);
        return closedDay.getId();
    }

    /**
     * 정기 휴무 삭제는 경로에 shopId가 없다. 소유권 검증을 위한 소속 조회 메서드가 없어(단건 조회 미제공),
     * ceo-api 계층에서는 소유권을 검증하지 않고 core에 위임한다(추후 core에 findClosedDayById 추가 시 보강 필요).
     */
    public void deleteClosedDay(Long closedDayId) {
        shopCommandService.deleteClosedDay(closedDayId);
    }

    public Long createTemporaryClosure(Long ceoId, Long shopId, LocalDate startDate, LocalDate endDate) {
        shopOwnershipValidator.validateOwnership(ceoId, shopId);
        ShopTemporaryClosureCreateCommand command = ShopTemporaryClosureCreateCommand.of(shopId, startDate, endDate);
        return shopTemporaryClosureCommandService.createTemporaryClosure(command);
    }

    /**
     * 임시 휴무 삭제도 경로에 shopId가 없다. 소유권 검증을 위한 소속 조회 메서드가 없어(단건 조회 미제공),
     * ceo-api 계층에서는 소유권을 검증하지 않고 core에 위임한다(추후 core에 findTemporaryClosureById 추가 시 보강 필요).
     */
    public void deleteTemporaryClosure(Long temporaryClosureId) {
        shopTemporaryClosureCommandService.deleteTemporaryClosure(temporaryClosureId);
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
