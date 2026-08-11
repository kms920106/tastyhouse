package com.tastyhouse.ceoapi.shop;

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

/**
 * 점주용 가게 최소주문금액 변경 서비스(CQRS command 측).
 *
 * <p>설정 범위(0=미설정, 또는 5,000~30,000원)와 폐업 가드는 도메인 불변식이므로
 * {@link Shop#changeMinOrderAmount(int)}가 담당한다. 이 서비스는 소유권 검증과 트랜잭션 경계,
 * 그리고 변경 후 명시적 저장만 책임진다.
 *
 * <p><b>변경이력({@code MIN_ORDER_AMOUNT})을 예외적으로 이 서비스가 남긴다.</b> 다른 배달 분류는 기록을
 * 도메인 서비스가 소유하는데, 최소주문금액에는 대응 도메인 서비스가 없고 이 서비스가
 * {@link ShopOwnershipValidator}를 통해 <b>이미 {@code Shop} 애그리거트를 손에 들고</b> 있어 변경 전 값을
 * 추가 조회 없이 볼 수 있다. 기록만을 위해 도메인 서비스를 새로 만들면 불변식이 없는 껍데기가 하나 늘어난다.
 */
@Service
@Transactional
public class ShopMinOrderAmountCommandService {

    private final ShopRepository shopRepository;
    private final ShopOwnershipValidator shopOwnershipValidator;
    private final ShopChangeHistoryRecorder shopChangeHistoryRecorder;

    public ShopMinOrderAmountCommandService(
        ShopRepository shopRepository,
        ShopOwnershipValidator shopOwnershipValidator,
        ShopChangeHistoryRecorder shopChangeHistoryRecorder
    ) {
        this.shopRepository = shopRepository;
        this.shopOwnershipValidator = shopOwnershipValidator;
        this.shopChangeHistoryRecorder = shopChangeHistoryRecorder;
    }

    public void updateMinOrderAmount(Long ceoId, Long shopId, int minOrderAmount) {
        Shop shop = shopOwnershipValidator.validateOwnership(ceoId, shopId);
        String previousValue = describeMinOrderAmount(shop.getMinOrderAmount());

        shop.changeMinOrderAmount(minOrderAmount);
        shopRepository.save(shop);

        ShopId id = ShopId.of(shopId);
        ShopChangeActor actor = ShopChangeActor.ceo(ceoId);
        shopChangeHistoryRecorder.record(
            id,
            ShopChangeType.MIN_ORDER_AMOUNT,
            ShopChangeActionType.UPDATE,
            actor,
            previousValue,
            describeMinOrderAmount(shop.getMinOrderAmount())
        );
    }

    /**
     * 최소주문금액을 한 줄로 요약한다(예: {@code "12,000원"}).
     *
     * <p>0원은 금액이 아니라 <b>미설정</b>을 뜻하는 도메인 규약이므로 "0원"이 아니라 "미설정"으로 적는다 —
     * 그대로 적으면 이력을 읽는 사람이 "최소주문금액 0원으로 바꿨다"로 오해한다.
     */
    private String describeMinOrderAmount(int minOrderAmount) {
        return minOrderAmount == 0
            ? ShopChangeValueFormatter.unset()
            : ShopChangeValueFormatter.amount(minOrderAmount);
    }
}
