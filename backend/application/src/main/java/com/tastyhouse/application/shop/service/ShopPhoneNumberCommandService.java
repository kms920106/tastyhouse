package com.tastyhouse.application.shop.service;

import com.tastyhouse.application.shared.marker.CeoApp;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.domain.shop.model.ShopChangeActor;
import com.tastyhouse.domain.shop.service.ShopPhoneNumberRegistryService;
import com.tastyhouse.application.shop.port.in.ShopPhoneNumberCommandUseCase;
import com.tastyhouse.application.shop.port.in.ShopPhoneNumberCreateCommand;
import com.tastyhouse.application.shop.port.in.ShopPhoneNumberDeleteCommand;
import com.tastyhouse.application.shop.port.in.ShopPhoneNumberPrimaryDesignateCommand;

/**
 * 점주용 가게 전화번호 변경 서비스(CQRS command 측).
 *
 * <p>최대 10건 제한·대표번호 승계·가게 대표 전화번호 동기화 불변식은 도메인 서비스
 * {@link ShopPhoneNumberRegistryService}가 담당한다.
 *
 * <p><b>소유권 검증 한계</b>: 생성은 {@code shopId} 경로 변수로 소유권을 검증한다. 삭제·대표지정은
 * {@code phoneNumberId}만 경로에 있고 write 포트에 단건 조회가 있으나 목록 소유 shopId 역조회를
 * 별도로 하지 않아, ceo-api 계층에서는 소유권을 검증하지 않고 도메인 서비스에 위임한다(기존 동작 유지).
 * 다만 변경이력의 주체를 남기기 위해 {@code ceoId}는 전달받는다.
 *
 * <p><b>변경이력</b>: 전화번호({@code PHONE_NUMBER})·대표번호({@code REPRESENTATIVE_PHONE}) 기록은
 * 변경 전 값을 추가 조회 없이 볼 수 있는 도메인 서비스가 담당하고, 이 서비스는 변경 주체
 * ({@link ShopChangeActor})만 만들어 전달한다.
 */
@Service
@CeoApp
@Transactional
public class ShopPhoneNumberCommandService implements ShopPhoneNumberCommandUseCase {

    private final ShopPhoneNumberRegistryService shopPhoneNumberRegistryService;
    private final ShopOwnershipValidator shopOwnershipValidator;

    public ShopPhoneNumberCommandService(ShopPhoneNumberRegistryService shopPhoneNumberRegistryService, ShopOwnershipValidator shopOwnershipValidator) {
        this.shopPhoneNumberRegistryService = shopPhoneNumberRegistryService;
        this.shopOwnershipValidator = shopOwnershipValidator;
    }

    @Override
    public Long addPhoneNumber(ShopPhoneNumberCreateCommand command) {
        Long ceoId = command.ceoId();
        Long shopId = command.shopId();
        String phoneNumber = command.phoneNumber();
        boolean virtual = command.virtual();

        shopOwnershipValidator.validateOwnership(ceoId, shopId);
        ShopChangeActor actor = ShopChangeActor.ceo(ceoId);
        return shopPhoneNumberRegistryService.addPhoneNumber(shopId, phoneNumber, virtual, actor);
    }

    @Override
    public void deletePhoneNumber(ShopPhoneNumberDeleteCommand command) {
        Long ceoId = command.ceoId();
        Long phoneNumberId = command.phoneNumberId();

        ShopChangeActor actor = ShopChangeActor.ceo(ceoId);
        shopPhoneNumberRegistryService.deletePhoneNumber(phoneNumberId, actor);
    }

    @Override
    public void designatePrimary(ShopPhoneNumberPrimaryDesignateCommand command) {
        Long ceoId = command.ceoId();
        Long phoneNumberId = command.phoneNumberId();

        ShopChangeActor actor = ShopChangeActor.ceo(ceoId);
        shopPhoneNumberRegistryService.designatePrimary(phoneNumberId, actor);
    }
}
