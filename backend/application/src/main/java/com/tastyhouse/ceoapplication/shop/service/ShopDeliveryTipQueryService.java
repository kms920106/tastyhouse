package com.tastyhouse.ceoapplication.shop.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.ceoapplication.shop.port.in.ShopDeliveryTipQueryUseCase;
import com.tastyhouse.application.shop.port.out.ShopDeliveryTipQueryPort;
import com.tastyhouse.application.shop.port.out.ShopDeliveryTipRegionResult;
import com.tastyhouse.application.shop.port.out.ShopDeliveryTipScheduleResult;
import com.tastyhouse.application.shop.port.out.ShopDeliveryTipSettingResult;
import com.tastyhouse.application.shop.port.out.ShopDeliveryTipTierResult;
import com.tastyhouse.application.shop.port.out.ShopDeliveryTipOwnerViewResult;

/**
 * 점주용 가게 배달팁 조회 서비스(CQRS query 측).
 *
 * <p>행정동 이름 조립은 infra query DAO가 조인으로 완성하므로 이 서비스는 소유권 검증과 Result 언패킹만
 * 담당한다. write 포트({@code ShopDeliveryTipRepository})는 주입하지 않는다(CQRS 교차 주입 금지).
 *
 * <p><b>설정 헤더 행이 없는 가게도 200을 반환한다</b> — 배달팁을 아직 설정하지 않은 것은 점주가 설정
 * 화면에 처음 들어온 정상 상태이지 리소스 부재가 아니므로 404를 던지지 않는다. 이때
 * {@code extraTipType}은 {@code "NONE"}, {@code distance}는 {@code null}, 목록 3종은 빈 배열,
 * {@code holidayTipAmount}는 0으로 내려간다(빈 화면을 그리기 위해 프론트가 별도 분기를 두지 않아도 된다).
 */
@Service
@Transactional(readOnly = true)
public class ShopDeliveryTipQueryService implements ShopDeliveryTipQueryUseCase {

    private final ShopDeliveryTipQueryPort shopDeliveryTipQueryPort;
    private final ShopOwnershipValidator shopOwnershipValidator;

    public ShopDeliveryTipQueryService(ShopDeliveryTipQueryPort shopDeliveryTipQueryPort, ShopOwnershipValidator shopOwnershipValidator) {
        this.shopDeliveryTipQueryPort = shopDeliveryTipQueryPort;
        this.shopOwnershipValidator = shopOwnershipValidator;
    }

    @Override
    public ShopDeliveryTipOwnerViewResult getDeliveryTips(Long ceoId, Long shopId) {
        shopOwnershipValidator.validateOwnership(ceoId, shopId);

        ShopDeliveryTipSettingResult setting = shopDeliveryTipQueryPort.findSetting(shopId).orElse(null);

        List<ShopDeliveryTipTierResult> tiers = shopDeliveryTipQueryPort.findTiers(shopId);
        List<ShopDeliveryTipRegionResult> regions = shopDeliveryTipQueryPort.findRegionTips(shopId);
        List<ShopDeliveryTipScheduleResult> schedules = shopDeliveryTipQueryPort.findScheduleTips(shopId);

        return new ShopDeliveryTipOwnerViewResult(
            setting,
            tiers,
            regions,
            schedules,
            shopDeliveryTipQueryPort.findHolidayTipAmount(shopId)
        );
    }
}
