package com.tastyhouse.ceoapi.shop;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.core.domain.shop.domain.service.ShopPhoneNumberRegistryService;

/**
 * 점주용 가게 전화번호 변경 서비스(CQRS command 측).
 *
 * <p>최대 10건 제한·대표번호 승계·가게 대표 전화번호 동기화 불변식은 도메인 서비스
 * {@link ShopPhoneNumberRegistryService}가 담당한다.
 *
 * <p><b>소유권 검증 한계</b>: 생성은 {@code shopId} 경로 변수로 소유권을 검증한다. 삭제·대표지정은
 * {@code phoneNumberId}만 경로에 있고 write 포트에 단건 조회가 있으나 목록 소유 shopId 역조회를
 * 별도로 하지 않아, ceo-api 계층에서는 소유권을 검증하지 않고 도메인 서비스에 위임한다(기존 동작 유지).
 */
@Service
@Transactional
@RequiredArgsConstructor
public class ShopPhoneNumberCommandService {

    private final ShopPhoneNumberRegistryService shopPhoneNumberRegistryService;
    private final ShopOwnershipValidator shopOwnershipValidator;

    public Long addPhoneNumber(Long ceoId, Long shopId, String phoneNumber, boolean virtual) {
        shopOwnershipValidator.validateOwnership(ceoId, shopId);
        return shopPhoneNumberRegistryService.addPhoneNumber(shopId, phoneNumber, virtual);
    }

    public void deletePhoneNumber(Long phoneNumberId) {
        shopPhoneNumberRegistryService.deletePhoneNumber(phoneNumberId);
    }

    public void designatePrimary(Long phoneNumberId) {
        shopPhoneNumberRegistryService.designatePrimary(phoneNumberId);
    }
}
