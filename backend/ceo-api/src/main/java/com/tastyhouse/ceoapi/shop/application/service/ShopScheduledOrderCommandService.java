package com.tastyhouse.ceoapi.shop.application.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.domain.shop.model.Shop;
import com.tastyhouse.domain.shop.model.ShopChangeActionType;
import com.tastyhouse.domain.shop.model.ShopChangeActor;
import com.tastyhouse.domain.shop.model.ShopChangeType;
import com.tastyhouse.domain.shop.repository.ShopRepository;
import com.tastyhouse.domain.shop.service.ShopChangeHistoryRecorder;
import com.tastyhouse.domain.shop.service.ShopChangeValueFormatter;
import com.tastyhouse.domain.shop.vo.ShopId;
import com.tastyhouse.ceoapi.shop.ShopOwnershipValidator;
import com.tastyhouse.ceoapi.shop.application.port.in.ShopScheduledOrderCommandUseCase;
import com.tastyhouse.ceoapi.shop.application.port.in.ShopScheduledOrderUpdateCommand;

/**
 * 점주용 가게 예약주문 운영 여부 변경 서비스(CQRS command 측).
 *
 * <p>폐업 가드와 "OFF는 신규 예약만 차단한다"는 규칙은 도메인 불변식이므로
 * {@link Shop#changeScheduledOrderEnabled(boolean)}가 담당한다. 이 서비스는 소유권 검증과 트랜잭션 경계,
 * 그리고 변경 후 명시적 저장만 책임진다({@link ShopMinOrderAmountCommandService}와 동일한 형태).
 *
 * <p><b>변경이력({@code SCHEDULED_ORDER})을 예외적으로 이 서비스가 남긴다.</b> 사유는
 * {@link ShopMinOrderAmountCommandService}와 같다 — 대응 도메인 서비스가 없고, 이 서비스가 소유권 검증으로
 * 이미 {@code Shop}을 손에 들고 있어 변경 전 값을 추가 조회 없이 볼 수 있다.
 * ({@code ScheduledOrderSlotService}는 슬롯을 계산·조회하는 읽기 전용 서비스이므로 이 설정을 바꾸지 않는다.)
 */
@Service
@Transactional
public class ShopScheduledOrderCommandService implements ShopScheduledOrderCommandUseCase {

    private final ShopRepository shopRepository;
    private final ShopOwnershipValidator shopOwnershipValidator;
    private final ShopChangeHistoryRecorder shopChangeHistoryRecorder;

    public ShopScheduledOrderCommandService(
        ShopRepository shopRepository,
        ShopOwnershipValidator shopOwnershipValidator,
        ShopChangeHistoryRecorder shopChangeHistoryRecorder
    ) {
        this.shopRepository = shopRepository;
        this.shopOwnershipValidator = shopOwnershipValidator;
        this.shopChangeHistoryRecorder = shopChangeHistoryRecorder;
    }

    @Override
    public void updateScheduledOrder(ShopScheduledOrderUpdateCommand command) {
        Long ceoId = command.ceoId();
        Long shopId = command.shopId();
        boolean enabled = command.enabled();

        Shop shop = shopOwnershipValidator.validateOwnership(ceoId, shopId);
        String previousValue = describeScheduledOrder(shop.isScheduledOrderEnabled());

        shop.changeScheduledOrderEnabled(enabled);
        shopRepository.save(shop);

        ShopId id = ShopId.of(shopId);
        ShopChangeActor actor = ShopChangeActor.ceo(ceoId);
        shopChangeHistoryRecorder.record(
            id,
            ShopChangeType.SCHEDULED_ORDER,
            ShopChangeActionType.UPDATE,
            actor,
            previousValue,
            describeScheduledOrder(shop.isScheduledOrderEnabled())
        );
    }

    /**
     * 예약주문 설정을 한 줄로 요약한다(예: {@code "예약주문 사용"}).
     *
     * <p>담을 값은 운영 여부 하나뿐이다 — 슬롯 간격·수령 가능 시간대는 저장된 설정이 아니라 영업시간·휴게시간
     * ·임시중지에서 매번 파생되는 계산 결과({@code ScheduledOrderSlotCalculator})라, 이 엔드포인트가 바꾸는
     * 값이 아니고 시점마다 달라져 이력에 박제할 수 없다.
     */
    private String describeScheduledOrder(boolean scheduledOrderEnabled) {
        return "예약주문 " + ShopChangeValueFormatter.enabled(scheduledOrderEnabled);
    }
}
