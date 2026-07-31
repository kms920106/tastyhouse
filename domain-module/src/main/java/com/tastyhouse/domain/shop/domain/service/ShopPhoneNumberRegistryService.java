package com.tastyhouse.domain.shop.domain.service;

import java.util.List;

import com.tastyhouse.domain.shop.domain.model.Shop;
import com.tastyhouse.domain.shop.domain.model.ShopPhoneNumber;
import com.tastyhouse.domain.shop.domain.repository.ShopPhoneNumberRepository;
import com.tastyhouse.domain.shop.domain.repository.ShopRepository;
import com.tastyhouse.domain.shop.domain.vo.ShopId;
import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.EntityNotFoundException;
import com.tastyhouse.domain.exception.ErrorCode;

/**
 * 가게 전화번호 목록 불변식(도메인 서비스).
 *
 * <p>가게 전화번호는 목록({@code ShopPhoneNumber} 다건)과 가게 대표 전화번호({@code Shop.phoneNumber})가
 * <b>항상 일치</b>해야 한다. 대표번호가 바뀌거나 삭제되면 가게 애그리거트의 전화번호도 같은 트랜잭션에서
 * 함께 갱신되어야 하며(그렇지 않으면 목록과 상세의 번호가 어긋난다), "대표는 정확히 한 건"이라는
 * 불변식도 유지되어야 한다. {@code ShopPhoneNumber}와 {@code Shop} 두 애그리거트 타입을 함께
 * load &amp; save 하는 불변식 오케스트레이션(분류 C)이므로 도메인 계층에 둔다.
 *
 * <p>정책: 가게당 최대 {@value #MAX_PHONE_NUMBER_COUNT}건, 첫 등록분이 자동으로 대표가 되며, 대표를
 * 삭제하면 남은 번호 중 첫 건이 대표를 승계한다.
 *
 * <p>{@code @Service}/{@code @Transactional} 없는 순수 POJO이며(공통 지침 패턴 1), 빈 등록은
 * infrastructure-module의 {@code DomainServiceConfig}가 담당한다. 트랜잭션 경계는 이 서비스를 호출하는
 * 소비 모듈의 command 서비스가 선언한다.
 *
 * <p>도메인 모델은 순수 POJO라 더티 체킹이 없으므로 변경 후 명시적으로 {@code save}를 호출한다.
 */
public class ShopPhoneNumberRegistryService {

    private static final int MAX_PHONE_NUMBER_COUNT = 10;

    private final ShopPhoneNumberRepository shopPhoneNumberRepository;
    private final ShopRepository shopRepository;

    public ShopPhoneNumberRegistryService(
        ShopPhoneNumberRepository shopPhoneNumberRepository,
        ShopRepository shopRepository
    ) {
        this.shopPhoneNumberRepository = shopPhoneNumberRepository;
        this.shopRepository = shopRepository;
    }

    /**
     * 전화번호를 추가한다. 가게의 첫 번호면 대표로 지정되고 가게 대표 전화번호도 함께 갱신된다.
     *
     * @return 생성된 전화번호 식별자
     */
    public Long addPhoneNumber(Long shopId, String phoneNumber, boolean virtual) {
        List<ShopPhoneNumber> existingPhoneNumbers = shopPhoneNumberRepository.findByShopId(shopId);
        if (existingPhoneNumbers.size() >= MAX_PHONE_NUMBER_COUNT) {
            throw new BusinessException(ErrorCode.SHOP_PHONE_NUMBER_LIMIT_EXCEEDED);
        }

        boolean primary = existingPhoneNumbers.isEmpty();
        ShopPhoneNumber saved = shopPhoneNumberRepository.save(
            ShopPhoneNumber.of(shopId, phoneNumber, primary, virtual)
        );

        if (primary) {
            syncShopPhoneNumber(shopId, saved.getPhoneNumber());
        }

        return saved.getId();
    }

    /**
     * 전화번호를 삭제한다. 대표번호를 삭제하면 남은 번호 중 첫 건이 대표를 승계하고 가게 대표
     * 전화번호도 함께 갱신된다.
     */
    public void deletePhoneNumber(Long id) {
        ShopPhoneNumber phoneNumber = shopPhoneNumberRepository.findById(id)
            .orElseThrow(() -> new EntityNotFoundException(ErrorCode.SHOP_PHONE_NUMBER_NOT_FOUND));
        shopPhoneNumberRepository.deleteById(id);

        if (!phoneNumber.isPrimary()) {
            return;
        }

        List<ShopPhoneNumber> remainingPhoneNumbers = shopPhoneNumberRepository.findByShopId(phoneNumber.getShopId());
        if (remainingPhoneNumbers.isEmpty()) {
            return;
        }

        ShopPhoneNumber newPrimary = remainingPhoneNumbers.getFirst();
        newPrimary.markPrimary();
        ShopPhoneNumber saved = shopPhoneNumberRepository.save(newPrimary);
        syncShopPhoneNumber(phoneNumber.getShopId(), saved.getPhoneNumber());
    }

    /**
     * 대표번호를 지정한다. 기존 대표는 해제되고 가게 대표 전화번호도 함께 갱신된다.
     */
    public void designatePrimary(Long id) {
        ShopPhoneNumber target = shopPhoneNumberRepository.findById(id)
            .orElseThrow(() -> new EntityNotFoundException(ErrorCode.SHOP_PHONE_NUMBER_NOT_FOUND));

        List<ShopPhoneNumber> phoneNumbers = shopPhoneNumberRepository.findByShopId(target.getShopId());
        for (ShopPhoneNumber phoneNumber : phoneNumbers) {
            if (phoneNumber.isPrimary() && !phoneNumber.getId().equals(target.getId())) {
                phoneNumber.unmarkPrimary();
                shopPhoneNumberRepository.save(phoneNumber);
            }
        }

        target.markPrimary();
        ShopPhoneNumber saved = shopPhoneNumberRepository.save(target);
        syncShopPhoneNumber(saved.getShopId(), saved.getPhoneNumber());
    }

    /**
     * 가게 애그리거트의 대표 전화번호를 목록의 대표번호와 일치시킨다.
     */
    private void syncShopPhoneNumber(Long shopId, String phoneNumber) {
        ShopId targetShopId = ShopId.of(shopId);
        Shop shop = shopRepository.findById(targetShopId)
            .orElseThrow(() -> new EntityNotFoundException(ErrorCode.SHOP_NOT_FOUND));
        shop.changePhoneNumber(phoneNumber);
        shopRepository.save(shop);
    }
}
