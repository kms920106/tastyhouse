package com.tastyhouse.ceoapi.shop;

import java.util.List;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import com.tastyhouse.core.domain.shop.application.ShopPhoneNumberCommandService;
import com.tastyhouse.core.domain.shop.application.ShopPhoneNumberQueryService;
import com.tastyhouse.core.domain.shop.application.dto.command.ShopPhoneNumberCreateCommand;
import com.tastyhouse.core.domain.shop.application.dto.result.ShopPhoneNumberResult;
import com.tastyhouse.ceoapi.shop.response.ShopPhoneNumberResponse;

/**
 * 점주용 가게 전화번호 관리 중개 서비스.
 *
 * <p><b>소유권 검증 한계</b>: 생성/목록 조회는 {@code shopId} 경로 변수로 소유권을 검증한다. 삭제·대표지정은
 * {@code phoneNumberId}만 경로에 있고 core에 단건 소유 shopId 조회 메서드가 없어, ceo-api 계층에서는
 * 소유권을 검증하지 않고 core에 위임한다(추후 core에 단건 조회 메서드 추가 시 보강 필요).
 */
@Service
@RequiredArgsConstructor
public class ShopPhoneNumberService {

    private final ShopPhoneNumberQueryService shopPhoneNumberQueryService;
    private final ShopPhoneNumberCommandService shopPhoneNumberCommandService;
    private final ShopOwnershipValidator shopOwnershipValidator;

    public List<ShopPhoneNumberResponse> getPhoneNumbers(Long ceoId, Long shopId) {
        shopOwnershipValidator.validateOwnership(ceoId, shopId);
        return shopPhoneNumberQueryService.findPhoneNumbers(shopId).stream()
            .map(this::toShopPhoneNumberResponse)
            .toList();
    }

    public Long addPhoneNumber(Long ceoId, Long shopId, String phoneNumber, boolean virtual) {
        shopOwnershipValidator.validateOwnership(ceoId, shopId);
        ShopPhoneNumberCreateCommand command = ShopPhoneNumberCreateCommand.of(shopId, phoneNumber, virtual);
        return shopPhoneNumberCommandService.addPhoneNumber(command);
    }

    public void deletePhoneNumber(Long phoneNumberId) {
        shopPhoneNumberCommandService.deletePhoneNumber(phoneNumberId);
    }

    public void designatePrimary(Long phoneNumberId) {
        shopPhoneNumberCommandService.designatePrimary(phoneNumberId);
    }

    private ShopPhoneNumberResponse toShopPhoneNumberResponse(ShopPhoneNumberResult dto) {
        return ShopPhoneNumberResponse.from(
            dto.id(),
            dto.phoneNumber(),
            dto.primary(),
            dto.virtual()
        );
    }
}
